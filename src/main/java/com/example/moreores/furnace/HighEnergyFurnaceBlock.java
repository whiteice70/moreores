package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 高能熔炉：普通熔炉 + 一圈铅块合成。
 *
 * <p>几何、朝向状态、比较器输出、掉落物全部沿用 {@link AbstractFurnaceBlock}，
 * 只有三处不同（都在 {@link HighEnergyFurnaceBlockEntity} 里）：
 * 烧制速度 ×10、燃料消耗速度 ×10、多一个副产物槽。
 */
public class HighEnergyFurnaceBlock extends AbstractFurnaceBlock {
	public HighEnergyFurnaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HighEnergyFurnaceBlockEntity(pos, state);
	}

	@Override
	protected void openContainer(Level level, BlockPos pos, Player player) {
		if (level.getBlockEntity(pos) instanceof HighEnergyFurnaceBlockEntity furnace
				&& player instanceof ServerPlayer serverPlayer) {
			// 26.3 的 openMenu 只剩这一个重载了（不再需要把 BlockPos 编进附加数据）：
			// 物品靠槽位同步推给客户端，进度条靠 ContainerData 同步。
			serverPlayer.openMenu(furnace);
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		// 服务端才跑逻辑，和原版熔炉一致
		return level.isClientSide() ? null
				: createFurnaceTicker(level, type, ModBlockEntities.HIGH_ENERGY_FURNACE);
	}

	/** 让方块实体能被活塞/漏斗等按朝向访问（沿用父类规则即可）。 */
	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
}
