package com.example.moreores.client.furnace;

import com.example.moreores.MoreOres;
import com.example.moreores.furnace.MineralCatalyzerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 矿物催化器的界面：和生物催化器共用高能熔炉的 GUI 底图与槽位布局。
 */
public class MineralCatalyzerScreen extends AbstractContainerScreen<MineralCatalyzerMenu> {
	private static final Identifier TEXTURE = MoreOres.id("textures/gui/high_energy_furnace.png");

	private static final int WIDTH = 176;
	private static final int HEIGHT = 166;

	private static final int FLAME_U = 176;
	private static final int FLAME_V = 0;
	private static final int FLAME_W = 14;
	private static final int FLAME_H = 14;

	private static final int ARROW_U = 176;
	private static final int ARROW_V = 14;
	private static final int ARROW_W = 24;
	private static final int ARROW_H = 16;

	private static final int FLAME_X = 56;
	private static final int FLAME_Y = 36;

	private static final int ARROW_X = 79;
	private static final int ARROW_Y = 17;

	public MineralCatalyzerScreen(MineralCatalyzerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos,
				0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			int lit = (int) (FLAME_H * this.menu.getLitProgress());
			lit = Math.max(1, Math.min(FLAME_H, lit));

			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
					this.leftPos + FLAME_X, this.topPos + FLAME_Y + (FLAME_H - lit),
					(float) FLAME_U, (float) (FLAME_V + FLAME_H - lit),
					FLAME_W, lit, 256, 256);
		}

		int progress = (int) (ARROW_W * this.menu.getBurnProgress());

		if (progress > 0) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
					this.leftPos + ARROW_X, this.topPos + ARROW_Y,
					(float) ARROW_U, (float) ARROW_V,
					progress, ARROW_H, 256, 256);
		}
	}
}
