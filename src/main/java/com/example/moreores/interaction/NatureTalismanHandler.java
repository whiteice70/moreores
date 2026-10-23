package com.example.moreores.interaction;

import java.util.List;
import java.util.Set;

import com.example.moreores.ModItems;
import com.example.moreores.crop.ModCrops;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * 自然护符的效果：破坏草时，把「小麦种子」**替换**成随机一种矿物种子。
 *
 * <p>之所以挂在掉落表事件上，而不是 {@code PlayerBlockBreakEvents} ——
 * 后者只能在破坏<b>之后</b>拿到结果，那时候小麦种子已经作为物品实体掉出来了，
 * 想"换成别的"就得先去把它删掉再补一个，又脏又容易和别的模组打架。
 * {@code LootTableEvents#MODIFY_DROPS} 在掉落生成<b>时</b>介入，
 * 直接改列表内容，最终掉出来的就是换过的版本。
 *
 * <p>「转而掉落」的语义：原来那一格小麦种子被替换，不会两个一起掉。
 *
 * <p>效果<b>不叠加</b>：只判断身上有没有，不看数量。
 */
public final class NatureTalismanHandler {
	/** 触发概率。 */
	private static final float CHANCE = 0.20F;

	/** 草的掉落表 —— 只有它们会掉小麦种子。 */
	private static final Set<Identifier> GRASS_TABLES = Set.of(
			Identifier.withDefaultNamespace("blocks/short_grass"),
			Identifier.withDefaultNamespace("blocks/tall_grass"),
			Identifier.withDefaultNamespace("blocks/fern"),
			Identifier.withDefaultNamespace("blocks/large_fern"));

	private NatureTalismanHandler() {
	}

	public static void register() {
		LootTableEvents.MODIFY_DROPS.register(NatureTalismanHandler::onDrops);
	}

	private static void onDrops(Holder<LootTable> table, LootContext context,
			List<ItemStack> drops) {
		// ① 只处理草。unwrapKey() 拿不到 id 的一律跳过
		//    ⚠️ 26.3 把 ResourceKey#location() 改名成了 identifier()
		if (table.unwrapKey().map(key -> !GRASS_TABLES.contains(key.identifier())).orElse(true)) {
			return;
		}

		// ② 必须是玩家破坏的 —— 后面要判断"谁身上带着护符"
		Entity entity = context.getOptional(LootContextParams.THIS_ENTITY);

		if (!(entity instanceof Player player)) {
			return;
		}

		// ③ 身上有没有护符（只数有无，不数个数 → 不叠加）
		if (!hasTalisman(player)) {
			return;
		}

		// ④ 这一轮掉落里得真有小麦种子
		if (drops.stream().noneMatch(stack -> stack.is(Items.WHEAT_SEEDS))) {
			return;
		}

		// ⑤ 掷 20%
		if (context.getRandom().nextFloat() >= CHANCE) {
			return;
		}

		// ⑥ 换成随机一种矿物种子（ModCrops 手里就是全部 12 种）
		List<Item> seeds = ModCrops.seeds();

		if (seeds.isEmpty()) {
			return;
		}

		Item replacement = seeds.get(context.getRandom().nextInt(seeds.size()));

		for (int i = 0; i < drops.size(); i++) {
			ItemStack stack = drops.get(i);

			if (stack.is(Items.WHEAT_SEEDS)) {
				drops.set(i, new ItemStack(replacement, stack.getCount()));
			}
		}
	}

	/** 背包 + 副手，任一带一个就算。 */
	private static boolean hasTalisman(Player player) {
		Inventory inventory = player.getInventory();

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(ModItems.NATURE_TALISMAN)) {
				return true;
			}
		}

		return player.getOffhandItem().is(ModItems.NATURE_TALISMAN);
	}
}
