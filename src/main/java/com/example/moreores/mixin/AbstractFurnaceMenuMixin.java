package com.example.moreores.mixin;

import com.example.moreores.furnace.HighEnergyFurnaceMenu;
import com.example.moreores.furnace.BioCatalyzerMenu;
import com.example.moreores.furnace.MineralCatalyzerMenu;
import com.example.moreores.furnace.ShortSafeContainerData;

import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 熔炉菜单的两处改动，两个注入点都落在<b>同一个构造函数</b>上。
 *
 * <ol>
 *   <li><b>烧制物槽挪位</b> —— 只对高能熔炉生效，见 {@link #moreores$alignResultSlot}。</li>
 *   <li><b>火焰时长缩写</b> —— 对所有熔炉生效，见 {@link #moreores$shrinkLitTime}。</li>
 * </ol>
 */
@Mixin(AbstractFurnaceMenu.class)
public class AbstractFurnaceMenuMixin {
	/** 原版结果槽的 y。 */
	private static final int VANILLA_RESULT_Y = 35;

	/** 我们要的 y：和原料槽（17）齐平。 */
	private static final int ALIGNED_RESULT_Y = 17;

	/**
	 * 真正铺槽位、登记数据槽的那个构造（另一个重载只是转发过来）。
	 */
	private static final String INIT =
			"<init>(Lnet/minecraft/world/inventory/MenuType;"
					+ "Lnet/minecraft/resources/ResourceKey;"
					+ "Lnet/minecraft/world/inventory/RecipeBookType;"
					+ "ILnet/minecraft/world/entity/player/Inventory;"
					+ "Lnet/minecraft/world/Container;"
					+ "Lnet/minecraft/world/inventory/ContainerData;)V";

	/**
	 * 高能熔炉的<b>烧制物槽</b>从原版的 {@code (116,35)} 挪到 {@code (116,17)}，
	 * 和原料槽对齐（原版为了让箭头竖直居中，故意把结果槽放低了半格）。
	 *
	 * <p><b>为什么不直接替换槽</b>：最早的做法是在自家菜单的构造函数里
	 * {@code slots.set(2, new FurnaceResultSlot(...))}，看着更"直接"，但**绕过了原版的
	 * {@code addSlot}**。26.3 的 {@code addSlot} 除了往 {@code slots} 里加，还要同步维护
	 * {@code lastSlots}（变化检测）和 {@code remoteSlots}（客户端视图）这两个<b>平行的列表</b>——
	 * 自己 {@code set} 只动了 {@code slots}，结果是客户端那边取不出、进度也不刷新。
	 *
	 * <p>现在的做法是**完全不碰槽位结构**，只在字节码层面把原版构造里那个
	 * {@code bipush 35}（结果槽的 y）改成 17。这样原版该走的流程一步不少，
	 * {@code slots} / {@code lastSlots} / {@code remoteSlots} 三者照常对齐。
	 *
	 * <p>只对高能熔炉生效，普通熔炉的坐标原样返回。
	 */
	@ModifyConstant(method = INIT,
			constant = @Constant(intValue = VANILLA_RESULT_Y, ordinal = 0))
	private int moreores$alignResultSlot(int original) {
		// 构造执行到这里时 super() 已经跑完，this 是完整的子类实例。
		// 先用 (Object) 转一下：编译期本类还不认识 HighEnergyFurnaceMenu，
		// 直接 instanceof 会报「不兼容的类型」。
		// 生物催化器 / 矿物催化器用的也是同一套「两行平行」布局，所以一并挪
		return ((Object) this) instanceof HighEnergyFurnaceMenu
				|| ((Object) this) instanceof BioCatalyzerMenu
				|| ((Object) this) instanceof MineralCatalyzerMenu
						? ALIGNED_RESULT_Y : original;
	}

	/**
	 * 把登记给数据槽的那份 {@link ContainerData} 换成「缩短版」，好让超长的燃料时长
	 * 能通过 {@code ClientboundContainerSetDataPacket} 的 short。
	 *
	 * <p>具体原因见 {@link ShortSafeContainerData}：同步包的 {@code value} 字段是
	 * {@code writeShort}，而铀锭的 200000 tick 截断后只剩 3392，
	 * 火焰便按 3392 tick 排空（≈2.8 分钟），和服务端实际的 2.8 小时对不上。
	 *
	 * <p>⚠️ 这里只改<b>传给 {@code addDataSlots} 的那一份</b>。
	 * 菜单自己的 {@code data} 字段、以及方块实体真正用来倒计时的
	 * {@code litTimeRemaining} 都不受影响 —— 所以<b>燃烧时长一点没改</b>，
	 * 只是同步给客户端看的数字变了个比例。
	 *
	 * <p>对总时长 ≤ 32767 的情况（原版燃料、高能熔炉的 20000）系数是 1，
	 * 数值原样返回，行为与改动前完全一致。
	 */
	@ModifyArg(method = INIT,
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;"
							+ "addDataSlots(Lnet/minecraft/world/inventory/ContainerData;)V"),
			index = 0)
	private ContainerData moreores$shrinkLitTime(ContainerData data) {
		return new ShortSafeContainerData(data);
	}
}
