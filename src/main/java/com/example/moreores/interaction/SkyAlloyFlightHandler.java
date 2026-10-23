package com.example.moreores.interaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.moreores.ModArmor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * 天空合金全套效果：获得和创造模式一样的飞行能力。
 *
 * <p>判定只看四个护甲槽是否都是天空合金部件。齐全时把 {@code mayfly} 打开，
 * 玩家就可以像创造模式一样双击跳跃起飞；脱下任意一件就收回。
 *
 * <p>用 UUID 记录「这次飞行是机器给的」，避免误删创造 / 旁观模式或其它模组
 * 提供的飞行能力；创造和旁观玩家本来就能飞，不插手。
 */
public final class SkyAlloyFlightHandler {
	/** 由天空合金套授予飞行能力的玩家。 */
	private static final Set<UUID> GRANTED = new HashSet<>();

	private SkyAlloyFlightHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(SkyAlloyFlightHandler::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean fullSet = hasFullSet(player);
			boolean nativeFlight = player.isCreative() || player.isSpectator();

			if (fullSet) {
				if (!player.getAbilities().mayfly && !nativeFlight) {
					player.getAbilities().mayfly = true;
					player.onUpdateAbilities();
					GRANTED.add(player.getUUID());
				}
				continue;
			}

			if (GRANTED.remove(player.getUUID()) && !nativeFlight) {
				player.getAbilities().mayfly = false;
				player.getAbilities().flying = false;
				player.onUpdateAbilities();
			}
		}
	}

	private static boolean hasFullSet(ServerPlayer player) {
		return isSkyAlloy(player.getItemBySlot(EquipmentSlot.HEAD), ModArmor.SKY_ALLOY_HELMET)
				&& isSkyAlloy(player.getItemBySlot(EquipmentSlot.CHEST), ModArmor.SKY_ALLOY_CHESTPLATE)
				&& isSkyAlloy(player.getItemBySlot(EquipmentSlot.LEGS), ModArmor.SKY_ALLOY_LEGGINGS)
				&& isSkyAlloy(player.getItemBySlot(EquipmentSlot.FEET), ModArmor.SKY_ALLOY_BOOTS);
	}

	private static boolean isSkyAlloy(ItemStack stack, net.minecraft.world.item.Item item) {
		return stack.is(item);
	}
}
