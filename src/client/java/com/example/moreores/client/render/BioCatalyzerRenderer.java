package com.example.moreores.client.render;

import com.example.moreores.furnace.BioCatalyzerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 生物催化器的渲染器：在玻璃罩内的草坪上**固定**摆一棵**橡树树苗**的微缩。
 *
 * <p>这里渲染的内容**和槽里放什么无关** —— 罩子里永远是同一棵小树苗，
 * 它只是台"生物机器"的装饰，不承担"显示当前催化物"的职责了。
 * 所以整个渲染器不需要读方块实体，连渲染状态都不用带额外字段。
 *
 * <p>位置取 {@code y = 0.40}：模型里草坪顶面在 {@code 6/16 = 0.375}，
 * 放 0.40 正好贴着草面。玻璃罩从 {@code 4/16} 到顶，所以这个高度在罩子内部。
 */
public class BioCatalyzerRenderer
		implements BlockEntityRenderer<BioCatalyzerBlockEntity, BlockEntityRenderState> {
	/** 草坪顶面的高度（模型里是 6/16）。 */
	private static final float LAWN_TOP = 0.40F;

	/** 缩放到 0.6 倍 —— 原版地面物品是 0.5，稍微放大一点才看得清。 */
	private static final float SCALE = 0.6F;

	/** 罩子里固定摆的那棵「微缩树」。 */
	private static final BlockState TREE = Blocks.OAK_SAPLING.defaultBlockState();

	/**
	 * 方块模型解析器。
	 *
	 * <p>⚠️ 物品那个在 {@code Minecraft} 单例上（{@code getItemModelResolver()}），
	 * 方块这个<b>只在渲染器上下文里给</b> —— 所以必须从构造函数接过来存着。
	 */
	private final BlockModelResolver blockModelResolver;

	/** 渲染器工厂要求的构造签名。 */
	public BioCatalyzerRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
	}

	@Override
	public BlockEntityRenderState createRenderState() {
		return new BlockEntityRenderState();
	}

	@Override
	public void submit(BlockEntityRenderState state, PoseStack pose,
			SubmitNodeCollector collector, CameraRenderState camera) {
		BlockModelRenderState blockState = new BlockModelRenderState();
		this.blockModelResolver.update(blockState, TREE, BlockDisplayContext.create());

		if (blockState.isEmpty()) {
			return;
		}

		pose.pushPose();
		pose.translate(0.5F, LAWN_TOP, 0.5F);
		pose.scale(SCALE, SCALE, SCALE);
		// 方块模型的原点在**角上**（物品是居中的），所以往回挪半格才落在草坪正中
		pose.translate(-0.5F, 0.0F, -0.5F);
		blockState.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}
