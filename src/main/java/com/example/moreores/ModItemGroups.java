package com.example.moreores;

import com.example.moreores.crop.ModSeeds;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** 一个属于本模组的创造模式物品栏页签。 */
public final class ModItemGroups {
	public static final ResourceKey<CreativeModeTab> MORE_ORES =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, MoreOres.id("more_ores"));

	private ModItemGroups() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MORE_ORES,
				CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
						.title(Component.translatable("itemGroup.moreores.more_ores"))
						.icon(() -> new ItemStack(ModItems.URANIUM_INGOT))
						.displayItems((parameters, output) -> {
							output.accept(ModItems.RAW_LEAD);
							output.accept(ModItems.LEAD_INGOT);
							output.accept(ModItems.LEAD_NUGGET);
							output.accept(ModBlocks.RAW_LEAD_BLOCK);
							output.accept(ModBlocks.LEAD_BLOCK);
							output.accept(ModBlocks.LEAD_ORE);
							output.accept(ModBlocks.DEEPSLATE_LEAD_ORE);
							output.accept(ModTools.LEAD_SWORD);
							output.accept(ModTools.LEAD_PICKAXE);
							output.accept(ModTools.LEAD_AXE);
							output.accept(ModTools.LEAD_SHOVEL);
							output.accept(ModTools.LEAD_HOE);
							output.accept(ModRanged.LEAD_BOW);
							output.accept(ModRanged.LEAD_ARROW);
							output.accept(ModArmor.LEAD_HELMET);
							output.accept(ModArmor.LEAD_CHESTPLATE);
							output.accept(ModArmor.LEAD_LEGGINGS);
							output.accept(ModArmor.LEAD_BOOTS);

							output.accept(ModItems.RAW_URANIUM);
							output.accept(ModItems.URANIUM_INGOT);
							output.accept(ModItems.URANIUM_NUGGET);
							output.accept(ModBlocks.RAW_URANIUM_BLOCK);
							output.accept(ModBlocks.URANIUM_BLOCK);
							output.accept(ModBlocks.URANIUM_ORE);
							output.accept(ModBlocks.DEEPSLATE_URANIUM_ORE);
							output.accept(ModTools.URANIUM_SWORD);
							output.accept(ModTools.URANIUM_PICKAXE);
							output.accept(ModTools.URANIUM_AXE);
							output.accept(ModTools.URANIUM_SHOVEL);
							output.accept(ModTools.URANIUM_HOE);
							// 铀弓已删除
							output.accept(ModRanged.URANIUM_ARROW);

							// ---------------- 钛 ----------------
							output.accept(ModItems.RAW_TITANIUM);
							output.accept(ModItems.TITANIUM_INGOT);
							output.accept(ModItems.TITANIUM_NUGGET);
							output.accept(ModItems.TITANIUM_ORE_FRAGMENT);
							output.accept(ModItems.SKY_ALLOY_INGOT);
							output.accept(ModItems.NETHERITE_BLANK);
							output.accept(ModItems.DIAMOND_SHARD);
							output.accept(ModItems.EMERALD_SHARD);
							output.accept(ModItems.LAPIS_SHARD);
							output.accept(ModItems.QUARTZ_SHARD);
							output.accept(ModBlocks.RAW_TITANIUM_BLOCK);
							output.accept(ModBlocks.TITANIUM_BLOCK);
							output.accept(ModBlocks.TITANIUM_ORE);
							output.accept(ModBlocks.DEEPSLATE_TITANIUM_ORE);
							output.accept(ModTools.TITANIUM_SWORD);
							output.accept(ModTools.TITANIUM_PICKAXE);
							output.accept(ModTools.TITANIUM_AXE);
							output.accept(ModTools.TITANIUM_SHOVEL);
							output.accept(ModTools.TITANIUM_HOE);
							output.accept(ModArmor.TITANIUM_HELMET);
							output.accept(ModArmor.TITANIUM_CHESTPLATE);
							output.accept(ModArmor.TITANIUM_LEGGINGS);
							output.accept(ModArmor.TITANIUM_BOOTS);
							output.accept(ModArmor.SKY_ALLOY_HELMET);
							output.accept(ModArmor.SKY_ALLOY_CHESTPLATE);
							output.accept(ModArmor.SKY_ALLOY_LEGGINGS);
							output.accept(ModArmor.SKY_ALLOY_BOOTS);

							output.accept(ModBlocks.HIGH_ENERGY_FURNACE);
							output.accept(ModBlocks.BIO_CATALYZER);
							output.accept(ModBlocks.MINERAL_CATALYZER);
							output.accept(ModItems.SULFUR_POWDER);
							output.accept(ModItems.LEAD_COOKIE);
							output.accept(ModItems.MILK_BOTTLE);
							output.accept(ModItems.NATURE_TALISMAN);
							output.accept(ModItems.GEIGER_COUNTER);
							output.accept(ModItems.NUCLEAR_BATTERY);
							output.accept(ModItems.DEPLETED_NUCLEAR_BATTERY);

							// ---------------- 矿物种子 ----------------
							output.accept(ModSeeds.LEAD_SEEDS);
							output.accept(ModSeeds.URANIUM_SEEDS);
							output.accept(ModSeeds.TITANIUM_SEEDS);
							output.accept(ModSeeds.IRON_SEEDS);
							output.accept(ModSeeds.GOLD_SEEDS);
							output.accept(ModSeeds.COPPER_SEEDS);
							output.accept(ModSeeds.DIAMOND_SEEDS);
							output.accept(ModSeeds.EMERALD_SEEDS);
							output.accept(ModSeeds.LAPIS_SEEDS);
							output.accept(ModSeeds.REDSTONE_SEEDS);
							output.accept(ModSeeds.COAL_SEEDS);
							output.accept(ModSeeds.QUARTZ_SEEDS);
							// 下界合金作物 / 种子已移除
						})
						.build());
	}
}
