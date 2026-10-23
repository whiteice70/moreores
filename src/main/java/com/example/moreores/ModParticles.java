package com.example.moreores;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * 模组自定义粒子。
 *
 * <p>辐射粒子：不冒原版药水那种「气泡」，而是一小撮会缓慢上浮的荧光绿尘点，
 * 纹理在 {@code assets/moreores/textures/particle/radiation_*.png}。
 *
 * <p>注意 {@code SimpleParticleType} 的构造函数是 protected，原版不让外模组直接 new，
 * 所以走 Fabric 的 {@link FabricParticleTypes#simple()} 工厂。
 */
public final class ModParticles {
	public static final SimpleParticleType RADIATION = Registry.register(
			BuiltInRegistries.PARTICLE_TYPE,
			MoreOres.id("radiation"),
			FabricParticleTypes.simple());

	private ModParticles() {
	}

	/** 触发本类的静态初始化，完成粒子类型注册（必须在 ModEffects 之前）。 */
	public static void register() {
	}
}
