package com.example.moreores.mixin;

import com.example.moreores.recipe.CombinedCookingCheck;
import com.example.moreores.recipe.ModRecipeTypes;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让高能熔炉「同时认两个配方桶」。
 *
 * <h2>为什么要从这儿下手</h2>
 *
 * {@code AbstractFurnaceBlockEntity} 的配方查找入口是构造里建的那一个对象：
 *
 * <pre>
 * this.quickCheck = RecipeManager.createCheck(recipeType);
 * </pre>
 *
 * 而整个类里用它查配方的地方<b>只有两处</b>（{@code serverTick} 和
 * {@code getTotalCookTime}）。所以只要在这里把「单桶查找」换成
 * {@link CombinedCookingCheck}，两处调用点全都自动跟着变 ——
 * <b>不需要去改那两个方法</b>。
 *
 * <h2>为什么能区分是不是高能熔炉</h2>
 *
 * 注入的方法是构造里那一次 {@code createCheck}，参数就是子类传进来的配方类型。
 * 高能熔炉传的是 {@link ModRecipeTypes#HIGH_ENERGY_SMELTING}，
 * 原版熔炉传的是 {@code RecipeType.SMELTING} —— 拿参数一比就知道，
 * 而且此时 {@code this} 还没构造完，也不该去碰它。
 *
 * <p>普通熔炉走的是 {@code else} 分支，行为与改动前<b>逐字节一致</b>。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceRecipeCheckMixin {
	@Redirect(method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;"
			+ "Lnet/minecraft/core/BlockPos;"
			+ "Lnet/minecraft/world/level/block/state/BlockState;"
			+ "Lnet/minecraft/world/item/crafting/RecipeType;)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/item/crafting/RecipeManager;"
							+ "createCheck(Lnet/minecraft/world/item/crafting/RecipeType;)"
							+ "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;"))
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static RecipeManager.CachedCheck<?, ?> moreores$combineCookingChecks(RecipeType<?> type) {
		if (type == ModRecipeTypes.HIGH_ENERGY_SMELTING) {
			return new CombinedCookingCheck();
		}

		return RecipeManager.createCheck((RecipeType) type);
	}
}
