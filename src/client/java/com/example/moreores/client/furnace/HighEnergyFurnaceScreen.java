package com.example.moreores.client.furnace;

import com.example.moreores.MoreOres;
import com.example.moreores.furnace.HighEnergyFurnaceMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 高能熔炉的界面。
 *
 * <p>没有继承 {@code AbstractFurnaceScreen}，因为它的槽位绘制逻辑写死了原版的三个位置，
 * 而我们多一个副产物槽。这里直接走 {@link AbstractContainerScreen}，
 * 火焰和进度箭头自己用 blit 画。
 *
 * <p>图幅仍是原版的 176×166 —— 副产物槽放在输出槽正上方（116,17），
 * 没有往下占地方，所以不需要加高（加高反而会压到背包，实测很挤）。
 *
 * <p>26.3 的界面绘制是「提取」式：入口是
 * {@code extractBackground(GuiGraphicsExtractor, ...)}（旧版叫 {@code renderBg}），
 * 而 {@code GuiGraphicsExtractor#blit} 有一个很简洁的重载：
 * {@code blit(RenderPipeline, Identifier, x, y, u, v, w, h, texW, texH)}。
 */
public class HighEnergyFurnaceScreen extends AbstractContainerScreen<HighEnergyFurnaceMenu> {
	private static final Identifier TEXTURE = MoreOres.id("textures/gui/high_energy_furnace.png");

	/** 图幅：和原版熔炉一致。 */
	private static final int WIDTH = 176;
	private static final int HEIGHT = 166;

	/**
	 * 火焰图标在 GUI 纹理里的位置。
	 *
	 * <p>⚠️ 26.3 把火焰和箭头从「底图扩展区」拆成了独立 sprite
	 * （`textures/gui/sprites/container/furnace/lit_progress.png` 等），
	 * 原版 `furnace.png` 的 `u >= 176` 那块**已经是全透明的** ——
	 * 照老约定从这里取图会取到一片空白，表现就是「火焰完全不显示」。
	 *
	 * <p>我们的做法是把那两张 sprite **按老位置拼回自己的 GUI 纹理**，
	 * 所以这里的坐标依然成立，绘制代码不用改。详见 README。
	 */
	private static final int FLAME_U = 176;
	private static final int FLAME_V = 0;
	private static final int FLAME_W = 14;
	private static final int FLAME_H = 14;

	/** 进度箭头（同上：内容由拼图提供）。 */
	private static final int ARROW_U = 176;
	private static final int ARROW_V = 14;
	private static final int ARROW_W = 24;
	private static final int ARROW_H = 16;

	/**
	 * 火焰：夹在原料槽（y 17~33）和燃料槽（y 53~69）之间，和原版熔炉一致。
	 */
	private static final int FLAME_X = 56;
	private static final int FLAME_Y = 36;

	/**
	 * 箭头：原料槽和烧制物槽都在 y = 17 这一行（原版烧制物槽在 35，所以箭头原先是 34），
	 * 这里跟着上移，让箭头的竖直中线对准两个槽的中间（17 + 8 = 25，箭头高 16 → 起点 17）。
	 */
	private static final int ARROW_X = 79;
	private static final int ARROW_Y = 17;

	public HighEnergyFurnaceScreen(HighEnergyFurnaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);

		// 底图
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos,
				0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		// 剩余燃料：火焰从下往上
		if (this.menu.isLit()) {
			int lit = (int) (FLAME_H * this.menu.getLitProgress());
			lit = Math.max(1, Math.min(FLAME_H, lit));

			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
					this.leftPos + FLAME_X, this.topPos + FLAME_Y + (FLAME_H - lit),
					(float) FLAME_U, (float) (FLAME_V + FLAME_H - lit),
					FLAME_W, lit, 256, 256);
		}

		// 烧制进度：箭头从左往右
		int progress = (int) (ARROW_W * this.menu.getBurnProgress());

		if (progress > 0) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
					this.leftPos + ARROW_X, this.topPos + ARROW_Y,
					(float) ARROW_U, (float) ARROW_V,
					progress, ARROW_H, 256, 256);
		}
	}
}
