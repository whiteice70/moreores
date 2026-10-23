package com.example.moreores.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 钛作物：**长得比别的作物快** —— 这是它相对铅 / 铀作物的特色。
 *
 * <p>为什么是「快」：本模组里钛的定位一直是「**轻**」——
 * 每件盔甲 +5% 移速、每件工具攻速 +0.4，都是这个字。作物这边就顺着写成「轻快、长得快」。
 *
 * <p>做法上**没有重写生长逻辑**，只是把父类那一次「随机刻掷骰子」多掷一遍：
 * 原版 {@code CropBlock.randomTick} 每次以 {@code 1 / (25 / 生长速度 + 1)} 的概率长一级，
 * 掷两次就大约翻倍。光照检测、骨粉、掉落这些全部照旧走父类，不会漏掉任何规则。
 *
 * <p>另一个特色（**反光**）不在这里，而是方块属性上的
 * {@code emissiveRendering} —— 见 {@code ModCrops.TITANIUM_CROP}：
 * 它的纹理不随环境光变暗，暗处也银白发亮（但**不照亮周围**，
 * 和「会照亮环境」的铀作物正好形成对照）。
 */
public final class TitaniumCropBlock extends MineralCropBlock {
	/** 额外多掷几次生长骰子（1 = 总共掷 2 次，约等于生长速度翻倍）。 */
	private static final int EXTRA_GROWTH_ROLLS = 1;

	public TitaniumCropBlock(BlockBehaviour.Properties properties, Item seed) {
		super(properties, seed);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos,
			RandomSource random) {
		super.randomTick(state, level, pos, random);

		// ⚠️ 多掷的这一遍要**重新取一次方块状态**：上一掷可能已经让它长了一级，
		// 拿传进来的旧 state 再算会算出同样的年龄（无害，但等于白掷）。
		for (int i = 0; i < EXTRA_GROWTH_ROLLS; i++) {
			super.randomTick(level.getBlockState(pos), level, pos, random);
		}
	}
}
