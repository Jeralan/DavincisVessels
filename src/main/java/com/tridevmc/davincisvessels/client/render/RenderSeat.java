package com.tridevmc.davincisvessels.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tridevmc.davincisvessels.common.entity.EntitySeat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderSeat extends EntityRenderer<EntitySeat> {
    public RenderSeat(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntitySeat entity) {
        return new ResourceLocation("");
    }

    @Override
    public void render(@Nonnull EntitySeat entity, float f, float f1, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight) {
        //dont
    }

}
