package com.example.moreores;

import java.util.function.Function;

import com.example.moreores.item.RangedArrowItem;
import com.example.moreores.item.RangedBowItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * 铅制与铀制的远程武器。
 *
 * <p>数值基准是原版弓（耐久 384 / 射程 15）和原版箭（基础伤害 2.0）：
 * <ul>
 *   <li>铅弓 250 耐久 / 射程 15</li>
 *   <li>铅箭基础伤害 2.5，铀箭 3.0（铀箭命中还会挂辐射，见 RadiationManager）</li>
 * </ul>
 */
public final class ModRanged {
	/** 原版弓的射程，用作基准。 */
	private static final int VANILLA_RANGE = 15;

	// ---------------- 弓 ----------------
	public static final Item LEAD_BOW = register("lead_bow", 1,
			properties -> new RangedBowItem(properties, VANILLA_RANGE),
			new Item.Properties().durability(250).enchantable(1));

	// 铀弓已删除：铀的定位是"辐射源"而不是远程武器，铀箭保留（任何弓都能射）。

	// ---------------- 箭 ----------------
	public static final Item LEAD_ARROW = register("lead_arrow", 64,
			properties -> new RangedArrowItem(properties, 2.5D),
			new Item.Properties());

	public static final Item URANIUM_ARROW = register("uranium_arrow", 64,
			properties -> new RangedArrowItem(properties, 3.0D),
			new Item.Properties());

	private ModRanged() {
	}

	/** 触发本类的静态初始化，完成全部远程武器注册。 */
	public static void register() {
	}

	private static Item register(String name, int stackSize, Function<Item.Properties, Item> factory,
			Item.Properties settings) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		Item.Properties properties = settings.stacksTo(stackSize).setId(key);
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties));
	}
}
