package com.example.moreores.entity;

import com.example.moreores.mixin.AbstractArrowAccessor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 金属箭（铅 / 铀共用）。
 *
 * <p>存在的唯一理由：飞出去的箭要换成自己的建模。箭实体长什么样由 {@code EntityType}
 * 决定，而原版 {@code Arrow} 的公开带射手构造里写死了 {@code EntityTypes.ARROW}，
 * 所以必须换实体类型 + 在客户端注册换纹理的渲染器。
 *
 * <p>麻烦点在于初始化只能自己复现：{@code AbstractArrow} 那个带 {@code EntityType}
 * 的 protected 构造没法直接调（Java 不允许跳过父类 {@code Arrow} 的构造），所以这里
 * 用 {@code Arrow(EntityType, Level)} 起头，再按原版构造的字节码逻辑补上位置、主人、
 * 拾取物与「发射武器」。
 */
public class MetalArrow extends Arrow {
	public MetalArrow(EntityType<? extends MetalArrow> type, Level level) {
		super(type, level);
	}

	public MetalArrow(EntityType<? extends MetalArrow> type, Level level, LivingEntity shooter,
			ItemStack pickupItemStack, ItemStack weaponItemStack) {
		super(type, level);

		this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		this.setOwner(shooter);
		this.setPickupItemStack(pickupItemStack);

		// firedFromWeapon 是 private，只能走 accessor；不设置的话附魔会失效
		if (!weaponItemStack.isEmpty()) {
			((AbstractArrowAccessor) this).setFiredFromWeapon(weaponItemStack.copy());
		}
	}
}
