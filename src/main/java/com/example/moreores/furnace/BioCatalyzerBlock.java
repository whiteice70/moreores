package com.example.moreores.furnace;

import com.example.moreores.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 生物催化器：一块「玻璃罩住草坪」的钛制机器，把树苗 / 种子催化成木头 / 作物。
 *
 * <p>几何、朝向、比较器输出全部沿用 {@link AbstractFurnaceBlock}，
 * 差别都在 {@link BioCatalyzerBlockEntity} 里（只吃核子电池、燃料 10 倍速、产物速度不变）。
 *
 * <p><b>「催化物不消耗」在这里实现</b>，做法是<b>快照 + 还原</b>：
 * 每一 tick 先把催化物槽记下来，跑完父类那一套逻辑之后再放回去。
 * 父类 {@code serverTick} 在产物完成时会 {@code shrink(1)}，这一步会被原样抹掉。
 *
 * <p>为什么不用 Mixin 去改字节码：父类的消耗动作藏在 {@code serverTick} 深处，
 * 而这里**只需要包一层 ticker** 就能达到同样效果 ——
 * 逻辑留在自己的代码里、一眼能看懂，也不必跟原版字节码较劲。
 * 代价是每 tick 多一次 {@code copy()}（一个 {@code ItemStack} 对象，可以忽略）。
 */
public class BioCatalyzerBlock extends AbstractFurnaceBlock {
	public BioCatalyzerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BioCatalyzerBlockEntity(pos, state);
	}

	@Override
	protected void openContainer(Level level, BlockPos pos, Player player) {
		if (level.getBlockEntity(pos) instanceof BioCatalyzerBlockEntity catalyzer
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

		// 父类那个 ticker（就是普通熔炉的 serverTick）
		BlockEntityTicker<T> furnaceTick =
				createFurnaceTicker(level, type, ModBlockEntities.BIO_CATALYZER);

		return (lvl, pos, blockState, blockEntity) -> {
			if (!(blockEntity instanceof BioCatalyzerBlockEntity catalyzer)) {
				return;
			}

			// ① 记下催化物
			ItemStack catalyst = catalyzer.getCatalyst();
			// ② 正常跑熔炉逻辑（这一步可能会把催化物吃掉）
			furnaceTick.tick(lvl, pos, blockState, blockEntity);
			// ③ 还回去
			catalyzer.restoreCatalyst(catalyst);
		};
	}
}
