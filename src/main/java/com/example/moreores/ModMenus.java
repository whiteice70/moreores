package com.example.moreores;

import com.example.moreores.furnace.HighEnergyFurnaceMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** 模组的界面（容器菜单）类型。 */
public final class ModMenus {
	public static final MenuType<HighEnergyFurnaceMenu> HIGH_ENERGY_FURNACE =
			Registry.register(BuiltInRegistries.MENU,
					ResourceKey.create(Registries.MENU, MoreOres.id("high_energy_furnace")),
					new MenuType<>(HighEnergyFurnaceMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<com.example.moreores.furnace.BioCatalyzerMenu> BIO_CATALYZER =
			Registry.register(BuiltInRegistries.MENU,
					ResourceKey.create(Registries.MENU, MoreOres.id("bio_catalyzer")),
					new MenuType<>(com.example.moreores.furnace.BioCatalyzerMenu::new,
							FeatureFlags.VANILLA_SET));

	private ModMenus() {
	}

	/** 触发本类的静态初始化，完成菜单注册。 */
	public static void register() {
	}
}
