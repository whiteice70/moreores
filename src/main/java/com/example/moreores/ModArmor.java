package com.example.moreores;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * 铅制护甲四件套（数值对齐铁甲）与钛制护甲四件套（铁数值 + 铜耐久）。
 *
 * <p>两套的差别除了耐久，还有**移速**：钛甲每件 +5%，铅甲每件 -5%，
 * 四件齐了分别正好 ±20%。移速是直接写在物品的 {@code ATTRIBUTE_MODIFIERS}
 * 组件里的（见 {@link #register}），所以穿上就生效、脱下就恢复，不需要额外的 tick 逻辑。
 *
 * <p>⚠️ 运算方式必须用 {@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE}
 * 而不是 {@code ADD_MULTIPLIED_TOTAL} —— 见 {@link #register} 里的说明。
 */
public final class ModArmor {
	/** 钛甲轻：每件 +5% 移速，四件齐了 +20%。 */
	private static final double SPEED_PER_TITANIUM_PIECE = 0.05D;

	/** 铅甲重：每件 -5% 移速，四件齐了 -20%。 */
	private static final double SPEED_PER_LEAD_PIECE = -0.05D;

	/** 天空合金甲：每件 +10% 移速，四件齐了 +40%。 */
	private static final double SPEED_PER_SKY_ALLOY_PIECE = 0.10D;

	/**
	 * 铅甲挡辐射：每件 -25% 的**辐射穿透率**。
	 *
	 * <p>它和上面那条移速一样，是挂在物品上的**属性修饰符** ——
	 * 所以物品提示里「穿在身上时：」那一栏会自己多出一行
	 * <b>{@code -25% 辐射}</b>，不用手写 lore。
	 */
	private static final double RADIATION_PER_LEAD_PIECE = -0.25D;

	// ---------------- 铅 ----------------
	public static final Item LEAD_HELMET = register("lead_helmet",
			ModArmorMaterials.LEAD, ArmorType.HELMET,
			SPEED_PER_LEAD_PIECE, RADIATION_PER_LEAD_PIECE);
	public static final Item LEAD_CHESTPLATE = register("lead_chestplate",
			ModArmorMaterials.LEAD, ArmorType.CHESTPLATE,
			SPEED_PER_LEAD_PIECE, RADIATION_PER_LEAD_PIECE);
	public static final Item LEAD_LEGGINGS = register("lead_leggings",
			ModArmorMaterials.LEAD, ArmorType.LEGGINGS,
			SPEED_PER_LEAD_PIECE, RADIATION_PER_LEAD_PIECE);
	public static final Item LEAD_BOOTS = register("lead_boots",
			ModArmorMaterials.LEAD, ArmorType.BOOTS,
			SPEED_PER_LEAD_PIECE, RADIATION_PER_LEAD_PIECE);

	// ---------------- 钛 ----------------
	// 钛甲不抗辐射，所以最后一个参数传 0（= 不挂这条修饰符）
	public static final Item TITANIUM_HELMET = register("titanium_helmet",
			ModArmorMaterials.TITANIUM, ArmorType.HELMET, SPEED_PER_TITANIUM_PIECE, 0.0D);
	public static final Item TITANIUM_CHESTPLATE = register("titanium_chestplate",
			ModArmorMaterials.TITANIUM, ArmorType.CHESTPLATE, SPEED_PER_TITANIUM_PIECE, 0.0D);
	public static final Item TITANIUM_LEGGINGS = register("titanium_leggings",
			ModArmorMaterials.TITANIUM, ArmorType.LEGGINGS, SPEED_PER_TITANIUM_PIECE, 0.0D);
	public static final Item TITANIUM_BOOTS = register("titanium_boots",
			ModArmorMaterials.TITANIUM, ArmorType.BOOTS, SPEED_PER_TITANIUM_PIECE, 0.0D);

	// ---------------- 天空合金 ----------------
	// 数值对齐下界合金套，每件附加 +10% 移速；全套飞行由 SkyAlloyFlightHandler 负责。
	public static final Item SKY_ALLOY_HELMET = register("sky_alloy_helmet",
			ModArmorMaterials.SKY_ALLOY, ArmorType.HELMET, SPEED_PER_SKY_ALLOY_PIECE, 0.0D);
	public static final Item SKY_ALLOY_CHESTPLATE = register("sky_alloy_chestplate",
			ModArmorMaterials.SKY_ALLOY, ArmorType.CHESTPLATE, SPEED_PER_SKY_ALLOY_PIECE, 0.0D);
	public static final Item SKY_ALLOY_LEGGINGS = register("sky_alloy_leggings",
			ModArmorMaterials.SKY_ALLOY, ArmorType.LEGGINGS, SPEED_PER_SKY_ALLOY_PIECE, 0.0D);
	public static final Item SKY_ALLOY_BOOTS = register("sky_alloy_boots",
			ModArmorMaterials.SKY_ALLOY, ArmorType.BOOTS, SPEED_PER_SKY_ALLOY_PIECE, 0.0D);

	private ModArmor() {
	}

	/** 触发本类的静态初始化，完成全部护甲注册。 */
	public static void register() {
	}

	/**
	 * 注册一件护甲，并把「移速影响」并进材质自带的护甲属性里。
	 *
	 * <p>⚠️ 这里必须用 {@code humanoidArmor(...)} <b>之后再</b>整体写一次
	 * {@code ATTRIBUTE_MODIFIERS} —— 因为 {@code humanoidArmor} 内部就是把
	 * {@code ArmorMaterial.createAttributes()} 写进这个组件的。要是在它之前写，
	 * 会被它覆盖掉；要是不带材质的属性直接写，护甲值（{@code Attributes.ARMOR}）
	 * 会被一起抹掉，穿上就成了纸糊的。所以这里是在**材质给的基础上追加**。
	 *
	 * <p>⚠️ 修饰符 ID 每件必须不同：属性表是按 ID 存的，重名会互相覆盖，
	 * 表现为「穿了四件却只生效一件」。
	 *
	 * <p>⚠️ 运算方式必须是 {@code ADD_MULTIPLIED_BASE}，<b>不能用
	 * {@code ADD_MULTIPLIED_TOTAL}</b>。属性结算的顺序是：
	 * <pre>
	 *   ADD_VALUE             逐个相加
	 *   ADD_MULTIPLIED_BASE   逐个加上「基础值 × 系数」   ← 多个修饰符是**相加**的
	 *   ADD_MULTIPLIED_TOTAL  逐个把总值乘以 (1 + 系数)   ← 多个修饰符是**叠乘**的
	 * </pre>
	 * 四件护甲各带一个 {@code ADD_MULTIPLIED_TOTAL(0.05)}，乘出来是
	 * {@code 1.05⁴ = 1.2155}，也就是 <b>+21.55%</b> 而不是 +20%（实测确认）——
	 * 铅甲那边同理只有 -18.55%。
	 *
	 * <p>换成 {@code ADD_MULTIPLIED_BASE(0.05)} 后，四件各加「基础值 × 0.05」，
	 * 加起来正好是基础值的 1.20 倍 = <b>+20%</b> ✓。
	 *
	 * @param radiationPerPiece 抗辐射系数，<b>不抗辐射就传 {@code 0}</b>
	 *                          （0 表示这条修饰符干脆不挂，避免提示里多一行 "+0% 辐射"）
	 */
	private static Item register(String name, ArmorMaterial material,
			ArmorType type, double speedPerPiece, double radiationPerPiece) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(type.getSlot());

		ItemAttributeModifiers attributes = material.createAttributes(type)
				.withModifierAdded(
						Attributes.MOVEMENT_SPEED,
						new AttributeModifier(MoreOres.id(name + "_speed"), speedPerPiece,
								AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						group);

		if (radiationPerPiece != 0.0D) {
			// 抗辐射走和移速**同一种运算方式** —— 这是关键：
			// 提示里带不带 % 取决于运算方式（见 ModAttributes 的说明），
			// 用 ADD_MULTIPLIED_BASE 出来就是「-25% 辐射」，
			// 用 ADD_VALUE 会变成「-0.25 辐射」。
			attributes = attributes.withModifierAdded(
					ModAttributes.RADIATION_SHIELDING,
					new AttributeModifier(MoreOres.id(name + "_radiation"), radiationPerPiece,
							AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
					group);
		}

		Item.Properties properties = new Item.Properties()
				.humanoidArmor(material, type)
				.attributes(attributes)
				.setId(key);

		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
	}
}
