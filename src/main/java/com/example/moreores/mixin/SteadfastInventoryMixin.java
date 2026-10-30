package com.example.moreores.mixin;

import com.example.moreores.ModEnchantments;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 坚守附魔：死亡时只让**没有坚守**的物品掉出去。
 *
 * <p>原版 {@link Inventory#dropAll()} 会把每一格都生成掉落物并清空槽位；
 * 这里在 HEAD 处接管，对带坚守的格子直接跳过 —— 槽位保持原样，
 * 玩家复活后物品还在背包里（原版「死亡不掉落」规则用的也是同一条路径）。
 *
 * <p>只在确实存在坚守物品时才接管；没有的话直接放行原版逻辑，避免改变普通死亡行为。
 */
@Mixin(Inventory.class)
public abstract class SteadfastInventoryMixin {
	@Shadow
	@Final
	private NonNullList<ItemStack> items;

	@Shadow
	@Final
	private Player player;

	@Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
	private void moreores$keepSteadfastItems(CallbackInfo ci) {
		boolean hasKept = false;

		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);

			if (!stack.isEmpty() && ModEnchantments.hasSteadfast(stack)) {
				hasKept = true;
				break;
			}
		}

		if (!hasKept) {
			return;
		}

		// 和原版 dropAll 相同的掉落流程，只是跳过带坚守的格子
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);

			if (stack.isEmpty() || ModEnchantments.hasSteadfast(stack)) {
				continue;
			}

			ItemEntity entity = this.player.createItemStackToDrop(stack, true, false);

			if (entity != null) {
				this.player.level().addFreshEntity(entity);
			}

			this.items.set(i, ItemStack.EMPTY);
		}

		ci.cancel();
	}
}
