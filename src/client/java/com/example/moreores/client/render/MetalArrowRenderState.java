package com.example.moreores.client.render;

import net.minecraft.client.renderer.entity.state.ArrowRenderState;

/**
 * 给箭的渲染状态多带一个标记。
 *
 * <p>26.3 的渲染器取纹理时拿到的是 RenderState 而不是实体本身，
 * 所以「这是铅箭还是铀箭」必须在 extractRenderState 阶段记下来。
 */
public class MetalArrowRenderState extends ArrowRenderState {
	public boolean uranium;
}
