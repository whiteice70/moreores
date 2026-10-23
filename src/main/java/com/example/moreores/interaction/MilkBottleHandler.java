package com.example.moreores.interaction;

import com.example.moreores.ModItems;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

/**
 * 用玻璃瓶给牛挤奶。
 *
 * <p>原版只有「空桶 → 牛奶桶」这一条路，而且写死在 {@code AbstractCow#mobInteract} 里，
 * 所以只能挂 Fabric 的 {@link UseEntityCallback} 自己接一档。
 *
 * <p>判定条件和原版挤奶保持一致：**成年牛**才行，小牛不给挤。
 */
public final class MilkBottleHandler {
	private MilkBottleHandler() {
	}

	public static void register() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);

			// 只认「玻璃瓶 + 成年牛」，其余情况让给原版逻辑
			if (!(entity instanceof AbstractCow cow) || cow.isBaby()) {
				return InteractionResult.PASS;
			}

			if (!stack.is(Items.GLASS_BOTTLE)) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				// 客户端只负责挥手动画，真正发物品在服务端做
				return InteractionResult.SUCCESS;
			}

			player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);

			// createFilledResult 会处理「手上只有 1 个 / 有一叠」两种情况：
			// 一叠时扣掉 1 个玻璃瓶，再把牛奶瓶塞进背包。
			player.setItemInHand(hand, ItemUtils.createFilledResult(
					stack, player, new ItemStack(ModItems.MILK_BOTTLE)));

			return InteractionResult.SUCCESS;
		});
	}
}
