package com.example.moreores.mixin;

import com.example.moreores.furnace.BioCatalyzerBlockEntity;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物催化器的**催化物槽一格只放 1 个**。
 *
 * <p>需求原话是「树苗 / 作物种子不消耗且只能放入一个」——
 * 「不消耗」由 {@code BioCatalyzerBlock} 的「快照 + 还原」负责，
 * 「只放一个」在这儿卡。
 *
 * <p><b>为什么注入 {@code Slot} 而不是换槽</b>：和
 * {@link FurnaceFuelSlotMixin} 是同一套理由 —— 原版的输入槽自带
 * 放置判定与同步逻辑，自己 new 一个换上去容易把别的行为一起弄丢。
 * 只挂一个 {@code getMaxStackSize} 的注入，鼠标点、拖拽、Shift 快移
 * 三条路径都会来问这个方法，一处就够。
 *
 * <p>⚠️ 目标是原版的 {@link Slot}（输入槽就是最普通的 {@code Slot}，
 * 不像燃料槽有专门的 {@code FurnaceFuelSlot}），所以这个注入对**所有**槽位
 * 都会跑一遍 —— 靠容器类型 + 槽位号两层判断把自己摘出去。
 */
@Mixin(Slot.class)
public abstract class FurnaceInputSlotMixin {
	/** 催化物的上限。 */
	private static final int CATALYST_LIMIT = 1;

	@Shadow
	public abstract int getContainerSlot();

	/** 只为拿到 Slot 的 {@code container} 字段；Mixin 类不会被实例化。 */
	@Shadow
	protected net.minecraft.world.Container container;

	@Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
			at = @At("HEAD"), cancellable = true)
	private void moreores$limitCatalyst(ItemStack stack, CallbackInfoReturnable<Integer> info) {
		if (this.container instanceof BioCatalyzerBlockEntity
				&& this.getContainerSlot() == BioCatalyzerBlockEntity.SLOT_CATALYST) {
			info.setReturnValue(CATALYST_LIMIT);
		}
	}
}
