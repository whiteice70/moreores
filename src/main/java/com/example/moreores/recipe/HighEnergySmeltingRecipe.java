package com.example.moreores.recipe;

import com.example.moreores.ModBlocks;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 只能在高能熔炉里烧的配方。
 *
 * <p>它就是 {@code SmeltingRecipe} 的翻版，只改一件事：{@link #getType()} 返回
 * {@link ModRecipeTypes#HIGH_ENERGY_SMELTING} 而不是 {@code RecipeType.SMELTING}。
 * 而 {@code RecipeManager} 正是按 {@code getType()} 给配方分桶的 ——
 * 于是这条配方<b>永远落不进原版冶炼那一桶</b>，普通熔炉自然找不到它。
 *
 * <p>JSON 里写 {@code "type": "moreores:high_energy_smelting"}，
 * 其余字段（{@code ingredient} / {@code result} / {@code experience} / {@code cookingtime}）
 * 和 {@code minecraft:smelting} 完全一样。
 */
public class HighEnergySmeltingRecipe extends AbstractCookingRecipe {
	public static final MapCodec<HighEnergySmeltingRecipe> MAP_CODEC =
			cookingMapCodec(HighEnergySmeltingRecipe::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, HighEnergySmeltingRecipe> STREAM_CODEC =
			cookingStreamCodec(HighEnergySmeltingRecipe::new);

	public static final RecipeSerializer<HighEnergySmeltingRecipe> SERIALIZER =
			new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/**
	 * 参数顺序与 {@link AbstractCookingRecipe.Factory} 完全一致 ——
	 * {@code cookingMapCodec(HighEnergySmeltingRecipe::new)} 就是靠它反序列化的。
	 */
	public HighEnergySmeltingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo,
			Ingredient ingredient, ItemStackTemplate result,
			float experience, int cookingTime) {
		super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
	}

	@Override
	public RecipeSerializer<? extends AbstractCookingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<? extends AbstractCookingRecipe> getType() {
		return ModRecipeTypes.HIGH_ENERGY_SMELTING;
	}

	/**
	 * 配方书分类。这些配方不在原版熔炉那本书里，所以这里取什么其实不影响玩法；
	 * 填 {@code FURNACE_BLOCKS} 只是让「万一被翻到」时归到方块那一栏。
	 */
	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.FURNACE_BLOCKS;
	}

	/**
	 * 代表「烧这个配方的炉子」的那件物品（原版熔炉返回熔炉、高炉返回高炉）。
	 * 这里自然是高能熔炉本身。
	 */
	@Override
	protected Item furnaceIcon() {
		return ModBlocks.HIGH_ENERGY_FURNACE.asItem();
	}
}
