package com.example.moreores.recipe;

import com.example.moreores.MoreOres;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 模组自己的配方类型。
 *
 * <p>存在的唯一理由是让<b>「只有高能熔炉能烧」</b>这件事成立：
 * 配方在 {@code RecipeManager} 里是<b>按类型分桶</b>存的，
 * 普通熔炉查 {@link RecipeType#SMELTING}、高能熔炉原本也查它 ——
 * 两者共用一桶，没法只让其中一个看见某条配方。
 *
 * <p>所以给这类配方单开一个桶：配方写 {@code "type": "moreores:high_energy_smelting"}，
 * 于是<b>所有原版炉子（熔炉 / 高炉 / 烟熏炉 / 营火）天然看不到它</b>，
 * 而高能熔炉靠 {@link CombinedCookingCheck} 同时查两个桶。
 *
 * <p>⚠️ 这个类的静态字段<b>不能</b>挪进 {@link ModRecipes}：
 * {@code HighEnergySmeltingRecipe.getType()} 会引用它，而 {@code ModRecipes} 又引用
 * {@code HighEnergySmeltingRecipe.SERIALIZER} —— 放一起就构成类的循环初始化，
 * 结果是这个常量读到 {@code null}。分开就没这问题（泛型参数在运行期会被擦除，
 * 不会触发 {@code HighEnergySmeltingRecipe} 的初始化）。
 *
 * <p>⚠️ <b>不能用原版的 {@code RecipeType.register(String)} 便捷方法</b>。
 * 它内部是 {@code Identifier.withDefaultNamespace(name)} —— 这个方法会把**整串**当成
 * path、硬加上 {@code minecraft:} 前缀，传 {@code "moreores:xxx"} 进去就变成
 * {@code minecraft:moreores:xxx}，直接抛 {@code IdentifierException} 把游戏打崩。
 *
 * <p>所以这里手动 {@code Registry.register}，用 {@link MoreOres#id} 拿到正确的命名空间。
 */
public final class ModRecipeTypes {
	/** 只有高能熔炉认的冶炼类型。 */
	public static final RecipeType<HighEnergySmeltingRecipe> HIGH_ENERGY_SMELTING =
			Registry.register(BuiltInRegistries.RECIPE_TYPE,
					MoreOres.id("high_energy_smelting"),
					new RecipeType<HighEnergySmeltingRecipe>() {
						@Override
						public String toString() {
							return "moreores:high_energy_smelting";
						}
					});

	/** 只有生物催化器认的类型：树苗 / 种子 → 木头 / 作物。 */
	public static final RecipeType<BioCatalyzingRecipe> BIO_CATALYZING =
			Registry.register(BuiltInRegistries.RECIPE_TYPE,
					MoreOres.id("bio_catalyzing"),
					new RecipeType<BioCatalyzingRecipe>() {
						@Override
						public String toString() {
							return "moreores:bio_catalyzing";
						}
					});

	private ModRecipeTypes() {
	}

	/** 空方法，调用它只为触发静态初始化完成注册。 */
	public static void register() {
	}
}
