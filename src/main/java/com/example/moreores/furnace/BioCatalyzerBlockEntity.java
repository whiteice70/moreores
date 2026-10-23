package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;
import com.example.moreores.ModItems;
import com.example.moreores.recipe.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;

/**
 * 生物催化器的方块实体。
 *
 * <p>在普通熔炉的基础上改四件事：
 *
 * <ol>
 *   <li><b>配方桶换成 {@link ModRecipeTypes#BIO_CATALYZING}</b> ——
 *       所以它只认「种子 → 作物」这类配方，钛 / 矿石的冶炼配方它一条都看不到</li>
 *   <li><b>只吃核子电池</b> —— {@link #canPlaceItem} 对燃料槽卡死</li>
 *   <li><b>燃料消耗速度 ×10，但烧制速度不变</b> —— 只覆写 {@link #getBurnDuration}。
 *       这一点和高能熔炉<b>正好相反</b>：那边是两个都 ×10（同样时间吞吐 10 倍），
 *       这边是「燃料烧得快、产物出得慢」，也就是<b>同样一份燃料只能产出 1/10 的量</b> ——
 *       代价换的是「不用管供料、一次点火能顶很久」</li>
 *   <li><b>催化物不消耗</b> —— 这一条不在本类里实现，见
 *       {@link BioCatalyzerBlock#getTicker}</li>
 * </ol>
 */
public class BioCatalyzerBlockEntity extends AbstractFurnaceBlockEntity implements MenuProvider {
	/** 副产物槽：烧完的核子电池放这里。前三个沿用父类：0 催化物 / 1 燃料 / 2 产物。 */
	public static final int SLOT_BYPRODUCT = 3;

	/**
	 * 催化物槽的索引。
	 *
	 * <p>就是父类的 `SLOT_INPUT`，但那个是 {@code protected} —— 别的包（比如
	 * {@code mixin} 里的槽位限制）看不到，所以这里对外再公开一份。
	 */
	public static final int SLOT_CATALYST = SLOT_INPUT;

	private static final int SLOT_COUNT = 4;

	/** 燃料消耗倍率：同样一份核子电池，烧完只要 1/10 的时间。 */
	private static final int FUEL_BURN_DIVISOR = 10;

	public BioCatalyzerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BIO_CATALYZER, pos, state,
				ModRecipeTypes.BIO_CATALYZING);
		// 父类构造里建的是 3 格列表，这里换成 4 格
		this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	}

	// ---------------- 燃料 ----------------

	/**
	 * 燃料烧得快、产物出得慢。
	 *
	 * <p>⚠️ 这里和 {@link HighEnergyFurnaceBlockEntity} 的取舍完全不同：
	 * 那边 {@code speedMultiplier = 10} 且时长 ÷10，两者抵消，
	 * 单位燃料的产出和普通熔炉一样；这边<b>只除时长、不提速度</b>，
	 * 所以单位燃料的产出只有普通熔炉的 <b>1/10</b>。
	 */
	@Override
	protected int getBurnDuration(ServerLevel level, ItemStack fuel) {
		int base = super.getBurnDuration(level, fuel);
		return base <= 0 ? 0 : Math.max(1, base / FUEL_BURN_DIVISOR);
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

	/**
	 * 槽位限制。
	 *
	 * <ul>
	 *   <li><b>燃料槽</b>：只收核子电池。这是这台机器和普通熔炉最大的区别 ——
	 *       别的燃料（煤、岩浆桶、铀锭）一律塞不进来。</li>
	 *   <li><b>催化物槽</b>：只在为空时收，也就是「一次只放一样、只放一份」。</li>
	 *   <li><b>副产物槽</b>：只出不进，由机器自己填。</li>
	 * </ul>
	 */
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == SLOT_FUEL) {
			return stack.is(ModItems.NUCLEAR_BATTERY);
		}

		if (slot == SLOT_INPUT) {
			return this.items.get(SLOT_INPUT).isEmpty();
		}

		if (slot == SLOT_BYPRODUCT) {
			return false;
		}

		return super.canPlaceItem(slot, stack);
	}

	/**
	 * 漏斗的可访问槽。下方多给一个副产物槽（耗尽的核子电池要从这儿抽走），
	 * 但因为 {@link #canPlaceItem} 对它返回 false，所以是**只能抽不能塞**。
	 */
	@Override
	public int[] getSlotsForFace(Direction side) {
		if (side == Direction.DOWN) {
			return new int[]{SLOT_RESULT, SLOT_BYPRODUCT, SLOT_FUEL};
		}

		return super.getSlotsForFace(side);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		if (direction == Direction.DOWN && index == SLOT_BYPRODUCT) {
			return true;
		}

		return super.canTakeItemThroughFace(index, stack, direction);
	}

	/** 把耗尽的核子电池放进第 4 格，放不下就交给调用方弹出。 */
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

	// ---------------- 催化物（不消耗） ----------------

	/** 当前催化物。调用方先记下来，跑完一轮再还回去。 */
	public ItemStack getCatalyst() {
		return this.items.get(SLOT_INPUT).copy();
	}

	/**
	 * 把催化物还原回去。
	 *
	 * <p>只在「被消耗掉了」时补 —— 玩家中途把槽清空的话不动它，
	 * 免得凭空冒出东西。
	 */
	public void restoreCatalyst(ItemStack snapshot) {
		if (snapshot.isEmpty()) {
			return;
		}

		ItemStack current = this.items.get(SLOT_INPUT);

		if (current.isEmpty()) {
			this.items.set(SLOT_INPUT, snapshot.copyWithCount(1));
			return;
		}

		// 同一种就补回原数量；换了别的（玩家手动换过）就不插手
		if (ItemStack.isSameItemSameComponents(current, snapshot)
				&& current.getCount() < snapshot.getCount()) {
			current.setCount(snapshot.getCount());
		}
	}

	// ---------------- 菜单 ----------------

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.moreores.bio_catalyzer");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new BioCatalyzerMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new BioCatalyzerMenu(containerId, inventory, this, this.dataAccess);
	}

	/** 供菜单构造时读取进度条数据用（和父类那个受保护字段是同一个对象）。 */
	public ContainerData dataAccess() {
		return this.dataAccess;
	}
}
