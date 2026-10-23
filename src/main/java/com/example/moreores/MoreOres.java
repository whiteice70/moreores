package com.example.moreores;

import com.example.moreores.crop.ModCrops;
import com.example.moreores.crop.ModSeeds;
import com.example.moreores.effect.ModEffects;
import com.example.moreores.interaction.MilkBottleHandler;
import com.example.moreores.interaction.NatureTalismanHandler;
import com.example.moreores.interaction.SkyAlloyFlightHandler;
import com.example.moreores.radiation.RadiationManager;
import com.example.moreores.recipe.ModRecipes;
import com.example.moreores.recipe.RecipeAutoUnlock;
import com.example.moreores.world.ModWorldGen;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreOres implements ModInitializer {
	public static final String MOD_ID = "moreores";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModBlockEntities.register();
		// 配方类型 / 序列化器要在数据包加载前注册好
		ModRecipes.register();
		// 种子要在作物之前 —— ModCrops 的静态字段会引用它们
		ModSeeds.register();
		ModCrops.register();
		ModItems.register();
		ModMenus.register();
		// 属性要在护甲之前 —— ModArmor 会给铅甲挂这个属性
		ModAttributes.register();
		ModTools.register();
		ModArmor.register();
		ModEntities.register();
		ModRanged.register();
		ModParticles.register();
		ModEffects.register();
		ModItemGroups.register();
		ModWorldGen.register();
		RadiationManager.register();
		MilkBottleHandler.register();
		NatureTalismanHandler.register();
		SkyAlloyFlightHandler.register();
		// 拿到配方里任意物品就自动解锁（见 RecipeAutoUnlock）
		RecipeAutoUnlock.register();

		LOGGER.info("More Ores (lead & uranium) initialized.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
