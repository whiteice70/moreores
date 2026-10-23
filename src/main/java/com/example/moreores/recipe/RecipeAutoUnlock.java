package com.example.moreores.recipe;

import com.example.moreores.MoreOres;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import net.minecraft.util.context.ContextMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 让本模组<b>所有</b>新增合成表在玩家「拿到配方里任意一样物品」时自动解锁。
 *
 * <p>原版的合成书只会在你亲手合成过、或某些内置触发条件下才把配方标记为已知；
 * 本模组配方很多（冶练 / 高炉 / 锻造 / 合成 / 切石），玩家拿不到对应物品前在书里看不到，
 * 体验上像「配方失踪」。这里反着来：只要背包里出现了配方涉及的东西
 * （产物，或任意一种原料），就把对应配方解锁。
 *
 * <p>实现：服务端每个 tick 扫一遍每个玩家持有物品，和上次见过的物品集合做差，
 * 对新增的物品查「物品 → 配方」索引，把还没解锁的喂给 {@link ServerRecipeBook#addRecipes}。
 * 索引在第一次服务端 tick（数据包已加载）时从 {@link RecipeManager} 现建，只建一次。
 *
 * <p>索引来自配方的 {@link Recipe#display()} 数据 —— 产物走 {@code result()}，
 * 原料按显示类型分别取（合成取 {@code ingredients()}、熔炼取 {@code ingredient()}、
 * 锻造取 模板/底座/附加、切石取 {@code input()}）。本模组配方不含标签原料，
 * 所以直接用 {@link SlotDisplay#resolveForStacks(ContextMap)} + {@link ContextMap#EMPTY} 即可，
 * 不需要额外解析标签。
 */
public final class RecipeAutoUnlock {
	private RecipeAutoUnlock() {
	}

	/** 物品 → 涉及它的本模组配方。 */
	private static final Map<Item, List<RecipeHolder<?>>> INDEX = new HashMap<>();
	/** 玩家已见过的物品集合（弱引用键，玩家退出自动回收）。 */
	private static final Map<ServerPlayer, Set<Item>> SEEN = new WeakHashMap<>();
	/** 索引是否已建好（数据包加载完成）。 */
	private static boolean indexed = false;

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(RecipeAutoUnlock::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		if (!indexed) {
			buildIndex(server);
			// 数据包还没就绪时 getRecipes 可能不含本模组配方，等下一 tick 再试
			if (!INDEX.isEmpty()) {
				indexed = true;
			}
		}
		if (!indexed) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			scan(player);
		}
	}

	private static void buildIndex(MinecraftServer server) {
		INDEX.clear();
		RecipeManager recipeManager = server.getRecipeManager();

		for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
			Identifier location = holder.id().identifier();
			if (!location.getNamespace().equals(MoreOres.MOD_ID)) {
				continue;
			}

			Set<Item> items = new HashSet<>();
			for (RecipeDisplay display : holder.value().display()) {
				collect(display.result(), items);
				if (display instanceof ShapedCraftingRecipeDisplay shaped) {
					for (SlotDisplay slot : shaped.ingredients()) {
						collect(slot, items);
					}
				} else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
					for (SlotDisplay slot : shapeless.ingredients()) {
						collect(slot, items);
					}
				} else if (display instanceof FurnaceRecipeDisplay furnace) {
					collect(furnace.ingredient(), items);
				} else if (display instanceof SmithingRecipeDisplay smithing) {
					collect(smithing.template(), items);
					collect(smithing.base(), items);
					collect(smithing.addition(), items);
				} else if (display instanceof StonecutterRecipeDisplay stonecutter) {
					collect(stonecutter.input(), items);
				}
			}

			for (Item item : items) {
				INDEX.computeIfAbsent(item, k -> new ArrayList<>()).add(holder);
			}
		}

		int recipes = INDEX.values().stream().mapToInt(List::size).sum();
		MoreOres.LOGGER.info("RecipeAutoUnlock index built: {} recipe links across {} items", recipes, INDEX.size());
	}

	private static void collect(SlotDisplay slot, Set<Item> items) {
		for (ItemStack stack : slot.resolveForStacks(ContextMap.EMPTY)) {
			items.add(stack.getItem());
		}
	}

	private static void scan(ServerPlayer player) {
		Set<Item> current = new HashSet<>();
		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (!stack.isEmpty()) {
				current.add(stack.getItem());
			}
		}
		for (EquipmentSlot slot : new EquipmentSlot[] {
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
				EquipmentSlot.FEET, EquipmentSlot.OFFHAND}) {
			ItemStack stack = player.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				current.add(stack.getItem());
			}
		}

		Set<Item> seen = SEEN.computeIfAbsent(player, p -> new HashSet<>());
		ServerRecipeBook book = player.getRecipeBook();

		List<RecipeHolder<?>> toUnlock = new ArrayList<>();
		for (Item item : current) {
			if (seen.contains(item)) {
				continue;
			}
			List<RecipeHolder<?>> holders = INDEX.get(item);
			if (holders == null) {
				continue;
			}
			for (RecipeHolder<?> holder : holders) {
				if (!book.contains(holder.id())) {
					toUnlock.add(holder);
				}
			}
		}

		if (!toUnlock.isEmpty()) {
			book.addRecipes(toUnlock, player);
		}
		seen.addAll(current);
	}
}
