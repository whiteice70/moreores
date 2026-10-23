package com.example.moreores.effect;

import com.example.moreores.ModParticles;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 辐射：按等级周期性造成魔法伤害。
 *
 * <p>等级 I / II / III 分别每 40 / 25 / 12 tick 各造成 1 点伤害，
 * 也就是每秒 0.5 / 0.8 / 1.67 点。III 级附加的挖掘疲劳由
 * {@link com.example.moreores.radiation.RadiationManager} 施加。
 *
 * <p>伤害用魔法类型：和药水伤害一样会被保护附魔减免，和平模式也不会生效。
 */
public class RadiationEffect extends MobEffect {
	/**
	 * 第三个参数决定生物身上飘什么粒子 —— 不传的话默认是原版药水那种气泡，
	 * 这里换成我们自己的荧光绿辐射尘（{@link ModParticles#RADIATION}）。
	 */
	public RadiationEffect(MobEffectCategory category, int color) {
		super(category, color, ModParticles.RADIATION);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % damageInterval(amplifier) == 0;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		entity.hurtServer(level, level.damageSources().magic(), 1.0F);
		return true;
	}

	private static int damageInterval(int amplifier) {
		return switch (amplifier) {
			case 0 -> 40;
			case 1 -> 25;
			default -> 12;
		};
	}
}
