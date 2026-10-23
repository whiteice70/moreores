package com.example.moreores.recipe;

import com.example.moreores.ModBlocks;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 生物催化器的配方：把「树苗 / 种子」催化成「木头 / 作物」。
 *
 * <p>结构和 {@link HighEnergySmeltingRecipe} 一模一样（都是 {@link AbstractCookingRecipe}
 * 的翻版），只有两处不同：{@link #getType()} 指向
 * {@link ModRecipeTypes#BIO_CATALYZING}，以及 {@link #furnaceIcon()} 返回催化器本身。
 *
 * <p>因为 {@code RecipeManager} 按 {@code getType()} 分桶，这类配方<b>天然不会被
 * 熔炉 / 高炉 / 烟熏炉 / 营火看到</b>，反过来催化器也看不到冶炼配方 —— 两边互不干扰。
 *
 * <p>JSON 里写 {@code "type": "moreores:bio_catalyzing"}，
 * 其余字段（{@code ingredient} / {@code result} / {@code experience} / {@code cookingtime}）
 * 和 {@code minecraft:smelting} 完全相同。
 *
 * <p>⚠️ 配方里的 {@code ingredient} <b>不会被消耗</b> —— 那是催化器特有的行为，
 * 靠方块实体那边的「快照 + 还原」（见 {@code BioCatalyzerBlock}）。
 * 配方本身并不知道这件事，它只负责「什么东西能变成什么」。
 */
public class BioCatalyzingRecipe extends AbstractCookingRecipe {
	public static final MapCodec<BioCatalyzingRecipe> MAP_CODEC =
			cookingMapCodec(BioCatalyzingRecipe::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, BioCatalyzingRecipe> STREAM_CODEC =
			cookingStreamCodec(BioCatalyzingRecipe::new);

	public static final RecipeSerializer<BioCatalyzingRecipe> SERIALIZER =
			new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/** 参数顺序与 {@link AbstractCookingRecipe.Factory} 完全一致。 */
	public BioCatalyzingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo,
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
		return ModRecipeTypes.BIO_CATALYZING;
	}

	/** 这些配方不进原版熔炉的配方书，归到哪一栏都无所谓。 */
	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.FURNACE_MISC;
	}

	@Override
	protected Item furnaceIcon() {
		return ModBlocks.BIO_CATALYZER.asItem();
	}
}
