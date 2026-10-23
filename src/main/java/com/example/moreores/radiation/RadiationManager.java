package com.example.moreores.radiation;

import java.util.List;

import com.example.moreores.ModAttributes;
import com.example.moreores.ModBlocks;
import com.example.moreores.crop.ModCrops;
import com.example.moreores.ModItems;
import com.example.moreores.ModRanged;
import com.example.moreores.ModTools;
import com.example.moreores.MoreOres;
import com.example.moreores.effect.ModEffects;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 辐射系统：环境/手持的辐射会累积成「剂量」，剂量再按档位变成 debuff。
 *
 * <pre>
 *   每秒剂量 = 周围铀方块 + 手持铀制品          （铅可以屏蔽一部分）
 *   剂量     = clamp(剂量 + 获得 - 每秒衰减)    （0 ~ 1000）
 *
 *   150+ → 辐射 I    每 2 秒 1 点伤害
 *   400+ → 辐射 II   每 1.25 秒 1 点伤害
 *   700+ → 辐射 III  每 0.6 秒 1 点伤害 + 挖掘疲劳
 * </pre>
 *
 * 喝牛奶只能暂时清掉效果，剂量还在，下一轮结算会重新挂上 —— 想真正恢复只能离开辐射源等它衰减。
 */
public final class RadiationManager {
	/** 每 20 tick（1 秒）结算一次剂量。 */
	private static final int UPDATE_INTERVAL = 20;

	/** 扫描半径，±4 即 9×9×9 = 729 个方块。 */
	private static final int SCAN_RADIUS = 4;

	/** 没有辐射源时每秒衰减的剂量。 */
	private static final int DECAY_PER_SECOND = 3;

	/** 每秒剂量上限，防止玩家盖铀块房子把自己秒了。盖革计数器的响铃频率也按它归一化。 */
	public static final int MAX_EXPOSURE_PER_SECOND = 40;

	/** 效果刷新时长（tick）。 */
	private static final int EFFECT_DURATION = 60;

	/** 挖掉一块铀矿的一次性剂量。 */
	private static final int MINE_DOSE = 25;

	/** 被铀制武器打中后挂上的辐射时长（tick）。 */
	private static final int ATTACK_EFFECT_DURATION = 200;

	/** 被铀箭射中后挂上的辐射时长（tick），比近战短一些。 */
	private static final int ARROW_EFFECT_DURATION = 160;

	/** 吃一块含铅饼干清掉的剂量。 */
	public static final int LEAD_COOKIE_DOSE_CLEAR = 200;

	private static final int THRESHOLD_MILD = 150;
	private static final int THRESHOLD_MODERATE = 400;
	private static final int THRESHOLD_SEVERE = 700;

	/** 周围每个铀方块的每秒剂量。 */
	private static final int ORE_EXPOSURE = 3;
	private static final int RAW_BLOCK_EXPOSURE = 6;
	private static final int METAL_BLOCK_EXPOSURE = 12;

	/** 铀作物（任意生长阶段）的每秒剂量。比矿石略高一点，毕竟整株都是铀。 */
	private static final int URANIUM_CROP_EXPOSURE = 5;

	/**
	 * 手持铀制品（铀锭 / 粗铀）的每秒剂量。
	 *
	 * <p>从 2 提到 **6**：原来 2/s 连自然衰减（3/s）都盖不过，等于「揣着铀锭
	 * 反而比空着手还干净」，完全看不出代价。现在 6 > 3，只要不穿铅甲，
	 * 拿在手里剂量就会缓慢往上涨 —— 铅甲的屏蔽（0.45 × 4 件 = 满 45%）
	 * 刚好能把它压到 3.3/s 以下重新盖过衰减，所以「穿甲才敢拿」这个设计才立得住。
	 */
	private static final int HELD_MATERIAL_EXPOSURE = 6;

	/** 手持铀制工具的每秒剂量 —— 铀工具的核心代价，够高才有"拿着它就危险"的感觉。 */
	private static final int HELD_TOOL_EXPOSURE = 20;

