package com.example.moreores;

import com.example.moreores.entity.MetalArrow;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	/**
	 * 铅箭与铀箭共用同一个实体类型 —— 渲染时看箭身上带的物品来决定用哪张纹理，
	 * 这样只需要注册一次实体类型和渲染器。
	 *
	 * <p>尺寸 / 追踪范围照抄原版 {@code EntityType.ARROW}。
	 */
	public static final EntityType<MetalArrow> METAL_ARROW = register("metal_arrow",
			EntityType.Builder.<MetalArrow>of(MetalArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(20));

	private ModEntities() {
	}

	/** 触发本类的静态初始化，完成实体注册。 */
	public static void register() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, MoreOres.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}
}
