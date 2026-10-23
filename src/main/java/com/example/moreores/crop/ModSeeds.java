package com.example.moreores.crop;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.moreores.MoreOres;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * 12 种矿物种子。
 *
 * <p>种植系统在 {@link ModCrops} 里 —— 那边把「种子 → 作物方块」配好，
 * 这里只负责注册物品本身。
 *
 * <p>⚠️ 注册顺序有讲究：{@link ModCrops} 的静态字段会引用这里的种子，
 * 所以 {@code ModSeeds} 必须先完成初始化（{@code MoreOres#onInitialize} 里
 * 把它排在 {@code ModCrops} 之前，其实靠类加载也能自动触发，但写明白更稳）。
 *
 * <p>种子是**普通 {@code Item}**，不是原版的 {@code ItemNameBlockItem} ——
 * 因为种植走的是 {@code UseBlockCallback}（右键耕地），不需要物品自带放置逻辑。
 */
public final class ModSeeds {
	/** 所有种子，按注册顺序排列，方便统计和遍历。 */
	private static final Map<String, Item> ALL = new LinkedHashMap<>();

	// ---------------- 本模组的矿物 ----------------
	public static final Item LEAD_SEEDS = define("lead_seeds");
	public static final Item URANIUM_SEEDS = define("uranium_seeds");
	public static final Item TITANIUM_SEEDS = define("titanium_seeds");

	// ---------------- 原版金属 ----------------
	public static final Item IRON_SEEDS = define("iron_seeds");
	public static final Item GOLD_SEEDS = define("gold_seeds");
	public static final Item COPPER_SEEDS = define("copper_seeds");

	// ---------------- 原版宝石 / 矿物 ----------------
	public static final Item DIAMOND_SEEDS = define("diamond_seeds");
	public static final Item EMERALD_SEEDS = define("emerald_seeds");
	public static final Item LAPIS_SEEDS = define("lapis_seeds");
	public static final Item REDSTONE_SEEDS = define("redstone_seeds");
	public static final Item COAL_SEEDS = define("coal_seeds");
	public static final Item QUARTZ_SEEDS = define("quartz_seeds");

	// 下界合金作物已移除：它成熟后掉的只是下界合金碎片（netherite_scrap），
	// 产业链太短，和「最贵的一种作物」的名不副实，不如删掉。

	private ModSeeds() {
	}

	/** 触发静态初始化完成全部种子注册。 */
	public static void register() {
	}

	/** 已注册的种子数量，调试用。 */
	public static int count() {
		return ALL.size();
	}

	private static Item define(String name) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key,
				new Item(new Item.Properties().setId(key)));
		ALL.put(name, item);
		return item;
	}
}
