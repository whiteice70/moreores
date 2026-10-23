package com.example.moreores.furnace;

import com.example.moreores.ModMenus;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;

/**
 * 矿物催化器的菜单。槽位布局与生物催化器完全一致，只是菜单类型不同。
 */
public class MineralCatalyzerMenu extends AbstractFurnaceMenu {
	public static final int BYPRODUCT_SLOT_X = 116;
	public static final int BYPRODUCT_SLOT_Y = 53;

	/** 客户端构造：容器内容与进度由服务端 openMenu 时同步过来。 */
	public MineralCatalyzerMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(4), new SimpleContainerData(4));
	}

	public MineralCatalyzerMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.MINERAL_CATALYZER, RecipePropertySet.FURNACE_INPUT,
				RecipeBookType.FURNACE, containerId, inventory, container, data);

		this.addSlot(new Slot(container, MineralCatalyzerBlockEntity.SLOT_BYPRODUCT,
				BYPRODUCT_SLOT_X, BYPRODUCT_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				// 副产物槽只出不进
				return false;
			}
		});
	}
}
