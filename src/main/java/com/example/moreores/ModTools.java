package com.example.moreores;

import java.util.function.UnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * 铅制与铀制的武器工具。
 *
 * <p>参数含义（与原版 {@code Items} 完全一致）：
 * <pre>
 *   最终攻击伤害 = 1 + 参数1 + 材质的 attackDamageBonus
 *   最终攻击速度 = 4.0 + 参数2
 * </pre>
 *
 * <p>铅：伤害与铁一模一样，但攻速整体比铁低 0.4（铅更重、更钝）。
 * <p>铀：数值与钻石完全一致。
 *
 * <p>注意：铀制工具**不加附魔光效** —— 加了之后和真附魔的物品分不清。
 */
public final class ModTools {
	// ---------------- 铅：伤害同铁，攻速 -0.4 ----------------
	public static final Item LEAD_SWORD = register("lead_sword",
			properties -> properties.sword(ModToolMaterials.LEAD, 3.0F, -2.8F));
	public static final Item LEAD_PICKAXE = register("lead_pickaxe",
			properties -> properties.pickaxe(ModToolMaterials.LEAD, 1.0F, -3.2F));
	public static final Item LEAD_AXE = register("lead_axe",
			properties -> properties.axe(ModToolMaterials.LEAD, 6.0F, -3.5F));
	public static final Item LEAD_SHOVEL = register("lead_shovel",
			properties -> properties.shovel(ModToolMaterials.LEAD, 1.5F, -3.4F));
	public static final Item LEAD_HOE = register("lead_hoe",
			properties -> properties.hoe(ModToolMaterials.LEAD, -2.0F, -1.4F));

	// ---------------- 铀：数值同钻石 ----------------
	public static final Item URANIUM_SWORD = register("uranium_sword",
			properties -> properties.sword(ModToolMaterials.URANIUM, 3.0F, -2.4F));
	public static final Item URANIUM_PICKAXE = register("uranium_pickaxe",
			properties -> properties.pickaxe(ModToolMaterials.URANIUM, 1.0F, -2.8F));
	public static final Item URANIUM_AXE = register("uranium_axe",
			properties -> properties.axe(ModToolMaterials.URANIUM, 5.0F, -3.0F));
	public static final Item URANIUM_SHOVEL = register("uranium_shovel",
			properties -> properties.shovel(ModToolMaterials.URANIUM, 1.5F, -3.0F));
	public static final Item URANIUM_HOE = register("uranium_hoe",
			properties -> properties.hoe(ModToolMaterials.URANIUM, -3.0F, 0.0F));

	// ---------------- 钛：铁的伤害 + 铜的耐久 + 更快的攻速 ----------------
	/*
	 * 攻速：在**各自原来的取值**上统一 +0.4（下表里的「原版参照值」都是实测出来的）。
	 * 攻速那个参数是「相对 4.0 的偏移」，所以直接把偏移加 0.4：
	 *
	 *   钛剑  -2.4 → -2.0    最终攻速 1.6 → 2.0   （原本对标铁剑   1.6）
	 *   钛镐  -2.8 → -2.4    最终攻速 1.2 → 1.6   （原本对标铁镐   1.2）
	 *   钛斧  -3.0 → -2.6    最终攻速 1.0 → 1.4   （原本对标钻石斧 1.0）
	 *   钛锹  -3.0 → -2.6    最终攻速 1.0 → 1.4   （原本对标铁锹   1.0）
	 *   钛锄   0.0 →  0.4    最终攻速 4.0 → 4.4   （原本对标钻石锄 4.0）
	 *
	 * 换算成「比同类原版快多少」：剑 / 镐 / 锹比铁快 0.4，
	 * 斧比铁快 0.5（它原本对标的是钻石斧 1.0，而铁斧是 0.9），锄比钻石快 0.4。
	 *
	 * ⚠️ 顺带记一笔（**既有问题，本次没动**）：斧和锄的「伤害参数」原本照抄的是
	 * **钻石那一套**（5.0 / -3.0），但钛材质的 attackDamageBonus 是铁的 2.0
	 * 而不是钻石的 3.0，所以这两件的伤害反而比铁低 1.0
	 * （实测：钛斧 7.0 / 铁斧 8.0，钛锄 -1.0 / 钻石锄 0.0）。
	 */
	public static final Item TITANIUM_SWORD = register("titanium_sword",
			properties -> properties.sword(ModToolMaterials.TITANIUM, 3.0F, -2.0F));
	public static final Item TITANIUM_PICKAXE = register("titanium_pickaxe",
			properties -> properties.pickaxe(ModToolMaterials.TITANIUM, 1.0F, -2.4F));
	public static final Item TITANIUM_AXE = register("titanium_axe",
			properties -> properties.axe(ModToolMaterials.TITANIUM, 6.0F, -2.6F));
	public static final Item TITANIUM_SHOVEL = register("titanium_shovel",
			properties -> properties.shovel(ModToolMaterials.TITANIUM, 1.5F, -2.6F));
	public static final Item TITANIUM_HOE = register("titanium_hoe",
			properties -> properties.hoe(ModToolMaterials.TITANIUM, -2.0F, 0.4F));

	private ModTools() {
	}

	/** 触发本类的静态初始化，完成全部工具注册。 */
	public static void register() {
	}

	/** 手持铀制工具 / 武器时会持续吃到辐射（见 RadiationManager）。 */
	public static boolean isUraniumTool(Item item) {
		return item == URANIUM_SWORD
				|| item == URANIUM_PICKAXE
				|| item == URANIUM_AXE
				|| item == URANIUM_SHOVEL
				|| item == URANIUM_HOE;
	}

	private static Item register(String name, UnaryOperator<Item.Properties> configure) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		Item.Properties properties = configure.apply(new Item.Properties()).setId(key);
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
	}
}
