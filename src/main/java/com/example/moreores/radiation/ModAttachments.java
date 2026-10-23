package com.example.moreores.radiation;

import com.example.moreores.MoreOres;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * 挂在玩家身上的辐射剂量（0 ~ {@link #MAX_DOSE}）。
 *
 * <p>故意没有用 {@code persistent(Codec)}：剂量不写进存档，
 * 退出重进游戏相当于「休养归零」，避免被永久困在辐射状态里。
 *
 * <p>也**没有**声明 {@code syncWith} —— 剂量是纯服务端状态，客户端不需要知道具体数值：
 * 盖革计数器的咔哒声用的是客户端自己按同样规则算出的「环境辐射强度」，
 * 而不是这个累积值（详见 {@link RadiationManager#exposurePerSecond}）。
 * 如果以后要做「把剂量显示在 HUD 上」，那时再加同步也不迟，参考 git 历史。
 */
public final class ModAttachments {
	public static final int MAX_DOSE = 1000;

	public static final AttachmentType<Integer> RADIATION_DOSE =
			AttachmentRegistry.createDefaulted(MoreOres.id("radiation_dose"), () -> 0);

	private ModAttachments() {
	}
}
