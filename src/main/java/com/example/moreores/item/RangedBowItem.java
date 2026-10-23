package com.example.moreores.item;

import java.util.function.Predicate;

import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/**
 * 弓：把原版写死的两个东西做成可配置的。
 *
 * <ul>
 *   <li>射程（{@link #getDefaultProjectileRange()}，原版固定 15）</li>
 *   <li>可用的弹药 —— 原版只认三种原版箭，这里放开成任意 {@link ArrowItem}，
 *       否则我们自己加的箭根本射不出去</li>
 * </ul>
 *
 * <p>拉弓速度没有改：原版满力时间由 {@code BowItem#getPowerForTime} 这个静态方法决定，
 * 想改必须重写整个 {@code releaseUsing}，代价太大。
 */
public class RangedBowItem extends BowItem {
	private final int projectileRange;

	public RangedBowItem(Properties properties, int projectileRange) {
		super(properties);
		this.projectileRange = projectileRange;
	}

	@Override
	public int getDefaultProjectileRange() {
		return projectileRange;
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return stack -> stack.getItem() instanceof ArrowItem;
	}
}
