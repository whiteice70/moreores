package com.example.moreores.client.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * 辐射粒子：荧光绿的小尘点，缓慢上浮 + 轻微漂移，靠贴图帧切换做出「闪烁」感。
 *
 * <p>26.3 里旧的 {@code TextureSheetParticle} 已经改名成 {@link SingleQuadParticle}，
 * 而且 {@code getLayer()} 变成抽象方法，子类必须实现。
 */
public class RadiationParticle extends SingleQuadParticle {
	/** 主色调：偏黄的荧光绿，和铀块、辐射效果图标保持一致。 */
	private static final float RED = 0.62F;
	private static final float GREEN = 0.95F;
	private static final float BLUE = 0.35F;

	private final SpriteSet sprites;

	protected RadiationParticle(ClientLevel level, double x, double y, double z,
			double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
		super(level, x, y, z, velocityX, velocityY, velocityZ, sprites.first());

		this.sprites = sprites;

		this.lifetime = 24 + this.random.nextInt(16);
		this.quadSize = 0.10F + this.random.nextFloat() * 0.06F;
		this.gravity = -0.004F;                 // 负数 = 向上飘
		this.friction = 0.92F;
		this.hasPhysics = false;

		this.setColor(RED, GREEN, BLUE);
		this.setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	/** 粒子工厂：Fabric 会把该粒子的 SpriteSet 传进来。 */
	public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level,
				double x, double y, double z, double velocityX, double velocityY, double velocityZ,
				RandomSource random) {
			return new RadiationParticle(level, x, y, z,
					velocityX, velocityY, velocityZ, this.sprites);
		}
	}
}
