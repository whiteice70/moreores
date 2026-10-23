package com.example.moreores.mixin;

import com.example.moreores.ModItems;
import com.example.moreores.furnace.HighEnergyFurnaceBlockEntity;
import com.example.moreores.furnace.BioCatalyzerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 高能熔炉 / 生物催化器的燃料副产物：燃料被消耗时顺手产出（铀锭 → 铅锭、
 * 核子电池 → 耗尽的电池），放进第 4 格，塞不下就吐在方块上方。
 *
 * <p>{@code consumeFuel} 是父类的私有静态方法，没有别的入口，只能从这儿注入。
 *
 * <p>⚠️ <b>岩浆桶不在这张表里。</b>原版熔炉烧掉岩浆桶之后，
 * 自己就会把空桶放回燃料槽 —— 这里再产一个就是「一个岩浆桶出两个空桶」。
 * 实测（原版熔炉做对照，岩浆桶 + 原料）：
 *
 * <pre>
 *   燃料槽(1): lava_bucket -&gt; bucket     ← 原版干的
 *   副产物槽(3): bucket                  ← Mixin 干的，重复
 * </pre>
 *
 * <p>判据是「原版有没有把容器还回来」：还了的就别插手。
 * 铀锭和核子电池都是**自定义物品**，原版不认识，自然也不会还，所以只有它们需要这条路。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceFuelMixin {
	@Inject(method = "consumeFuel", at = @At("HEAD"))
	private static void moreores$dropByproduct(ServerLevel level, BlockPos pos,
			NonNullList<ItemStack> items, ItemStack fuel, CallbackInfo info) {
		// 注入点用 HEAD —— 这时 fuel 还是原物品（还没 shrink），能判断出烧的是什么
		ItemStack byproduct = byproductOf(fuel);

		if (byproduct.isEmpty()) {
			return;
		}

		// 两台机器各有一个「第 4 格」用来收这种残留物，行为一样、类不同，所以分开判断
		boolean stored;

		if (level.getBlockEntity(pos) instanceof HighEnergyFurnaceBlockEntity furnace) {
			stored = furnace.storeByproduct(byproduct);
		} else if (level.getBlockEntity(pos) instanceof BioCatalyzerBlockEntity catalyzer) {
			stored = catalyzer.storeByproduct(byproduct);
		} else {
			return;
		}

		if (!stored) {
			Block.popResource(level, pos, byproduct);
		}
	}

	/** 燃料烧掉后留下什么。空表示没有副产物（也包括「原版自己会还」的那些）。 */
	private static ItemStack byproductOf(ItemStack fuel) {
		if (fuel.is(ModItems.URANIUM_INGOT)) {
			// 铀的天然衰变链终点就是铅
			return new ItemStack(ModItems.LEAD_INGOT);
		}

		if (fuel.is(ModItems.NUCLEAR_BATTERY)) {
			// 电用完了，只剩壳
			return new ItemStack(ModItems.DEPLETED_NUCLEAR_BATTERY);
		}

		return ItemStack.EMPTY;
	}
}
