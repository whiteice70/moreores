package com.example.moreores;

import java.util.Set;

import com.example.moreores.furnace.HighEnergyFurnaceBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * 模组的方块实体类型。
 *
 * <p>注意 26.3 的 {@link BlockEntityType} **没有 Builder 了**，
 * 直接 {@code new BlockEntityType(工厂, Set<方块>)} 即可（旧版是 {@code Builder.of(...)}）。
 */
public final class ModBlockEntities {
	public static final BlockEntityType<HighEnergyFurnaceBlockEntity> HIGH_ENERGY_FURNACE =
			Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
					ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, MoreOres.id("high_energy_furnace")),
					new BlockEntityType<>(HighEnergyFurnaceBlockEntity::new,
							Set.of(ModBlocks.HIGH_ENERGY_FURNACE)));

	public static final BlockEntityType<com.example.moreores.furnace.BioCatalyzerBlockEntity>
			BIO_CATALYZER =
			Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
					ResourceKey.create(Registries.BLOCK_ENTITY_TYPE,
							MoreOres.id("bio_catalyzer")),
					new BlockEntityType<>(
							com.example.moreores.furnace.BioCatalyzerBlockEntity::new,
							Set.of(ModBlocks.BIO_CATALYZER)));

	public static final BlockEntityType<com.example.moreores.furnace.MineralCatalyzerBlockEntity>
			MINERAL_CATALYZER =
			Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
					ResourceKey.create(Registries.BLOCK_ENTITY_TYPE,
							MoreOres.id("mineral_catalyzer")),
					new BlockEntityType<>(
							com.example.moreores.furnace.MineralCatalyzerBlockEntity::new,
							Set.of(ModBlocks.MINERAL_CATALYZER)));

	private ModBlockEntities() {
	}

	/** 触发本类的静态初始化，完成方块实体注册（必须在 ModBlocks 之后）。 */
	public static void register() {
	}
}
