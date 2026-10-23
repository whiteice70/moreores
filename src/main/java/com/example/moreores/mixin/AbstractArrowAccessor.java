package com.example.moreores.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code AbstractArrow.firedFromWeapon}（射箭用的那把弓）是 private 且没
 * 有 setter，这里补一个，好让自定义箭在初始化时把它填上 —— 否则弓的附魔
 * （力量、火矢、穿刺）对射出的箭就不生效了。
 */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	@Accessor("firedFromWeapon")
	void setFiredFromWeapon(ItemStack weapon);
}
