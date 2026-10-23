package com.example.moreores.recipe;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * 把「高能熔炉专属配方」和「原版冶炼配方」两个桶并成一个查找入口。
 *
 * <h2>为什么需要它</h2>
 *
 * {@code AbstractFurnaceBlockEntity} 在自己构造函数里建了一个「按类型查配方」的缓存对象：
 *
 * <pre>
 * this.quickCheck = RecipeManager.createCheck(recipeType);
 * </pre>
 *
 * 全类只有 {@code serverTick} 和 {@code getTotalCookTime} 两处用它。
 *
 * <p>高能熔炉现在传的是 {@link ModRecipeTypes#HIGH_ENERGY_SMELTING}，
 * 单查这个桶的话就<b>烧不了任何原版冶炼配方</b>了（铁矿石之类全废）。
 * 所以这里做个「先查专属桶、查不到再退回原版桶」的包装，
 * 靠 {@code mixin/FurnaceRecipeCheckMixin} 换进 {@code quickCheck}。
 *
 * <p>顺序很重要：专属桶优先。否则同一材料在两边都有配方时会随机命中，
 * 手感上会「时快时慢」。
 */
public final class CombinedCookingCheck
		implements RecipeManager.CachedCheck<SingleRecipeInput, AbstractCookingRecipe> {
	/** 高能熔炉专属（钛的烧制）。 */
	private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe>
			highEnergy;

	/** 原版冶炼，兜底。 */
	private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe>
			smelting;

	public CombinedCookingCheck() {
		this.highEnergy = RecipeManager.createCheck(ModRecipeTypes.HIGH_ENERGY_SMELTING);
		this.smelting = RecipeManager.createCheck(RecipeType.SMELTING);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Optional<RecipeHolder<AbstractCookingRecipe>> getRecipeFor(
			SingleRecipeInput input, ServerLevel level) {
		Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> hit =
				this.highEnergy.getRecipeFor(input, level);

		if (hit.isPresent()) {
			return Optional.of((RecipeHolder<AbstractCookingRecipe>) (RecipeHolder<?>) hit.get());
		}

		Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> fallback =
				this.smelting.getRecipeFor(input, level);

		return fallback.map(holder ->
				(RecipeHolder<AbstractCookingRecipe>) (RecipeHolder<?>) holder);
	}
}
