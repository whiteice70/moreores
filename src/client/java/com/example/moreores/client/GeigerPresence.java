package com.example.moreores.client;

import com.example.moreores.ModItems;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 「身上有没有盖革计数器」的判定 —— HUD 和咔哒声共用同一套逻辑。
 *
 * <p>**放在背包里就算**（含快捷栏、副手），不需要拿在手上。
 */
public final class GeigerPresence {
	private GeigerPresence() {
	}

	public static boolean isCarried(Player player) {
		Inventory inventory = player.getInventory();

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (!stack.isEmpty() && stack.is(ModItems.GEIGER_COUNTER)) {
				return true;
			}
		}

		return false;
	}
}
