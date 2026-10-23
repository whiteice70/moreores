package com.example.moreores.effect;

import com.example.moreores.MoreOres;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ModEffects {
	/** 辐射：有害效果，颜色用荧光绿。图标见 assets/moreores/textures/mob_effect/radiation.png。 */
	public static final Holder<MobEffect> RADIATION = Registry.registerForHolder(
			BuiltInRegistries.MOB_EFFECT,
			MoreOres.id("radiation"),
			new RadiationEffect(MobEffectCategory.HARMFUL, 0x9FE870));

	private ModEffects() {
	}

	public static void register() {
	}
}
