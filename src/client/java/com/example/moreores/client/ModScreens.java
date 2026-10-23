package com.example.moreores.client;

import com.example.moreores.ModMenus;
import com.example.moreores.client.furnace.HighEnergyFurnaceScreen;
import com.example.moreores.client.furnace.BioCatalyzerScreen;

import net.minecraft.client.gui.screens.MenuScreens;

/** 客户端界面注册。 */
public final class ModScreens {
	private ModScreens() {
	}

	public static void register() {
		MenuScreens.register(ModMenus.HIGH_ENERGY_FURNACE, HighEnergyFurnaceScreen::new);
		MenuScreens.register(ModMenus.BIO_CATALYZER, BioCatalyzerScreen::new);
	}
}
