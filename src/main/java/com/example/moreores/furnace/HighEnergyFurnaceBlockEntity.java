package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;
import com.example.moreores.recipe.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;

/**
 * 高能熔炉的方块实体。
 *
 * <p>在普通熔炉的基础上改三件事：
 * <ol>
 *   <li><b>烧制速度 ×10</b> —— 覆写 {@link #getSpeedMultiplier}</li>
 *   <li><b>燃料消耗速度 ×10</b> —— 覆写 {@link #getBurnDuration} 把时长除以 10
 *       （注意这是「同样的燃料只烧 1/10 时间」，不是「烧得更快更省」——
 *       配合速度 ×10，单位燃料能烧出的产物数量和普通熔炉一致，
 *       好处是**同样时间内的吞吐量是 10 倍**）</li>
 *   <li><b>多一个副产物槽</b>（索引 3）—— 燃料烧掉后留下的东西放这里，
 *       比如岩浆桶留下空桶、铀锭留下铅锭（铀的衰变终点就是铅）</li>
 * </ol>
 */
public class HighEnergyFurnaceBlockEntity extends AbstractFurnaceBlockEntity implements MenuProvider {
	/** 副产物槽的索引。前三个沿用父类：0 输入 / 1 燃料 / 2 输出。 */
	public static final int SLOT_BYPRODUCT = 3;

	/** 槽位总数。 */
	private static final int SLOT_COUNT = 4;

	/** 相对普通熔炉的速度倍率。 */
	private static final float SPEED_MULTIPLIER = 10.0F;

	/** 燃料消耗倍率：同样的燃料，烧完所需时间是原来的 1/10。 */
	private static final int FUEL_BURN_DIVISOR = 10;

	public HighEnergyFurnaceBlockEntity(BlockPos pos, BlockState state) {
		// ⚠️ 这里传的是**自定义**配方类型，不是 RecipeType.SMELTING。
		// 配方在 RecipeManager 里按类型分桶，传自定义类型才能让
		// 「只有高能熔炉能烧」成立（钛的配方挂在这个桶里）。
		// 想让高能熔炉同时保留原版冶炼能力，靠的是 mixin/FurnaceRecipeCheckMixin
		// 把这个桶和 SMELTING 桶并成一次查找。
		super(ModBlockEntities.HIGH_ENERGY_FURNACE, pos, state,
				ModRecipeTypes.HIGH_ENERGY_SMELTING);
		// 父类构造里建的是 3 格列表，这里换成 4 格
		this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	}

	// ---------------- 速度与燃料 ----------------

	@Override
	protected float getSpeedMultiplier(ServerLevel level, ItemStack fuel) {
		return SPEED_MULTIPLIER;
	}

	@Override
	protected int getBurnDuration(ServerLevel level, ItemStack fuel) {
		int base = super.getBurnDuration(level, fuel);
		return base <= 0 ? 0 : Math.max(1, base / FUEL_BURN_DIVISOR);
	}

	// ---------------- 副产物 ----------------

	/**
	 * 把副产物放进第 4 格。放不下就堆在原有堆里。
	 *
	 * <p>具体的「什么燃料产出什么」写在
	 * {@code mixin/FurnaceFuelMixin#byproductOf}，因为触发点在父类的私有方法里。
	 *
	 * @return 是否成功放进去了（放不下时调用方可以决定弹出）
	 */
	public boolean storeByproduct(ItemStack byproduct) {
		if (byproduct.isEmpty()) {
			return true;
		}

		ItemStack slot = this.items.get(SLOT_BYPRODUCT);

		if (slot.isEmpty()) {
			this.items.set(SLOT_BYPRODUCT, byproduct.copyWithCount(1));
			return true;
		}

		if (ItemStack.isSameItemSameComponents(slot, byproduct)
				&& slot.getCount() < slot.getMaxStackSize()) {
			slot.grow(1);
			return true;
		}

		return false;
	}

	// ---------------- 槽位 ----------------

	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == SLOT_BYPRODUCT) {
			// 副产物槽只出不进，由机器自己填
			return false;
		}

		return super.canPlaceItem(slot, stack);
	}

	/**
	 * 漏斗能访问哪些槽。原文只在 DOWN 方向加了副产物槽，其余沿用原版熔炉：
	 *
	 * <ul>
	 *   <li><b>下方</b>：输出槽 + **副产物槽** + 燃料槽
	 *       —— 产物和副产物走同一条路被抽走，燃料槽那一项是为了让漏斗能取走烧完的空桶</li>
	 *   <li><b>上方</b>：只有输入槽</li>
	 *   <li><b>侧面</b>：输入槽 + 燃料槽</li>
	 * </ul>
	 *
	 * <p>副产物槽虽然在下方的可访问列表里，但 {@link #canPlaceItem} 对它返回 false，
	 * 而父类的 {@code canPlaceItemThroughFace} 正是转发到那里，
	 * 所以漏斗**只能抽不能塞**。
	 */
	@Override
	public int[] getSlotsForFace(Direction side) {
		if (side == Direction.DOWN) {
			return new int[]{SLOT_RESULT, SLOT_BYPRODUCT, SLOT_FUEL};
		}

		return super.getSlotsForFace(side);
	}

	/** 下方抽燃料槽时沿用原版限制（只有空桶之类可回收的才让抽）。 */
	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		if (direction == Direction.DOWN && index == SLOT_BYPRODUCT) {
			return true;
		}

		return super.canTakeItemThroughFace(index, stack, direction);
	}

	// ---------------- 菜单 ----------------

	/** 容器标题。注意要覆写的是 {@code getDefaultName()}（MenuProvider 的 getDisplayName 已经转发到这里）。 */
	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.moreores.high_energy_furnace");
	}

	/** MenuProvider 的入口（右键打开时走这个）。 */
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new HighEnergyFurnaceMenu(containerId, inventory, this, this.dataAccess);
	}

	/** BaseContainerBlockEntity 要求的那个重载，通常只有内部逻辑会用。 */
	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new HighEnergyFurnaceMenu(containerId, inventory, this, this.dataAccess);
	}

	/** 供菜单构造时读取进度条数据用。 */
	public ContainerData dataAccess() {
		return this.dataAccess;
	}

	/** 让辐射扫描之类的外部逻辑能判断"这个容器是不是高能熔炉"。 */
	public static boolean isHighEnergyFurnace(Container container) {
		return container instanceof HighEnergyFurnaceBlockEntity;
	}
}