	/*
	 * 「背包里揣铅锭 / 铅块也能挡辐射」已经删除。
	 *
	 * 原来每个铅锭挡 15%、每个铅块挡 100%，等于往背包里塞几个铅块就白嫖满屏蔽 ——
	 * 穿护甲的代价（变慢）被完全绕开了，护甲本身反而没人穿。
	 * 现在只有真正穿在装备栏上的护甲才算，见 shielding()。
	 */

	/**
	 * 铅甲每件屏蔽多少，**不写死在这儿** —— 而是从护甲物品自带的
	 * {@code ATTRIBUTE_MODIFIERS} 里读（见 {@link #armorShielding}）。
	 *
	 * <p>系数写两处迟早会不一致：物品提示显示一个数、实际效果是另一个数。
	 * 所以真值只留一份 —— 挂在物品上的那个修饰符（`ModArmor` 里的
	 * {@code RADIATION_PER_LEAD_PIECE}），提示里显示的就是它，算的时候读的也是它。
	 */

	private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

	private RadiationManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % UPDATE_INTERVAL != 0) {
				return;
			}

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});

		// 挖铀矿当场吃一口剂量
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer serverPlayer && isUraniumOre(state)) {
				addDose(serverPlayer, MINE_DOSE);
			}
		});

		// 铀制武器 / 工具打中目标 → 给目标挂上辐射
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (level.isClientSide() || !(entity instanceof LivingEntity target)) {
				return InteractionResult.PASS;
			}

			if (ModTools.isUraniumTool(player.getItemInHand(hand).getItem())) {
				target.addEffect(new MobEffectInstance(
						ModEffects.RADIATION, ATTACK_EFFECT_DURATION, 0, false, true, true), player);
			}

			return InteractionResult.PASS;
		});

		// 铀箭射中生物 → 挂辐射（箭的伤害来源里取原始箭物品判断）
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) {
				return;
			}

			if (arrow.getPickupItemStackOrigin().is(ModRanged.URANIUM_ARROW)) {
				entity.addEffect(new MobEffectInstance(
						ModEffects.RADIATION, ARROW_EFFECT_DURATION, 0, false, true, true));
			}
		});
	}

	private static void tick(ServerPlayer player) {
		int exposure = exposurePerSecond(player.level(), player);
		double shielding = shielding(player);

		// 屏蔽只影响「新吃到」的剂量，已有的剂量照样衰减
		int gained = (int) Math.round(exposure * (1.0 - shielding));
		int dose = clamp(player.getAttachedOrElse(ModAttachments.RADIATION_DOSE, 0)
				+ gained - DECAY_PER_SECOND);

		setDose(player, dose);

		applyEffects(player, dose);
	}

	/** 写入剂量的唯一入口，方便以后加副作用（比如同步给客户端）。 */
	private static void setDose(ServerPlayer player, int dose) {
		player.setAttached(ModAttachments.RADIATION_DOSE, dose);
	}

	/** 直接往玩家身上加剂量（挖铀矿、辐射僵尸死亡爆发都会用）。 */
	public static void addDose(ServerPlayer player, int amount) {
		setDose(player, clamp(player.getAttachedOrElse(ModAttachments.RADIATION_DOSE, 0) + amount));
	}

	/** 清掉一部分累积剂量（含铅饼干用）。 */
	public static void reduceDose(ServerPlayer player, int amount) {
		setDose(player, clamp(player.getAttachedOrElse(ModAttachments.RADIATION_DOSE, 0) - amount));
	}

	private static int clamp(int dose) {
		return Math.max(0, Math.min(ModAttachments.MAX_DOSE, dose));
	}

	private static boolean isUraniumOre(BlockState state) {
		return state.is(ModBlocks.URANIUM_ORE) || state.is(ModBlocks.DEEPSLATE_URANIUM_ORE);
	}

	/**
	 * 每秒从环境与手持物品获得多少剂量。
	 *
	 * <p>参数用 {@link Level} 而不是 {@link ServerLevel}，因为客户端的盖革计数器
	 * 也要拿这个值来决定「咔哒」声频率 —— 方块数据客户端本来就有。
	 */
	public static int exposurePerSecond(Level level, Player player) {
		int total = 0;

		BlockPos center = player.blockPosition();
		BlockPos min = center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS);
		BlockPos max = center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockState state = level.getBlockState(pos);

			if (isUraniumOre(state)) {
				total += ORE_EXPOSURE;
			} else if (state.is(ModBlocks.RAW_URANIUM_BLOCK)) {
				total += RAW_BLOCK_EXPOSURE;
			} else if (state.is(ModBlocks.URANIUM_BLOCK)) {
				total += METAL_BLOCK_EXPOSURE;
			} else if (ModCrops.isUraniumCrop(state)) {
				total += URANIUM_CROP_EXPOSURE;
			}
		}

		total += heldExposure(player.getMainHandItem());
		total += heldExposure(player.getOffhandItem());

		return Math.min(total, MAX_EXPOSURE_PER_SECOND);
	}

	private static int heldExposure(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}

		Item item = stack.getItem();

		if (item == ModItems.URANIUM_INGOT || item == ModItems.RAW_URANIUM) {
			return HELD_MATERIAL_EXPOSURE;
		}

		return ModTools.isUraniumTool(item) ? HELD_TOOL_EXPOSURE : 0;
	}

	/**
	 * 玩家总共能屏蔽多少比例（0.0 ~ 1.0）。
	 *
	 * <p><b>只看装备栏，不算背包。</b> 攒在背包里的铅锭 / 铅块不再提供任何屏蔽 ——
	 * 否则塞满铅锭就白嫖满屏蔽，护甲的减速代价等于不存在。
	 */
	private static double shielding(Player player) {
		return armorShielding(player);
	}

	/**
	 * 装备栏里的护甲提供多少屏蔽。
	 *
	 * <p>做法是**读物品自带的抗辐射修饰符**，而不是判断"是不是铅甲"：
	 * 那个修饰符就是物品提示里显示的那一条，读它等于让「提示」和「效果」
	 * 共用同一个数字，不会再出现"提示写 25%、实际算 20%"这种事。
	 *
	 * <p>修饰符的值是**负数**（-0.25 = 少 25% 穿透），所以取相反数累加。
	 */
	private static double armorShielding(Player player) {
		double shield = 0;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack stack = player.getItemBySlot(slot);

			if (stack.isEmpty()) {
				continue;
			}

			ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

			if (modifiers == null) {
				continue;
			}

			// lambda 里改不了局部变量，用长度为 1 的数组当累加器
			double[] sum = {0.0D};
			modifiers.forEach(slot, (attribute, modifier) -> {
				if (attribute.value() == ModAttributes.RADIATION_SHIELDING.value()) {
					sum[0] += modifier.amount();
				}
			});

			shield -= sum[0];
		}

		return Math.min(1.0, shield);
	}

	private static void applyEffects(ServerPlayer player, int dose) {
		int target = dose >= THRESHOLD_SEVERE ? 2
				: dose >= THRESHOLD_MODERATE ? 1
				: dose >= THRESHOLD_MILD ? 0
				: -1;

		if (target < 0) {
			// 剂量已经降到阈值以下，让效果自己过期就好
			return;
		}

		MobEffectInstance current = player.getEffect(ModEffects.RADIATION);
		int previous = current == null ? -1 : current.getAmplifier();

		if (previous > target) {
			// 原版 addEffect 只会往上叠，降档得先摘掉
			player.removeEffect(ModEffects.RADIATION);
		}

		// 第 5 个参数 visible：必须为 true，否则生物身上不会冒任何粒子
		player.addEffect(new MobEffectInstance(ModEffects.RADIATION, EFFECT_DURATION, target, false, true, true));

		if (target >= 2) {
			player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, EFFECT_DURATION, 0, false, false, true));
		}

		if (previous != target) {
			announce(player, target);
		}
	}

	private static void announce(ServerPlayer player, int amplifier) {
		// 26.3 里动作栏提示叫 sendOverlayMessage（旧名 displayClientMessage 已不在）
		player.sendOverlayMessage(
				Component.translatable("message.moreores.radiation." + (amplifier + 1))
						.withStyle(ChatFormatting.GREEN));

		player.level().playSound(null, player.blockPosition(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 0.6F);
	}
}
