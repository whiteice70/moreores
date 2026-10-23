package com.example.moreores.client.hud;

import com.example.moreores.client.GeigerPresence;
import com.example.moreores.radiation.RadiationManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 盖革计数器的「咔哒」声：只要**背包里有**（不用拿在手上），就会按环境辐射强度响。
 *
 * <p>频率规则：
 * <ul>
 *   <li>环境辐射 0 → 完全不响</li>
 *   <li>辐射越高间隔越短，40（每秒剂量上限）时最快（每 2 tick 一声，约 10 次/秒）</li>
 * </ul>
 *
 * <p>音效借用原版的拉杆声 —— 自定义音效要额外打包 ogg，这里没必要。
 */
public final class GeigerSoundHandler {
	/** 最快间隔（tick）：曝光拉满时每 2 tick 响一次。 */
	private static final int MIN_INTERVAL = 2;

	/** 最慢间隔（tick）：曝光接近 0 时约 1 秒响一次。 */
	private static final int MAX_INTERVAL = 22;

	private static int cooldown;

	private GeigerSoundHandler() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null || client.isPaused() || !GeigerPresence.isCarried(player)) {
			cooldown = 0;
			return;
		}

		int exposure = RadiationManager.exposurePerSecond(player.level(), player);

		// 没有辐射源的时候应该完全安静，不然会一直咔哒很烦
		if (exposure <= 0) {
			cooldown = 0;
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		cooldown = intervalFor(exposure);

		BlockPos pos = player.blockPosition();
		player.level().playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
				SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.18F, 1.7F, false);
	}

	/** 曝光越高间隔越短，线性压到 {@link #MIN_INTERVAL} ~ {@link #MAX_INTERVAL}。 */
	private static int intervalFor(int exposure) {
		int clamped = Math.min(exposure, RadiationManager.MAX_EXPOSURE_PER_SECOND);
		int span = MAX_INTERVAL - MIN_INTERVAL;
		int interval = MAX_INTERVAL - (span * clamped / RadiationManager.MAX_EXPOSURE_PER_SECOND);

		return Math.max(MIN_INTERVAL, interval);
	}

}
