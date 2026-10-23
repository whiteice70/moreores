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
 * 矿物催化器的配方：把石头 / 泥土 / 黑曜石一类的自然方块复制成自身。
 *
 * <p>结构和 {@link BioCatalyzingRecipe} 完全一致，只是配方类型指向
 * {@link ModRecipeTypes#MINERAL_CATALYZING}，图标换成矿物催化器。
 *
 * <p>原料同样<b>不会被消耗</b> —— 由方块实体那边的「快照 + 还原」负责。
 */
public class MineralCatalyzingRecipe extends AbstractCookingRecipe {
	public static final MapCodec<MineralCatalyzingRecipe> MAP_CODEC =
			cookingMapCodec(MineralCatalyzingRecipe::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, MineralCatalyzingRecipe> STREAM_CODEC =
			cookingStreamCodec(MineralCatalyzingRecipe::new);

	public static final RecipeSerializer<MineralCatalyzingRecipe> SERIALIZER =
			new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/** 参数顺序与 {@link AbstractCookingRecipe.Factory} 完全一致。 */
	public MineralCatalyzingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo,
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
		return ModRecipeTypes.MINERAL_CATALYZING;
	}

	/** 这些配方不进原版熔炉的配方书，归到哪一栏都无所谓。 */
	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.FURNACE_MISC;
	}

	@Override
	protected Item furnaceIcon() {
		return ModBlocks.MINERAL_CATALYZER.asItem();
	}
}
