package com.tridevmc.davincisvessels.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.content.block.BlockHelm;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import com.tridevmc.movingworld.api.IMovingTile;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.joml.Quaternionf;

public class TileEntityHelmRenderer implements BlockEntityRenderer<TileHelm> {

    @Override
    public void render(@Nonnull TileHelm te, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int overlayTexture) {
        try {
            renderHelm(te, poseStack, bufferSource, partialTicks);
        } catch (Exception e) {
            if (e instanceof IOException)
                e.printStackTrace();
            else
                DavincisVesselsMod.LOG.error("Error when rendering helm, ", e);
        }
    }

    private void renderHelm(TileHelm helm, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, float partialTicks) throws Exception {
        EntityVessel vessel = null;
        BlockState blockState = helm.getLevel().getBlockState(helm.getBlockPos());
        Direction blockStateFacing = Direction.UP;

        if (blockState.getBlock() instanceof BlockHelm)
            blockStateFacing = blockState.getValue(BlockHelm.FACING);
        if (((IMovingTile) helm).getParentMovingWorld() != null && ((IMovingTile) helm).getParentMovingWorld() instanceof EntityVessel) {
            vessel = (EntityVessel) ((IMovingTile) helm).getParentMovingWorld();
        }

        float vesselPitch = 0;
        if (vessel != null)
            vesselPitch = vessel.xRotO + (vessel.getXRot() - vessel.xRotO) * partialTicks;

        if (blockStateFacing == Direction.NORTH || blockStateFacing == Direction.WEST) {
            vesselPitch *= -1;
        }
        boolean onZAxis = blockStateFacing.getAxis() == Direction.Axis.Z;

        float translateX, translateY, translateZ;

        if (onZAxis) {
            translateX = .5F;
            translateY = 10F / 16F;
            translateZ = 0F;
        } else {
            translateX = 0F;
            translateY = 10F / 16F;
            translateZ = -.5F;
        }

        poseStack.pushPose();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        // GlStateManager.enableRescaleNormal();

        // if (Minecraft.isAmbientOcclusionEnabled()) {
        //     GlStateManager.shadeModel(7425);
        // } else {
        //     GlStateManager.shadeModel(7424);
        // }

        poseStack.translate(0, 0, 1);

        poseStack.translate(translateX, translateY, translateZ);
        poseStack.mulPose(new Quaternionf(vesselPitch * 10, onZAxis ? 0 : 1, 0, onZAxis ? 1 : 0));
        poseStack.translate(-translateX, -translateY, -translateZ);

        BlockState wheelState = blockState.setValue(BlockHelm.IS_WHEEL, true);
        BakedModel stateModel = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(wheelState);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .tesselateBlock(helm.getLevel(), stateModel, wheelState, 
                helm.getBlockPos(), poseStack, buffer, false, 
                RandomSource.create(), wheelState.getSeed(helm.getBlockPos()), 
                OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        // GlStateManager.disableRescaleNormal();
        poseStack.popPose();

    }
}
