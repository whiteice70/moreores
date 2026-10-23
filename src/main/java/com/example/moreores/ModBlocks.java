package com.example.moreores;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.example.moreores.furnace.HighEnergyFurnaceBlock;
import com.example.moreores.furnace.BioCatalyzerBlock;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * 铀与铅相关的全部方块。
 *
 * <p>26.3 里 Block 的构造仍然是 {@code new Block(BlockBehaviour.Properties)}，
 * 但注册前必须通过 {@code Properties#setId} 写入自己的 ResourceKey。
 */
public final class ModBlocks {
	// ---------------- 高能熔炉 ----------------
	/** 高能熔炉：烧制速度与燃料消耗速度都是普通熔炉的 10 倍，另带一个副产物槽。 */
	public static final Block HIGH_ENERGY_FURNACE = register("high_energy_furnace",
			HighEnergyFurnaceBlock::new,
			BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops()
					.strength(3.5F)
					.sound(SoundType.STONE)
					// 燃烧时发光，和原版熔炉一致
					.lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

	// ---------------- 生物催化器 ----------------
	/**
	 * 生物催化器：玻璃罩住草坪的钛制机器，把树苗 / 种子催化成木头 / 作物。
	 *
	 * <p>三条和熔炉不同的规矩：<b>只吃核子电池</b>、<b>燃料 10 倍速而产物速度不变</b>、
	 * <b>催化物不消耗</b>（详见 {@code BioCatalyzerBlockEntity}）。
	 *
	 * <p>{@code noOcclusion} + {@code notSolid} 是给玻璃罩用的：模型只占下半部分的
	 * 一圈玻璃，不这么设的话贴着它放的火把会被判定成「埋在方块里」。
	 */
	public static final Block BIO_CATALYZER = register("bio_catalyzer",
			BioCatalyzerBlock::new,
			BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops()
					.strength(3.5F)
					.sound(SoundType.GLASS)
					.noOcclusion()
					.lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

	// ---------------- 铅：浅层常见金属 ----------------
	public static final Block LEAD_ORE = register("lead_ore",
			settings -> new DropExperienceBlock(UniformInt.of(0, 0), settings),
			ore(MapColor.STONE));

	public static final Block DEEPSLATE_LEAD_ORE = register("deepslate_lead_ore",
			settings -> new DropExperienceBlock(UniformInt.of(0, 0), settings),
			deepslateOre(MapColor.DEEPSLATE));

	public static final Block RAW_LEAD_BLOCK = register("raw_lead_block",
			Block::new,
			rawBlock(MapColor.STONE));

	public static final Block LEAD_BLOCK = register("lead_block",
			Block::new,
			metalBlock(MapColor.METAL, 0));

	// ---------------- 铀：深层稀有，方块自发绿光 ----------------
	// 发光等级：矿石 8 / 粗铀块 9 / 铀块 12（原版火把 14、萤石 15）
	public static final Block URANIUM_ORE = register("uranium_ore",
			settings -> new DropExperienceBlock(UniformInt.of(3, 7), settings),
			ore(MapColor.STONE).lightLevel(state -> 8));

	public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore",
			settings -> new DropExperienceBlock(UniformInt.of(3, 7), settings),
			deepslateOre(MapColor.DEEPSLATE).lightLevel(state -> 8));

	public static final Block RAW_URANIUM_BLOCK = register("raw_uranium_block",
			Block::new,
			rawBlock(MapColor.STONE).lightLevel(state -> 9));

	public static final Block URANIUM_BLOCK = register("uranium_block",
			Block::new,
			metalBlock(MapColor.METAL, 12));

	// ---------------- 钛：分布照抄煤矿，硬度对齐铁系 ----------------
	// 世界生成完全照搬原版煤矿（两条 placed feature，详见 README §9），
	// 方块属性这边只是「和铁矿一个级别」：同样要正确工具才能掉落。
	public static final Block TITANIUM_ORE = register("titanium_ore",
			settings -> new DropExperienceBlock(UniformInt.of(0, 0), settings),
			ore(MapColor.STONE));

	public static final Block DEEPSLATE_TITANIUM_ORE = register("deepslate_titanium_ore",
			settings -> new DropExperienceBlock(UniformInt.of(0, 0), settings),
			deepslateOre(MapColor.DEEPSLATE));

	public static final Block RAW_TITANIUM_BLOCK = register("raw_titanium_block",
			Block::new,
			rawBlock(MapColor.STONE));

	public static final Block TITANIUM_BLOCK = register("titanium_block",
			Block::new,
			metalBlock(MapColor.METAL, 0));

	private ModBlocks() {
	}

	/** 触发本类的静态初始化，完成全部方块注册。 */
	public static void register() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties settings) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, MoreOres.id(name));
		Block block = factory.apply(settings.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static BlockBehaviour.Properties ore(MapColor color) {
		return BlockBehaviour.Properties.of()
				.mapColor(color)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.0F, 3.0F)
				.sound(SoundType.STONE);
	}

	private static BlockBehaviour.Properties deepslateOre(MapColor color) {
		return BlockBehaviour.Properties.of()
				.mapColor(color)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(4.5F, 3.0F)
				.sound(SoundType.DEEPSLATE);
	}

	/** 粗矿块：手感和原版铁矿块一致。 */
	private static BlockBehaviour.Properties rawBlock(MapColor color) {
		return BlockBehaviour.Properties.of()
				.mapColor(color)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 6.0F)
				.sound(SoundType.STONE);
	}

	/** 金属块：手感和原版铁块一致，可用 lightLevel 附带发光。 */
	private static BlockBehaviour.Properties metalBlock(MapColor color, int lightLevel) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.mapColor(color)
				.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 6.0F)
				.sound(SoundType.METAL);

		return lightLevel > 0 ? properties.lightLevel(state -> lightLevel) : properties;
	}
}
