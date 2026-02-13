package com.tridevmc.davincisvessels.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.content.block.BlockGauge;
import com.tridevmc.davincisvessels.common.tileentity.TileGauge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class TileEntityGaugeRenderer implements BlockEntityRenderer<TileGauge> {
    @Override
    public void render(@Nonnull TileGauge gauge, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int overlayTexture) {
        Lighting.setupFor3DItems();

        boolean extended = gauge.getBlockState().getBlock() == DavincisVesselsMod.CONTENT.blockGaugeExtended.get();
        int meta = gauge.getBlockState().getValue(BlockGauge.FACING).get2DDataValue();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        Vec3 dVec = new Vec3(0, 0, 0);
        if (gauge.parentVessel == null) {
            dVec.add(0.5F, 0, 0.5F);
        } else if (gauge.parentVessel.getControllingPassenger() instanceof AbstractClientPlayer) {
            dVec = new Vec3(gauge.parentVessel.riderDestination.getX() - gauge.getBlockPos().getX(),
                    gauge.parentVessel.riderDestination.getY() - gauge.getBlockPos().getY(),
                    gauge.parentVessel.riderDestination.getZ() - gauge.getBlockPos().getZ());
        } else {
            dVec = new Vec3(gauge.parentVessel.position().x - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x,
                    gauge.parentVessel.position().y - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y,
                    gauge.parentVessel.position().z - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z);
        }
        double d = dVec.x * dVec.x + dVec.y * dVec.y + dVec.z * dVec.z;
        if (d > 256D) return;

        RenderSystem.lineWidth(6);

        ShaderInstance oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float northGaugeAngle;
        float velGaugeAngle;
        if (gauge.parentVessel == null) {
            northGaugeAngle = meta * 90F;
            velGaugeAngle = 0F;
        } else {
            velGaugeAngle = -(gauge.parentVessel.getHorizontalVelocity() * 3.6F * 20) / 60F * 270F; //vel in m/s * Tick rate (20 Hz) * 3.6 (conversion to km/h) with 60 km/h being 270 degrees.
            if (gauge.parentVessel.level().dimensionTypeId() == BuiltinDimensionTypes.OVERWORLD) {
                northGaugeAngle = -gauge.parentVessel.getYRot() + meta * 90F;
            } else {
                northGaugeAngle = (gauge.parentVessel.tickCount + partialTicks) * 42F + gauge.parentVessel.getYRot() / 3F;
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.05F, 0.5F);
        poseStack.mulPose(new Quaternionf(80F - meta * 90F, 0F, 1F, 0F));

        //Direction gauge
        poseStack.pushPose();
        poseStack.translate(-0.28125F, 0.02F, -0.28125F);
        poseStack.mulPose(new Quaternionf(northGaugeAngle, 0F, 1F, 0F));
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, 0F, 0F, 0F).color(1F, 0F, 0F, 1F).endVertex();
        buffer.vertex(matrix, 0F, 0F, 0.15F).color(1F, 0F, 0F, 1F).endVertex();
        buffer.vertex(matrix, 0F, 0F, 0F).color(1F, 1F, 1F, 1F).endVertex();
        buffer.vertex(matrix, 0F, 0F, -0.15F).color(1F, 1F, 1F, 1F).endVertex();
        tess.end();
        poseStack.popPose();

        //Velocity gauge
        poseStack.pushPose();
        poseStack.translate(0.25F, 0.02F, -0.25F);
        poseStack.mulPose(new Quaternionf(velGaugeAngle, 0F, 1F, 0F));
        matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, 0F, 0F, 0F).color(0F, 0F, 0.5F, 1F).endVertex();
        buffer.vertex(matrix, 0F, 0F, 0.2F).color(0F, 0F, 0.5F, 1F).endVertex();
        tess.end();
        poseStack.popPose();

        if (extended) {
            float vertGaugeAng;
            float height = gauge.getBlockPos().getY();
            if (gauge.parentVessel == null) {
                vertGaugeAng = 0F;
            } else {
                vertGaugeAng = Mth.clamp(((float) gauge.parentVessel.getDeltaMovement().y * 3.6F * 20) / 40F * 360F, -90F, 90F);
                height += (float) gauge.parentVessel.getY();
            }
            float heightGaugeLongAng = -height / 10F * 360F;
            float heightGaugeShortAng = heightGaugeLongAng / 10F;

            //Vertical velocity gauge
            poseStack.pushPose();
            poseStack.translate(0.25F, 0.02F, 0.25F);
            poseStack.mulPose(new Quaternionf(vertGaugeAng, 0F, 1F, 0F));
            matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
            buffer.vertex(matrix, 0F, 0F, 0F).color(0F, 0F, 0.5F, 1F).endVertex();
            buffer.vertex(matrix, 0.2F, 0F, 0F).color(0F, 0F, 0.5F, 1F).endVertex();
            tess.end();
            poseStack.popPose();

            //Height gauge
            poseStack.pushPose();
            poseStack.translate(-0.25F, 0.02F, 0.25F);
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf(heightGaugeLongAng, 0F, 1F, 0F));
            matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
            buffer.vertex(matrix, 0F, 0F, 0F).color(0.9F, 0.9F, 0F, 1F).endVertex();
            buffer.vertex(matrix, 0F, 0F, -0.2F).color(0.9F, 0.9F, 0F, 1F).endVertex();
            tess.end();
            poseStack.popPose();

            poseStack.mulPose(new Quaternionf(heightGaugeShortAng, 0F, 1F, 0F));
            matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
            buffer.vertex(matrix, 0F, -0.01F, 0F).color(0.7F, 0.7F, 0F, 1F).endVertex();
            buffer.vertex(matrix, 0F, -0.01F, -0.15F).color(0.7F, 0.7F, 0F, 1F).endVertex();
            tess.end();

            poseStack.popPose();
        }

        RenderSystem.setShader(() -> oldShader);
        poseStack.popPose();
        Lighting.setupForFlatItems();
    }
}
