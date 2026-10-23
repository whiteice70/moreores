package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 矿物催化器：和生物催化器同一副外壳，但内部是岩浆池，复制石头 / 泥土 / 黑曜石等自然方块。
 *
 * <p>「催化物不消耗」的「快照 + 还原」逻辑直接复用
 * {@link BioCatalyzerBlock#getTicker} 的做法，只把方块实体类型换成矿物催化器。
 */
public class MineralCatalyzerBlock extends BioCatalyzerBlock {
	public MineralCatalyzerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MineralCatalyzerBlockEntity(pos, state);
	}

	@Override
	protected void openContainer(Level level, BlockPos pos, Player player) {
		if (level.getBlockEntity(pos) instanceof MineralCatalyzerBlockEntity catalyzer
				&& player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(catalyzer);
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}

		BlockEntityTicker<T> furnaceTick =
				createFurnaceTicker(level, type, ModBlockEntities.MINERAL_CATALYZER);

		return (lvl, pos, blockState, blockEntity) -> {
			if (!(blockEntity instanceof MineralCatalyzerBlockEntity catalyzer)) {
				return;
			}

			ItemStack catalyst = catalyzer.getCatalyst();
			furnaceTick.tick(lvl, pos, blockState, blockEntity);
			catalyzer.restoreCatalyst(catalyst);
		};
	}
}
