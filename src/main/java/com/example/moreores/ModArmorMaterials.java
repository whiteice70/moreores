package com.example.moreores;

import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * 铅质与钛质护甲材质。
 *
 * <p>26.3 的护甲已经组件化：材质是 {@link ArmorMaterial} record，具体部件用
 * {@code Item.Properties.humanoidArmor(材质, ArmorType)} 设置。
 *
 * <p>原版参照值（反汇编 {@code ArmorMaterials} 得到）：
 * <pre>
 *   铁：耐久倍率 15 / 附魔度 9
 *   铜：耐久倍率 11 / 附魔度 8
 *   两套的护甲值都是 靴 2、腿 5、胸 6、头 2 / 韧性 0 / 击退抗性 0
 * </pre>
 *
 * <p>铅甲完全对齐铁那套数值。
 *
 * <p>钛甲沿用工具的设定 —— <b>铁的护甲值 + 铜的耐久</b>：护甲值 / 附魔度跟铁一致，
 * 只有耐久倍率换成铜的 <b>11</b>（铁是 15），和工具那边「190 而不是 250」是同一个思路。
 *
 * <p>最后那个 {@code assetId} 是 1.21.2 之后新增的「穿戴外观」资源键，
 * 对应 {@code assets/moreores/equipment/<名字>.json}。
 */
public final class ModArmorMaterials {
	public static final ResourceKey<EquipmentAsset> LEAD_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MoreOres.id("lead"));

	public static final ResourceKey<EquipmentAsset> TITANIUM_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MoreOres.id("titanium"));

	public static final ResourceKey<EquipmentAsset> SKY_ALLOY_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MoreOres.id("sky_alloy"));

	/** 修理材料：内容写在 data/moreores/tags/item/lead_armor_repair.json。 */
	private static final TagKey<Item> REPAIRS_LEAD_ARMOR =
			TagKey.create(Registries.ITEM, MoreOres.id("lead_armor_repair"));

	/** 修理材料：内容写在 data/moreores/tags/item/titanium_armor_repair.json。 */
	private static final TagKey<Item> REPAIRS_TITANIUM_ARMOR =
			TagKey.create(Registries.ITEM, MoreOres.id("titanium_armor_repair"));

	/** 修理材料：内容写在 data/moreores/tags/item/sky_alloy_armor_repair.json。 */
	private static final TagKey<Item> REPAIRS_SKY_ALLOY_ARMOR =
			TagKey.create(Registries.ITEM, MoreOres.id("sky_alloy_armor_repair"));

	public static final ArmorMaterial LEAD = new ArmorMaterial(
			15,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2),
			9,
			SoundEvents.ARMOR_EQUIP_IRON,
			0.0F,
			0.0F,
			REPAIRS_LEAD_ARMOR,
			LEAD_ASSET);

	public static final ArmorMaterial TITANIUM = new ArmorMaterial(
			11,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2),
			9,
			SoundEvents.ARMOR_EQUIP_IRON,
			0.0F,
			0.0F,
			REPAIRS_TITANIUM_ARMOR,
			TITANIUM_ASSET);

	/**
	 * 天空合金甲：数值完全对齐下界合金套。
	 *
	 * <pre>
	 *   耐久倍率 37 / 附魔度 15
	 *   护甲值 靴 3、腿 6、胸 8、头 3
	 *   韧性 3.0 / 击退抗性 0.1
	 * </pre>
	 *
	 * <p>额外效果不写在材质里：每件 +10% 移速由 {@link ModArmor} 追加，
	 * 全套飞行由 {@code SkyAlloyFlightHandler} 负责。
	 */
	public static final ArmorMaterial SKY_ALLOY = new ArmorMaterial(
			37,
			Map.of(
					ArmorType.BOOTS, 3,
					ArmorType.LEGGINGS, 6,
					ArmorType.CHESTPLATE, 8,
					ArmorType.HELMET, 3),
			15,
			SoundEvents.ARMOR_EQUIP_NETHERITE,
			3.0F,
			0.1F,
			REPAIRS_SKY_ALLOY_ARMOR,
			SKY_ALLOY_ASSET);

	private ModArmorMaterials() {
	}
}
