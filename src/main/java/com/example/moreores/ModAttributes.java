package com.example.moreores;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * 本模组自定义的属性。
 *
 * <p>目前只有一个：<b>辐射穿透率</b>。
 *
 * <p>为什么要专门造一个属性 —— 因为抗辐射原本是 {@code RadiationManager} 里
 * 自己算的隐藏机制，**游戏根本不知道它存在**，所以物品提示里那一栏
 * （「穿在身上时：」）根本列不出它来。给它一个真属性，护甲就能像挂移速那样
 * 挂一条修饰符，提示里自然会多出一行。
 *
 * <p>⚠️ <b>提示里显不显示百分号，取决于「运算方式」而不是属性本身。</b>
 * 原版那套 lang 键是按运算方式的序号分的：
 * <pre>
 *   attribute.modifier.take.0   "-%s %s"       ADD_VALUE             → 平值
 *   attribute.modifier.take.1   "-%s%% %s"     ADD_MULTIPLIED_BASE   → 百分比
 *   attribute.modifier.take.2   "-%s%% %s"     ADD_MULTIPLIED_TOTAL  → 百分比
 * </pre>
 * 所以「-5% 速度」之所以带 %，只是因为移速用的 {@code ADD_MULTIPLIED_BASE}。
 * 这里照抄同一种运算方式，就能得到同样风格的「-25% 辐射」。
 *
 * <p>默认值取 <b>1.0</b>（"100% 的辐射都会穿透"），每件铅甲 -0.25，
 * 四件正好归零 —— 也就是完全不累积。默认值必须是 1 而不是 0，
 * 因为 {@code ADD_MULTIPLIED_BASE} 加的是「默认值 × 系数」，
 * 默认值是 0 的话乘出来永远是 0，修饰符等于没挂。
 */
public final class ModAttributes {
	/** 辐射穿透率：1.0 = 完全穿透，0.0 = 完全屏蔽。 */
	public static final Holder<Attribute> RADIATION_SHIELDING = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE,
			MoreOres.id("radiation_shielding"),
			new RangedAttribute("attribute.name.moreores.radiation_shielding",
					1.0D, 0.0D, 1.0D)
					.setSyncable(true)
					.setSentiment(Attribute.Sentiment.NEGATIVE));

	private ModAttributes() {
	}

	/** 触发本类的静态初始化，完成属性注册。 */
	public static void register() {
	}
}
