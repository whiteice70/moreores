package com.example.moreores.crop;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.example.moreores.ModItems;
import com.example.moreores.MoreOres;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;

/**
 * 矿物作物：手持**金属锭或宝石**右键耕地直接种下，和土豆一样长。
 *
 * <p>掉落逻辑**完全对齐原版马铃薯**（`loot_table/blocks/potatoes.json` 的结构）：
 * <ul>
 *   <li>池 1 —— 无条件，掉 **1 个种子物品本身**。所以中途铲掉是**原样返还**，不亏不赚</li>
 *   <li>池 2 —— 仅成熟（age=3）时结算，再掉 1 个产物，受时运附魔加成（最多再 +3）</li>
 * </ul>
 *
 * <p>也就是「种什么收什么」：种钻石收钻石，种铅锭收铅锭。
 * 目前共 12 种：模组的铅、铀、钛 + 原版的铁、金、铜、钻石、绿宝石、青金石、红石、煤炭、下界石英。
 *
 * <p>作物方块**没有对应的物品**（和小麦一样），只能靠种植产生。
 *
 * <p>铀作物还会持续向周围释放辐射，见
 * {@link com.example.moreores.radiation.RadiationManager#exposurePerSecond}。
 */
public final class ModCrops {
	/** 种子物品 → 作物方块。 */
	private static final Map<Item, Block> BY_SEED = new LinkedHashMap<>();

	/** 会发光的作物**未成熟**时的亮度。 */
	private static final int CROP_LIGHT_YOUNG = 3;

	/** 会发光的作物**成熟**时的亮度（作对比：萤石 15，火把 14）。 */
	private static final int CROP_LIGHT_MATURE = 7;

	/** 会发光的作物的公共属性：照亮环境 + 自身纹理不随环境光变暗。 */
	private static final UnaryOperator<BlockBehaviour.Properties> GLOWING = properties -> properties
			.lightLevel(state -> state.getValue(MineralCropBlock.AGE_4_STAGES)
					== BlockStateProperties.MAX_AGE_3
							? CROP_LIGHT_MATURE : CROP_LIGHT_YOUNG)
			.emissiveRendering(state -> true);

	public static final Block LEAD_CROP = define("lead_crop", ModSeeds.LEAD_SEEDS);

	/**
	 * 钛作物：两个特色，其他作物都没有。
	 *
	 * <ul>
	 *   <li><b>长得快</b> —— 用 {@link TitaniumCropBlock}，每次随机刻多掷一遍生长骰子。
	 *       对应钛在本模组里的定位「**轻**」（盔甲 +5% 移速、工具 +0.4 攻速）。</li>
	 *   <li><b>反光</b> —— {@code emissiveRendering} 让它的纹理**不随环境光变暗**，
	 *       暗处也银白发亮。对应「**亮**」。</li>
	 * </ul>
	 *
	 * <p>注意它**不照亮周围**（没设 {@code lightLevel}）—— 那是铀作物的活。
	 * 两者的区别正好落在这个模组的设定上：钛是「亮」，铀才是「辐射」。
	 */
	public static final Block TITANIUM_CROP = define("titanium_crop", ModSeeds.TITANIUM_SEEDS,
			properties -> properties.emissiveRendering(state -> true),
			TitaniumCropBlock::new);

	/**
	 * 铀作物：唯一会**真正照亮环境**的作物。
	 *
	 * <p>两处发光设置各管一半：
	 * <ul>
	 *   <li>{@code lightLevel} —— 向周围方块发光（像萤石那样把地面照亮）。
	 *       未成熟时给 3，成熟（age 3）给 7：能看出"长成了会更亮"。</li>
	 *   <li>{@code emissiveRendering} —— 让**它自己的纹理**不随环境光变暗，
	 *       深夜或矿洞里也是鲜绿的，不会糊成一团黑。</li>
	 * </ul>
	 *
	 * <p>注意：真正让周围亮起来的是 {@code lightLevel}，
	 * 这也是它和「辐射僵尸那套自发光」最大的区别 —— <b>方块能照亮环境，实体不能</b>。
	 */
	public static final Block URANIUM_CROP = define("uranium_crop", ModSeeds.URANIUM_SEEDS,
			GLOWING);

