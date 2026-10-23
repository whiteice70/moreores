package com.example.moreores;

import net.minecraft.world.food.FoodProperties;

public final class ModFoods {
	/**
	 * 含铅饼干：饱食度对标原版曲奇（nutrition 2 / saturationModifier 0.1 → 0.4 饱和度）。
	 *
	 * <p>{@code alwaysEdible()} 让它就算饥饿值满了也能继续吃 —— 当解毒药用。
	 */
	public static final FoodProperties LEAD_COOKIE = new FoodProperties.Builder()
			.nutrition(2)
			.saturationModifier(0.1F)
			.alwaysEdible()
			.build();

	private ModFoods() {
	}
}
