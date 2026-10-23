package com.example.moreores.mixin;

import com.example.moreores.ModItems;
import com.example.moreores.furnace.HighEnergyFurnaceBlockEntity;

import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 普通熔炉的燃料槽里，铀锭一格只放 1 个。
 *
 * <p>为什么要限制：铀锭热值是岩浆桶的 10 倍（200000 tick ≈ 2.8 小时），
 * 一叠 64 个塞进去够烧一周，实际用起来更像"卡在槽里的燃料块"。
 *
 * <p><b>为什么注入这里而不是替换槽</b>：原版的燃料槽是 {@link FurnaceFuelSlot}，
 * 它自己覆写了 {@code mayPlace} 做燃料判定。之前图省事把整个槽换成普通
 * {@code Slot}，结果燃料判定一起丢了 —— 手动往燃料槽放铀锭会被拒。
 * 现在只挂一个 {@code getMaxStackSize} 的注入，原槽原封不动保留。
 *
 * <p>只改这一个方法就能同时管住「鼠标点击放置」「拖拽分配」「Shift 快速移动」
 * 三条路径，因为它们都会去问槽位能放多少。
 *
 * <p>高能熔炉不受影响（它烧得快，燃料时长已被除以 10），靠容器类型排除。
 */
@Mixin(FurnaceFuelSlot.class)
public abstract class FurnaceFuelSlotMixin extends Slot {
	/** 铀锭在普通熔炉燃料槽里的上限。 */
	private static final int URANIUM_LIMIT = 1;

	// 只为拿到 Slot 的 container 字段；Mixin 类不会被实例化。
	private FurnaceFuelSlotMixin() {
		super(null, 0, 0, 0);
	}

	@Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
			at = @At("HEAD"), cancellable = true)
	private void moreores$limitUranium(ItemStack stack, CallbackInfoReturnable<Integer> info) {
		if (!(stack.is(ModItems.URANIUM_INGOT))) {
			return;
		}

		// 高能熔炉照旧
		if (this.container instanceof HighEnergyFurnaceBlockEntity) {
			return;
		}

		info.setReturnValue(URANIUM_LIMIT);
	}
}