	// ---------------- 原版金属 ----------------
	public static final Block IRON_CROP = define("iron_crop", ModSeeds.IRON_SEEDS);
	public static final Block GOLD_CROP = define("gold_crop", ModSeeds.GOLD_SEEDS);
	public static final Block COPPER_CROP = define("copper_crop", ModSeeds.COPPER_SEEDS);

	// ---------------- 原版宝石 / 矿物 ----------------
	public static final Block DIAMOND_CROP = define("diamond_crop", ModSeeds.DIAMOND_SEEDS);
	public static final Block EMERALD_CROP = define("emerald_crop", ModSeeds.EMERALD_SEEDS);
	public static final Block LAPIS_CROP = define("lapis_crop", ModSeeds.LAPIS_SEEDS);

	/**
	 * 红石作物：会**发出红石的光** —— 照亮环境和自身发亮的处理与铀作物一致
	 * （未成熟 3、成熟 7 + 纹理不受环境光影响）。整株都是红石，
	 * 在矿洞里种一排当「活的红石火把」正合适。
	 */
	public static final Block REDSTONE_CROP = define("redstone_crop", ModSeeds.REDSTONE_SEEDS,
			GLOWING);

	public static final Block COAL_CROP = define("coal_crop", ModSeeds.COAL_SEEDS);
	public static final Block QUARTZ_CROP = define("quartz_crop", ModSeeds.QUARTZ_SEEDS);

	private ModCrops() {
	}

	/** 触发静态初始化完成方块注册，并挂上「右键耕地种植」的交互。 */
	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);
			Block crop = BY_SEED.get(stack.getItem());

			if (crop == null || stack.isEmpty()) {
				return InteractionResult.PASS;
			}

			// 只能种在耕地上，且上方那一格可以替换（空气）
			BlockPos soil = hitResult.getBlockPos();

			if (!level.getBlockState(soil).is(Blocks.FARMLAND)) {
				return InteractionResult.PASS;
			}

			BlockPos target = soil.above();

			if (!level.getBlockState(target).canBeReplaced()) {
				return InteractionResult.PASS;
			}

			// 客户端只负责挥手，真正放置交给服务端
			if (!level.isClientSide()) {
				level.setBlock(target, crop.defaultBlockState(), Block.UPDATE_ALL);
				level.playSound(null, target, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS,
						1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
				stack.consume(1, player);
			}

			return InteractionResult.SUCCESS;
		});
	}

	/** 铀作物（任意生长阶段）—— 辐射扫描用。 */
	public static boolean isUraniumCrop(BlockState state) {
		return state.is(URANIUM_CROP);
	}

	/** 目前注册了哪些「种子 → 作物」，调试用。 */
	public static List<Item> seeds() {
		return List.copyOf(BY_SEED.keySet());
	}

	private static Block define(String name, Item seed) {
		return define(name, seed, properties -> properties, MineralCropBlock::new);
	}

	/**
	 * @param extra 额外的方块属性（比如铀作物的发光）。
	 *              在公共属性之后应用，所以它可以覆盖默认值。
	 */
	private static Block define(String name, Item seed,
			UnaryOperator<BlockBehaviour.Properties> extra) {
		return define(name, seed, extra, MineralCropBlock::new);
	}

	/**
	 * @param factory 用哪个方块类。默认 {@link MineralCropBlock}，
	 *                钛作物换成 {@link TitaniumCropBlock}（长得更快）。
	 */
	private static Block define(String name, Item seed,
			UnaryOperator<BlockBehaviour.Properties> extra,
			BiFunction<BlockBehaviour.Properties, Item, Block> factory) {
		Block crop = register(name,
				properties -> factory.apply(extra.apply(properties), seed));
		BY_SEED.put(seed, crop);
		return crop;
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, MoreOres.id(name));

		// 注意 26.3 把它拼写修正成了 noCollision()（旧版一直是拼错的 noCollission）
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.noCollision()
				.randomTicks()
				.instabreak()
				.sound(SoundType.CROP)
				// 26.3 把 PushReaction.DESTROY 改名叫 POPPED 了（被活塞推会掉落）
				.pushReaction(PushReaction.POPPED)
				.setId(key);

		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties));
	}
}
