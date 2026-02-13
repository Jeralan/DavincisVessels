package com.tridevmc.davincisvessels.common.entity;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityParachute extends Entity implements IEntityAdditionalSpawnData {

    public EntityParachute(Level world) {
        super(DavincisVesselsMod.CONTENT.entityTypes.get(EntityParachute.class).get(), world);
    }

    public EntityParachute(Level world, EntityVessel vessel, BlockPos pos) {
        this(world);
        Vec3dMod vec = new Vec3dMod(pos.getX() - vessel.getMobileChunk().getCenterX(), pos.getY() - vessel.getMobileChunk().minY(), pos.getZ() - vessel.getMobileChunk().getCenterZ());
        vec = vec.rotateAroundY((float) Math.toRadians(vessel.getYRot()));

        moveTo(vessel.getX() + vec.x, vessel.getY() + vec.y - 2D, vessel.getZ() + vec.z, 0F, 0F);
        this.setDeltaMovement(vessel.getDeltaMovement());
    }

    public EntityParachute(Level world, Entity mounter, Vec3dMod vec, Vec3dMod vesselPos, Vec3dMod motion) {
        this(world);

        moveTo(vesselPos.x + vec.x, vesselPos.y + vec.y - 2D, vesselPos.z + vec.z, 0F, 0F);
        this.setDeltaMovement(motion);

        mounter.stopRiding();
        mounter.startRiding(this, true);
    }

    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return new EntityDimensions(1, 1, true);
    }

    @Override
    protected void defineSynchedData() {
        // NO-OP
    }

    @Override
    public void tick() {
        super.tick();

        xOld = getX();
        yOld = getY();
        zOld = getZ();

        if (!level().isClientSide
                &&
                (getControllingPassenger() == null
                        || onGround()
                        || isInWater())) {
            remove(RemovalReason.DISCARDED);
            return;
        }

        if (!level().isClientSide && getControllingPassenger() != null) {
            this.setDeltaMovement(this.getDeltaMovement().add(getControllingPassenger().getDeltaMovement().x, 0, getControllingPassenger().getDeltaMovement().z));
        }
        if (getDeltaMovement().y > -.5)
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.025D, 0));

        move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return (LivingEntity) this.getPassengers().stream().findAny().orElse(null);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        // NO-OP
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        // NO-OP
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        // No-OP
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMult, DamageSource damageSource) {
        return false;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(getControllingPassenger() != null);
        if (getControllingPassenger() != null) {
            buffer.writeInt(getControllingPassenger().getId());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        if (additionalData.readBoolean() && level() != null) {
            int entityID = additionalData.readInt();
            if (level().getEntity(entityID) != null) {
                level().getEntity(entityID).startRiding(this);
            }
        }
    }
}
