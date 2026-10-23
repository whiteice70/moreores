package com.example.moreores.recipe;

import com.example.moreores.MoreOres;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * 注册模组的配方类型与序列化器。
 *
 * <p>两样缺一不可：
 * <ul>
 *   <li><b>配方类型</b>（{@code moreores:high_energy_smelting}）——
 *       决定配方被分到哪个桶，见 {@link ModRecipeTypes}</li>
 *   <li><b>序列化器</b> —— 决定 JSON 里 {@code "type": "moreores:high_energy_smelting"}
 *       由谁来解析</li>
 * </ul>
 *
 * <p>必须在数据包加载<b>之前</b>完成，所以挂在 {@code MoreOres.onInitialize()} 里。
 */
public final class ModRecipes {
	/** 配方类型与序列化器共用的 id。 */
	private static final String HIGH_ENERGY_SMELTING = "high_energy_smelting";

	private ModRecipes() {
	}

	public static void register() {
		// 先触发 ModRecipeTypes 的静态初始化把类型注册进 BuiltInRegistries.RECIPE_TYPE
		ModRecipeTypes.register();

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
				MoreOres.id(HIGH_ENERGY_SMELTING),
				HighEnergySmeltingRecipe.SERIALIZER);

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
				MoreOres.id("bio_catalyzing"),
				BioCatalyzingRecipe.SERIALIZER);

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
				MoreOres.id("mineral_catalyzing"),
				MineralCatalyzingRecipe.SERIALIZER);
	}
}
