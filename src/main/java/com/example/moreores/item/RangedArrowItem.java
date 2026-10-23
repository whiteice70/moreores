package com.example.moreores.item;

import com.example.moreores.ModEntities;
import com.example.moreores.entity.MetalArrow;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 箭：把原版写死的基础伤害（2.0）做成可配置的，同时换成自己的箭实体，
 * 这样飞出去的是铅箭 / 铀箭的建模，而不是普通箭。
 *
 * <p>最终伤害还要叠上弓的力度和暴击，这里改的只是「箭本身有多重」。
 */
public class RangedArrowItem extends ArrowItem {
	private final double baseDamage;

	public RangedArrowItem(Properties properties, double baseDamage) {
		super(properties);
		this.baseDamage = baseDamage;
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, ItemStack weapon) {
		MetalArrow arrow = new MetalArrow(
				ModEntities.METAL_ARROW, level, shooter, ammo.copyWithCount(1), weapon);

		arrow.setBaseDamage(baseDamage);
		return arrow;
	}
}
