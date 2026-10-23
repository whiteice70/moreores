package com.example.moreores;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * 铅与铀的工具材质。
 *
 * <p>26.3 的工具属性已经完全组件化：材质是 {@link ToolMaterial} 这个 record，
 * 具体工具用 {@code Item.Properties.sword/pickaxe/axe/shovel/hoe} 设置。
 *
 * <p>原版参照值（反汇编 ToolMaterial 得到）：
 * <ul>
 *   <li>铁：250 耐久 / 6.0 挖掘速度 / +2.0 攻击 / 14 附魔度</li>
 *   <li>钻石：1561 耐久 / 8.0 挖掘速度 / +3.0 攻击 / 10 附魔度</li>
 * </ul>
 */
public final class ModToolMaterials {
	/** 修理材料：内容写在 data/moreores/tags/item/lead_tool_materials.json。 */
	private static final TagKey<Item> LEAD_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, MoreOres.id("lead_tool_materials"));

	private static final TagKey<Item> URANIUM_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, MoreOres.id("uranium_tool_materials"));

	private static final TagKey<Item> TITANIUM_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, MoreOres.id("titanium_tool_materials"));

	/** 铅：耐久 / 挖掘速度 / 攻击加成 / 附魔度全部对齐铁。 */
	public static final ToolMaterial LEAD = new ToolMaterial(
			BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, LEAD_TOOL_MATERIALS);

	/** 铀：全部对齐钻石。 */
	public static final ToolMaterial URANIUM = new ToolMaterial(
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10, URANIUM_TOOL_MATERIALS);

	/**
	 * 钛 = <b>铁的数值 + 铜的耐久</b>。
	 *
	 * <p>挖掘等级、速度、攻击加成、附魔度四项都跟铁一致
	 * （{@link BlockTags#INCORRECT_FOR_IRON_TOOL} / 6.0 / 2.0 / 14），
	 * 只有耐久换成铜的 <b>190</b>（铁是 250）—— 也就是「挖得快、砍得动，但比铁费」。
	 *
	 * <p>原版参照值（反编译 {@code ToolMaterial} 的静态块得到）：
	 * <pre>
	 *   铜：INCORRECT_FOR_COPPER_TOOL, 190, 5.0, 1.0, 13
	 *   铁：INCORRECT_FOR_IRON_TOOL,   250, 6.0, 2.0, 14
	 * </pre>
	 */
	public static final ToolMaterial TITANIUM = new ToolMaterial(
			BlockTags.INCORRECT_FOR_IRON_TOOL, 190, 6.0F, 2.0F, 14, TITANIUM_TOOL_MATERIALS);

	private ModToolMaterials() {
	}
}
