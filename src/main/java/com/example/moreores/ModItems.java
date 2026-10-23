package com.example.moreores;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public final class ModItems {
	/** 铀锭的燃料数值提供器（热值本体写在数据包里）。 */
	private static final ResourceKey<ContextIntProvider> URANIUM_FUEL =
			ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, MoreOres.id("cooking/time_uranium"));

	// ---------------- 铅 ----------------
	public static final Item RAW_LEAD = register("raw_lead", Item::new, new Item.Properties());
	public static final Item LEAD_INGOT = register("lead_ingot", Item::new, new Item.Properties());

	/** 铅粒：9 粒 ↔ 1 锭，也是工具 / 盔甲回炉的唯一产物。 */
	public static final Item LEAD_NUGGET = register("lead_nugget", Item::new, new Item.Properties());

	// ---------------- 铀 ----------------
	public static final Item RAW_URANIUM = register("raw_uranium", Item::new, new Item.Properties());

	/**
	 * 铀锭：热值 200000 tick（岩浆桶的 10 倍），既能在普通熔炉烧，也是高能熔炉的燃料。
	 *
	 * <p>26.3 的燃料是**组件化**的：靠 {@code cookingFuel} 挂一个
	 * {@code CONTEXT_INT_PROVIDER} 引用，数值本体写在数据包里
	 * （{@code data/moreores/context_int_provider/cooking/time_uranium.json}）。
	 *
	 * <p>⚠️ 这个组件**必须挂** —— 熔炉判定「这算不算燃料」看的就是它，
	 * 而不是 {@code getBurnDuration}。之前只在 Mixin 里改时长，结果铀锭
	 * 手动放进燃料槽会被拒（只有命令直接塞进容器才烧得起来）。
	 */
	public static final Item URANIUM_INGOT = register("uranium_ingot", Item::new,
			new Item.Properties().cookingFuel(URANIUM_FUEL));

	/** 铀粒：同铅粒。 */
	public static final Item URANIUM_NUGGET = register("uranium_nugget", Item::new, new Item.Properties());

	// ---------------- 钛 ----------------
	public static final Item RAW_TITANIUM = register("raw_titanium", Item::new, new Item.Properties());

	/**
	 * 钛锭<b>没有原版冶炼配方</b> —— 它的烧制配方挂在自定义的
	 * {@code moreores:high_energy_smelting} 类型下，只有高能熔炉认。
	 * 详见 {@code recipe/HighEnergySmeltingRecipe}。
	 */
	public static final Item TITANIUM_INGOT = register("titanium_ingot", Item::new, new Item.Properties());

	/** 钛粒：9 粒 ↔ 1 锭。工具 / 盔甲回炉**也只能走高能熔炉**，和锭一个规矩。 */
	public static final Item TITANIUM_NUGGET = register("titanium_nugget", Item::new, new Item.Properties());

	/**
	 * 钛矿碎片：**矿石那一级**的小单位，介于「矿石」和「粗矿」之间。
	 *
	 * <ul>
	 *   <li>9 碎片 ↔ 1 粗钛矿（普通合成，双向）</li>
	 *   <li>碎片 → 钛粒，**只能走高能熔炉**（和钛锭、工具回炉一个规矩）</li>
	 *   <li>钛作物的产物就是它</li>
	 * </ul>
	 *
	 * <p>为什么要单开一级：钛作物原来直接掉**钛粒**，而钛粒离锭只差一步，
	 * 等于作物一轮就快摸到成品；插一层「矿碎片」之后，
	 * 种地线的终点退回到「矿石级」，后面还得先合成粗矿、再烧成锭 ——
	 * 挖矿和种地两条线的**收益层级**就对上了。
	 *
	 * <p>贴图是**矿石的一角**：一块灰石头，里面嵌着三簇钛的金属亮斑 ——
	 * 矿石 = 石头 + 裹在里面的金属，这正是它和「粗钛」的区别
	 * （粗钛是六片亮银白的尖锐碎片，一点石头都没有）。
	 */
	public static final Item TITANIUM_ORE_FRAGMENT = register("titanium_ore_fragment",
			Item::new, new Item.Properties());

	// ---------------- 下界合金胚 ----------------
	/**
	 * 下界合金胚：烧制下界合金锭的**中间产物**。
	 *
	 * <p>贴图是玫瑰红的，但**名字里不写颜色** —— 和「钛锭」「铅锭」一个规矩，
	 * 名字只说是什么东西，外观交给贴图自己表达。
	 *
	 * <p>合成是「金 / 铅-铀-铅 / 铁」的三行 —— 把下界合金的两种原料（金、下界合金碎片）
	 * 换成本模组的金属体系：金锭打头、**铀块当芯**、铅锭包壳、铁锭收尾。
	 *
	 * <p><b>芯是一整块铀块</b>（= 9 个铀锭），不是一颗铀锭。下界合金是终局材料，
	 * 中间产物也该有对应的分量 —— 一格铀锭太轻了。于是一块胚要
	 * 3 金 + 2 铅 + 9 铀 + 3 铁 = **17 个锭**的料。
	 *
	 * <p>曾经还有一条「9 个粒换 1 块」的路子（换算下来是一个锭一块胚，
	 * 和锭那条等价），现已删除 —— 留着等于给粒开了条捷径，
	 * 和下界合金该有的成本不相称。
	 *
	 * <p>烧制只认 {@code moreores:high_energy_smelting}，所以普通熔炉 / 高炉都无效 ——
	 * 和钛一个规矩，见 {@link #TITANIUM_INGOT}。
	 */
	public static final Item NETHERITE_BLANK = register("netherite_blank", Item::new,
			new Item.Properties());

	// ---------------- 宝石碎片 ----------------
	/**
	 * 宝石碎片：9 片 ↔ 1 颗宝石。
	 *
	 * <p>给宝石作物配一个「小单位」—— 和金属那边的<b>粒</b>是同一个角色。
	 * 作物成熟后不再直接掉成品宝石，一次只产一小份。
	 *
	 * <p>贴图**一物一脸**，四张不共用轮廓（原来四张是同一个斜棱柱换了四次色，
	 * 数不透明像素是 89 / 89 / 89 / 89，分毫不差）：
	 *
	 * <ul>
	 *   <li>钻石碎片 —— 倒三角，钻石的<b>亭部</b>，尖被敲断</li>
	 *   <li>绿宝石碎片 —— <b>阶梯切面</b>（祖母绿切工）：外圈 / 台阶 / 台面三级</li>
	 *   <li>青金石碎片 —— 不规则碎块撒斑点（青金石本来就是斑驳的石头）</li>
	 *   <li>石英碎片 —— <b>细长针状</b>，左上缘高光、右下缘背光</li>
	 * </ul>
	 *
	 * <p>四张图的实心像素数是 106 / 116 / 113 / 68，确实各不相同。
	 */
	public static final Item DIAMOND_SHARD = register("diamond_shard", Item::new,
			new Item.Properties());
	public static final Item EMERALD_SHARD = register("emerald_shard", Item::new,
			new Item.Properties());
	public static final Item LAPIS_SHARD = register("lapis_shard", Item::new,
			new Item.Properties());
	public static final Item QUARTZ_SHARD = register("quartz_shard", Item::new,
			new Item.Properties());

	// ---------------- 自然护符 ----------------
	/**
	 * 自然护符：**放在身上**（背包 / 快捷栏 / 副手）就生效。
	 *
	 * <p>效果：破坏草时如果本来会掉小麦种子，有 **20%** 的概率
	 * **转而**掉落随机一种矿物种子。
	 *
	 * <p>「转而」是替换而不是附赠 —— 那一格小麦种子换成矿物种子，
	 * 不会两个一起掉。实现上靠改掉落表（{@code NatureTalismanHandler}），
	 * 不是先让它掉出来再补一个。
	 *
	 * <p><b>效果不叠加</b>：判定只看"身上有没有"，不看有几个，
	 * 所以带十个护符也还是 20%。理由是这个效果不该成为可量产的机器 ——
	 * 真要提概率就该改配方成本，而不是靠堆数量。
	 *
	 * <p><b>不可堆叠</b>（{@code stacksTo(1)}）：堆叠会让"带一个"和
	 * "带一组"在判定上无法区分，既然效果不叠加，索性不让它堆。
	 *
	 * <p>贴图是**一片挂在挂环上的叶子**（原来是一颗绿宝石换色）——
	 * 护身符要能看出是"挂在身上的东西"；而且 16×16 上
	 * 「圆牌里再刻一片小叶子」只会糊成一个点，索性把形状本身做成叶子。
	 */
	public static final Item NATURE_TALISMAN = register("nature_talisman", Item::new,
			new Item.Properties().stacksTo(1));

	// ---------------- 硫磺 ----------------
	/** 硫磺粉：由原版硫磺方块分解得到，也是合成火药的关键原料。 */
	public static final Item SULFUR_POWDER = register("sulfur_powder", Item::new, new Item.Properties());

	// ---------------- 食物 ----------------
	/** 含铅饼干：饱食度同原版曲奇，但饱食度满时也能吃，每块清除 200 点辐射剂量。 */
	public static final Item LEAD_COOKIE = register("lead_cookie", Item::new,
			new Item.Properties().food(ModFoods.LEAD_COOKIE));

	// ---------------- 牛奶瓶 ----------------
	/**
	 * 牛奶瓶：喝下去的效果和牛奶桶完全一样（清除全部状态效果 + 辐射剂量清零），
	 * 但有三个差别：
	 *
	 * <ul>
	 *   <li>能堆叠 <b>16</b> 个（牛奶桶只能 1 个）</li>
	 *   <li>喝完返还 <b>玻璃瓶</b>（{@code usingConvertsTo}）</li>
	 *   <li>合成时返还 <b>桶</b>（{@code craftRemainder}）</li>
	 * </ul>
	 *
	 * <p>「效果和牛奶相同」是靠直接复用原版的 {@link Consumables#MILK_BUCKET} 实现的，
	 * 而不是自己拼一个清除效果 —— 这样以后原版改牛奶行为我们也跟着变。
	 */
	public static final Item MILK_BOTTLE = register("milk_bottle", Item::new,
			new Item.Properties()
					.stacksTo(16)
					.usingConvertsTo(Items.GLASS_BOTTLE)
					.craftRemainder(Items.BUCKET)
					.component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET));

	// ---------------- 盖革计数器 ----------------
	/**
	 * 盖革计数器：拿在主手或副手时，右上角实时显示辐射剂量与强度档位。
	 *
	 * <p>物品本身没有任何逻辑 —— 显示完全由客户端 HUD 负责
	 * （{@code client/hud/GeigerHud}），它每帧检查玩家手上有没有这件东西。
	 */
	public static final Item GEIGER_COUNTER = register("geiger_counter", Item::new,
			new Item.Properties().stacksTo(1));

	// ---------------- 核子电池 ----------------
	/**
	 * 核子电池：铅壳 + 铜极 + 铀芯。
	 *
	 * <p>合成是 3×3：左右两列铅锭、上下各一个铜锭、正中一颗铀锭 ——
	 * 铅壳负责屏蔽，铜极负责导电，铀芯供能。
	 */
	public static final Item NUCLEAR_BATTERY = register("nuclear_battery", Item::new,
			new Item.Properties().cookingFuel(URANIUM_FUEL));

	/**
	 * 能源耗尽的核子电池：核子电池当燃料烧完之后剩下的壳。
	 *
	 * <p>它**不是**靠 {@code craftRemainder} 实现的 —— 那个组件在熔炉里同样生效，
	 * 会和「副产物槽」那条路重复产出两个。这里统一走
	 * {@code mixin/FurnaceFuelMixin} 的副产物逻辑，产出物落到催化器的第 4 格。
	 *
	 * <p>可以分解回 <b>4 个铅锭</b>（见 {@code data/moreores/recipe/depleted_*.json}）。
	 */
	public static final Item DEPLETED_NUCLEAR_BATTERY = register("depleted_nuclear_battery",
			Item::new, new Item.Properties());

	// ---------------- 刷怪蛋 ----------------
	private ModItems() {
	}

	/** 触发本类的静态初始化，完成全部物品注册（必须在 ModBlocks 之后）。 */
	public static void register() {
		registerBlockItem("lead_ore", ModBlocks.LEAD_ORE);
		registerBlockItem("deepslate_lead_ore", ModBlocks.DEEPSLATE_LEAD_ORE);
		registerBlockItem("raw_lead_block", ModBlocks.RAW_LEAD_BLOCK);
		registerBlockItem("lead_block", ModBlocks.LEAD_BLOCK);

		registerBlockItem("uranium_ore", ModBlocks.URANIUM_ORE);
		registerBlockItem("deepslate_uranium_ore", ModBlocks.DEEPSLATE_URANIUM_ORE);
		registerBlockItem("raw_uranium_block", ModBlocks.RAW_URANIUM_BLOCK);
		registerBlockItem("uranium_block", ModBlocks.URANIUM_BLOCK);
		registerBlockItem("high_energy_furnace", ModBlocks.HIGH_ENERGY_FURNACE);
		registerBlockItem("bio_catalyzer", ModBlocks.BIO_CATALYZER);

		registerBlockItem("titanium_ore", ModBlocks.TITANIUM_ORE);
		registerBlockItem("deepslate_titanium_ore", ModBlocks.DEEPSLATE_TITANIUM_ORE);
		registerBlockItem("raw_titanium_block", ModBlocks.RAW_TITANIUM_BLOCK);
		registerBlockItem("titanium_block", ModBlocks.TITANIUM_BLOCK);
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(settings.setId(key)));
	}

	private static Item registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MoreOres.id(name));
		Item item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
