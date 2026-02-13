package com.tridevmc.davincisvessels.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tridevmc.davincisvessels.common.entity.EntityParachute;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

//TODO: possible rewrite?
public class RenderParachute extends EntityRenderer<EntityParachute> {
    public static final ResourceLocation PARACHUTE_TEXTURE = new ResourceLocation("davincisvessels", "textures/entity/parachute.png");

    public ModelParachute model;

    public RenderParachute(EntityRendererProvider.Context context) {
        super(context);
        model = new ModelParachute();
    }

    public void renderParachute(EntityParachute parachute, float yaw, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, (parachute != null && parachute.getControllingPassenger() != null ? parachute.getControllingPassenger().getBoundingBox().getYsize() * 2.5F : 4F), 0);

        poseStack.pushPose();
        poseStack.scale(0.0625F, -0.0625F, -0.0625F);
        // bindEntityTexture(parachute);
        model.renderToBuffer(poseStack, bufferSource.getBuffer(model.renderType(PARACHUTE_TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        // GlStateManager.color4f(0F, 0F, 0F, 1F);
        RenderSystem.lineWidth(4F);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
        buffer.vertex(poseStack.last().pose(), 0F, -3F, 0F).color(0F, 0F, 0F, 1F).endVertex();
        buffer.vertex(poseStack.last().pose(), -1F, 0F, 1F).color(0F, 0F, 0F, 1F).endVertex();

        buffer.vertex(poseStack.last().pose(), 0F, -3F, 0F).color(0F, 0F, 0F, 1F).endVertex();
        buffer.vertex(poseStack.last().pose(), -1F, 0F, -1F).color(0F, 0F, 0F, 1F).endVertex();

        buffer.vertex(poseStack.last().pose(), 0F, -3F, 0F).color(0F, 0F, 0F, 1F).endVertex();
        buffer.vertex(poseStack.last().pose(), 1F, 0F, 1F).color(0F, 0F, 0F, 1F).endVertex();

        buffer.vertex(poseStack.last().pose(), 0F, -3F, 0F).color(0F, 0F, 0F, 1F).endVertex();
        buffer.vertex(poseStack.last().pose(), 1F, 0F, -1F).color(0F, 0F, 0F, 1F).endVertex();
        tess.end();
        // buffer.setTranslation(0F, 0F, 0F);

        poseStack.popPose();
    }

    @Override
    public void render(@Nonnull EntityParachute entity, float f, float f1, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight) {
        renderParachute(entity, f, f1, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityParachute entity) {
        return PARACHUTE_TEXTURE;
    }

}
