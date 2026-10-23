package com.example.moreores.world;

import com.example.moreores.MoreOres;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * 把模组自己的 placed_feature 注入到主世界生物群系。
 *
 * <p>26.3 已经没有 ConfiguredFeature 中间层：矿石的形态写在
 * {@code data/moreores/worldgen/feature/*.json}，分布规则写在
 * {@code data/moreores/worldgen/placed_feature/*.json}，这里只负责把
 * placed_feature 挂到生物群系上。
 */
public final class ModWorldGen {
	private ModWorldGen() {
	}

	public static void register() {
		// 铅矿照抄原版煤矿的两段分布（低段埋藏型、高段普通型）
		addOverworldFeature("lead_ore_lower");
		addOverworldFeature("lead_ore_upper");
		addOverworldFeature("uranium_ore");

		// 钛：完全照抄原版煤矿的两段分布（上段普通型 / 下段埋藏型）
		addOverworldFeature("titanium_ore_upper");
		addOverworldFeature("titanium_ore_lower");
	}

	private static void addOverworldFeature(String name) {
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES,
				ResourceKey.create(Registries.PLACED_FEATURE, MoreOres.id(name)));
	}
}
