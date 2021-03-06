package com.simibubi.create.foundation.gui;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ScreenElementRenderer {

	public static void render3DItem(Supplier<ItemStack> transformsAndStack) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		ItemStack stack = transformsAndStack.get();

		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(stack, 0, 0);
		GlStateManager.popMatrix();
	}

	public static void renderBlock(Supplier<BlockState> transformsAndState) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.translated(0, 0, 200);

		BlockState toRender = transformsAndState.get();

		GlStateManager.scaled(50, -50, 50);
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		mc.getBlockRendererDispatcher().renderBlockBrightness(toRender, 1);

		if (!toRender.getFluidState().isEmpty()) {
			RenderHelper.disableStandardItemLighting();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.setTranslation(0, -300, 0);
			bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			mc.getBlockRendererDispatcher().renderFluid(new BlockPos(0, 300, 0), mc.world, bufferbuilder, toRender.getFluidState());
			Tessellator.getInstance().draw();
			bufferbuilder.setTranslation(0, 0, 0);
		}

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();

		GlStateManager.popMatrix();
	}

}
