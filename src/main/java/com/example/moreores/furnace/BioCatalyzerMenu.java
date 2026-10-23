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
 * 生物催化器的菜单。槽位布局和高能熔炉完全一致（界面也共用同一张底图）：
 *
 * <pre>
 *   催化物 (56,17)  → 产物 (116,17)
 *        火焰
 *   燃料   (56,53)     副产物 (116,53)
 * </pre>
 *
 * <p>之所以没直接继承 {@code HighEnergyFurnaceMenu}：父类构造函数里
 * {@code super(...)} 写死了 {@code ModMenus.HIGH_ENERGY_FURNACE} 这个菜单类型，
 * 而菜单类型正是客户端「用哪个界面」的依据 —— 继承过来就变成共用一个类型了，
 * 界面虽然一样，但语义上两台机器会混在一起。所以这里照抄结构、换成自己的类型。
 */
public class BioCatalyzerMenu extends AbstractFurnaceMenu {
	public static final int INPUT_SLOT_X = 56;
	public static final int INPUT_SLOT_Y = 17;
	public static final int FUEL_SLOT_X = 56;
	public static final int FUEL_SLOT_Y = 53;
	public static final int RESULT_SLOT_X = 116;
	public static final int RESULT_SLOT_Y = 17;
	public static final int BYPRODUCT_SLOT_X = 116;
	public static final int BYPRODUCT_SLOT_Y = 53;

	/** 客户端构造：容器内容与进度由服务端 openMenu 时同步过来。 */
	public BioCatalyzerMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(4), new SimpleContainerData(4));
	}

	public BioCatalyzerMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.BIO_CATALYZER, RecipePropertySet.FURNACE_INPUT,
				RecipeBookType.FURNACE, containerId, inventory, container, data);

		// 只追加副产物槽；产物槽的坐标由 mixin/AbstractFurnaceMenuMixin 统一挪到 y=17
		this.addSlot(new Slot(container, BioCatalyzerBlockEntity.SLOT_BYPRODUCT,
				BYPRODUCT_SLOT_X, BYPRODUCT_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				// 副产物槽只出不进
				return false;
			}
		});
	}
}
