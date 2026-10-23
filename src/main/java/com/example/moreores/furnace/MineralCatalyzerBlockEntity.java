package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;
import com.example.moreores.recipe.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 矿物催化器的方块实体。
 *
 * <p>除了配方桶（{@link ModRecipeTypes#MINERAL_CATALYZING}）、方块实体类型和界面标题，
 * 其余行为全部继承 {@link BioCatalyzerBlockEntity}：只吃核子电池、燃料 10 倍速、
 * 催化物不消耗、副产物槽放耗尽的核子电池。
 */
public class MineralCatalyzerBlockEntity extends BioCatalyzerBlockEntity {
	public MineralCatalyzerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MINERAL_CATALYZER, pos, state,
				ModRecipeTypes.MINERAL_CATALYZING);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.moreores.mineral_catalyzer");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new MineralCatalyzerMenu(containerId, inventory, this, this.dataAccess());
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new MineralCatalyzerMenu(containerId, inventory, this, this.dataAccess());
	}
}
