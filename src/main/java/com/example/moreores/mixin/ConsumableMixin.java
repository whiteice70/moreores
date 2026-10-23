package com.example.moreores.mixin;

import com.example.moreores.ModItems;
import com.example.moreores.radiation.ModAttachments;
import com.example.moreores.radiation.RadiationManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 物品吃完 / 喝完时追加的自定义效果。
 *
 * <p>26.3 里所有可食用物品都会经过 {@link ItemStack#finishUsingItem}：
 * <ul>
 *   <li>牛奶桶 → 累积的辐射剂量全部清零</li>
 *   <li>含铅饼干 → 清掉 {@link RadiationManager#LEAD_COOKIE_DOSE_CLEAR} 点剂量</li>
 * </ul>
 *
 * <p>注入点用 HEAD 而不是 RETURN：方法跑完之后 {@code this}（牛奶桶）已经变成空桶了。
 */
@Mixin(ItemStack.class)
public class ConsumableMixin {
	@Inject(method = "finishUsingItem", at = @At("HEAD"))
	private void moreores$onConsumed(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> info) {
		ItemStack stack = (ItemStack) (Object) this;

		if (!(entity instanceof ServerPlayer player)) {
			return;
		}

		// 牛奶瓶和牛奶桶效果一致：剂量直接清零
		if (stack.is(Items.MILK_BUCKET) || stack.is(ModItems.MILK_BOTTLE)) {
			player.setAttached(ModAttachments.RADIATION_DOSE, 0);
		} else if (stack.is(ModItems.LEAD_COOKIE)) {
			RadiationManager.reduceDose(player, RadiationManager.LEAD_COOKIE_DOSE_CLEAR);
		}
	}
}
