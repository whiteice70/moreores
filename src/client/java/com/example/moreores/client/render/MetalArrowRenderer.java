package com.example.moreores.client.render;

import com.example.moreores.ModRanged;
import com.example.moreores.MoreOres;
import com.example.moreores.entity.MetalArrow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/**
 * 箭实体的渲染器：直接复用原版 {@link ArrowRenderer} 的几何与行为，
 * 只把纹理换成铅箭 / 铀箭的版本，按箭身上带的物品区分。
 */
public class MetalArrowRenderer extends ArrowRenderer<MetalArrow, MetalArrowRenderState> {
	private static final Identifier LEAD_TEXTURE =
			MoreOres.id("textures/entity/projectiles/lead_arrow.png");

	private static final Identifier URANIUM_TEXTURE =
			MoreOres.id("textures/entity/projectiles/uranium_arrow.png");

	public MetalArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public MetalArrowRenderState createRenderState() {
		return new MetalArrowRenderState();
	}

	@Override
	public void extractRenderState(MetalArrow entity, MetalArrowRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.uranium = entity.getPickupItemStackOrigin().is(ModRanged.URANIUM_ARROW);
	}

	@Override
	protected Identifier getTextureLocation(MetalArrowRenderState state) {
		return state.uranium ? URANIUM_TEXTURE : LEAD_TEXTURE;
	}
}
