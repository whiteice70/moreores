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
 * 高能熔炉的菜单。界面排成**两行平行**，沿用原版图幅 176×166：
 *
 * <pre>
 *   原料 (56,17)  → 烧制物 (116,17)
 *        火焰
 *   燃料 (56,53)     副产物 (116,53)
 * </pre>
 *
 * <p>左列（原料 / 燃料）由父类按原版坐标铺好。
 *
 * <p>右列的**烧制物槽**坐标由 {@code mixin/AbstractFurnaceMenuMixin} 在字节码层面
 * 把原版的 y=35 改成 17 —— **注意这里一行槽位代码都没有**，因为原版
 * {@code addSlot} 要同步维护 {@code slots} / {@code lastSlots} / {@code remoteSlots}
 * 三个平行列表，自己 {@code slots.set()} 替换槽会把后面两个落下，
 * 导致客户端取不出物品、进度也不刷新。
 *
 * <p>**副产物槽**是纯粹多出来的第 4 格，直接 {@code addSlot} 追加即可
 * （父类已占 0~38，它会被排到索引 39，但坐标由我们指定 —— 索引和位置是两回事）。
 */
public class HighEnergyFurnaceMenu extends AbstractFurnaceMenu {
	/** 原料槽（父类铺的，仅作对照理解布局）。 */
	public static final int INPUT_SLOT_X = 56;
	public static final int INPUT_SLOT_Y = 17;

	/** 燃料槽（父类铺的）。 */
	public static final int FUEL_SLOT_X = 56;
	public static final int FUEL_SLOT_Y = 53;

	/** 烧制物槽：实际坐标由 Mixin 改常量实现，这里只是记录下来给界面参考。 */
	public static final int RESULT_SLOT_X = 116;
	public static final int RESULT_SLOT_Y = 17;

	/** 副产物槽：和燃料槽平行。 */
	public static final int BYPRODUCT_SLOT_X = 116;
	public static final int BYPRODUCT_SLOT_Y = 53;

	/** 客户端构造：服务端会通过 openMenu 把容器内容与进度数据同步过来。 */
	public HighEnergyFurnaceMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(4), new SimpleContainerData(4));
	}

	public HighEnergyFurnaceMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.HIGH_ENERGY_FURNACE, RecipePropertySet.FURNACE_INPUT,
				RecipeBookType.FURNACE, containerId, inventory, container, data);

		// 只追加副产物槽；烧制物槽的坐标调整交给 Mixin，不动槽位结构
		this.addSlot(new Slot(container, HighEnergyFurnaceBlockEntity.SLOT_BYPRODUCT,
				BYPRODUCT_SLOT_X, BYPRODUCT_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				// 副产物槽只出不进
				return false;
			}
		});
	}
}
