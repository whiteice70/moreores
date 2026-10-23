package com.example.moreores.client;

import com.example.moreores.ModBlockEntities;
import com.example.moreores.ModEntities;
import com.example.moreores.ModParticles;
import com.example.moreores.client.hud.GeigerSoundHandler;
import com.example.moreores.client.render.MetalArrowRenderer;
import com.example.moreores.client.render.BioCatalyzerRenderer;
import com.example.moreores.client.render.RadiationParticle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MoreOresClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 铅箭 / 铀箭的实体建模
		EntityRendererRegistry.register(ModEntities.METAL_ARROW, MetalArrowRenderer::new);

		// 辐射粒子：纹理工厂只能客户端注册
		ParticleProviderRegistry.getInstance()
				.register(ModParticles.RADIATION, RadiationParticle.Provider::new);

		// 盖革计数器：只有咔哒声，没有 HUD（按需求去掉了界面）
		GeigerSoundHandler.register();

		// 高能熔炉 / 生物催化器的界面
		ModScreens.register();

		// 生物催化器：隔着玻璃把里面的树苗 / 种子画出来
		//
		// 注：玻璃罩的「半透明渲染层」不在这里设 —— 26.3 改由**模型自己声明**，
		// 见 block/bio_catalyzer.json 里 glass 那条纹理的 "force_translucent": true。
		// （旧版的 BlockRenderLayerMap 在当前的 Fabric API 里已经没有这个类了。）
		BlockEntityRendererRegistry.register(ModBlockEntities.BIO_CATALYZER,
				BioCatalyzerRenderer::new);
	}
}
