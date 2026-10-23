package com.example.moreores.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 矿物作物：种下的是**金属锭**，成熟收获出**金属锭**，中途铲掉只返还**粗矿**。
 *
 * <p>如果哪天要加宝石类作物（钻石、绿宝石这种没有「粗矿」形态的），
 * 把这个类抄一份、把「未成熟产物」直接设成宝石本身即可 —— 掉落表那边的
 * {@code match_block} 条件是一模一样的写法。
 *
 * <p>和原版作物最大的不同是**阶段数**：原版土豆/小麦是 8 个阶段（age 0~7），
 * 这里压到 4 个（age 0~3），少画一半贴图。所以必须覆写
 * {@link #getAgeProperty()}、{@link #getMaxAge()} 和 {@link #createBlockStateDefinition}。
 */
public class MineralCropBlock extends CropBlock {
	/** 4 个生长阶段（公开给发光配置用）。 */
	public static final IntegerProperty AGE_4_STAGES = BlockStateProperties.AGE_3;

	private static final int MAX_AGE = BlockStateProperties.MAX_AGE_3;

	/** 种植用的锭，也是「创造模式 Ctrl + 选取」拿到的物品。 */
	private final Item seed;

	public MineralCropBlock(BlockBehaviour.Properties properties, Item seed) {
		super(properties);
		this.seed = seed;
	}

	@Override
	protected IntegerProperty getAgeProperty() {
		return AGE_4_STAGES;
	}

	@Override
	public int getMaxAge() {
		return MAX_AGE;
	}

	/** 注意：**不要**调用 super，父类往状态里塞的是 0~7 的 AGE。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE_4_STAGES);
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return seed;
	}
}
