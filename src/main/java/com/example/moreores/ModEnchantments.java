package com.example.moreores;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 自定义附魔的键。
 *
 * <p>26.3 的附魔是数据驱动的，定义在
 * {@code data/moreores/enchantment/steadfast.json}，
 * 这里只保留代码侧需要的 {@link ResourceKey}。
 */
public final class ModEnchantments {
	/** 坚守：死亡时带此附魔的物品不掉落。 */
	public static final ResourceKey<Enchantment> STEADFAST =
			ResourceKey.create(Registries.ENCHANTMENT, MoreOres.id("steadfast"));

	private ModEnchantments() {
	}

	/** 判断物品是否带有坚守。只看已附魔到物品上的，不看附魔书里的 stored_enchantments。 */
	public static boolean hasSteadfast(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
			if (enchantment.is(STEADFAST)) {
				return true;
			}
		}

		return false;
	}
}
