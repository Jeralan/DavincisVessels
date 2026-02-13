package com.tridevmc.davincisvessels.common.entity;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.Objects;

import javax.annotation.Nonnull;

public class EntitySeat extends Entity {

    private static final EntityDataAccessor<BlockPos> CHUNK_POS = SynchedEntityData.defineId(EntitySeat.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> VESSEL_ID = SynchedEntityData.defineId(EntitySeat.class, EntityDataSerializers.INT);

    public EntitySeat(Level worldIn) {
        super(DavincisVesselsMod.CONTENT.entityTypes.get(EntitySeat.class).get(), worldIn);
    }

    public void setupVessel(EntityVessel vessel, BlockPos chunkPos) {
        moveTo(vessel.getX(), vessel.getY(), vessel.getZ());
        setVessel(vessel);
        setChunkPos(chunkPos);
    }

    @Override
    public void tick() {
        EntityVessel vessel = getVessel();
        if (vessel != null) {
            vessel.updatePassengerPosition(this, getChunkPos(), 0);
        }
        super.tick();
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
    }

    @Override
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand) {
        if (player.isCrouching()) {
            return InteractionResult.FAIL;
        } else if (this.getPassengers().size() > 0) {
            return InteractionResult.SUCCESS;
        } else {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(CHUNK_POS, BlockPos.ZERO);
        this.entityData.define(VESSEL_ID, 0);
    }

    public boolean setChunkPos(BlockPos chunkPos) {
        if (!getChunkPos().equals(chunkPos)) {
            entityData.set(CHUNK_POS, chunkPos);
            return true;
        }
        return false;
    }

    public BlockPos getChunkPos() {
        return entityData.get(CHUNK_POS);
    }

    public EntityVessel getVessel() {
        Entity foundEntity = level().getEntity(entityData.get(VESSEL_ID));
        EntityVessel vessel = null;

        if (foundEntity instanceof EntityVessel)
            vessel = (EntityVessel) foundEntity;

        return vessel;
    }

    public int getVesselId() {
        return entityData.get(VESSEL_ID);
    }

    // Passenger code below.

    @Override
    public double getPassengersRidingOffset() {
        return -0.3D;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : (LivingEntity) this.getPassengers().get(0);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean canAddPassenger(@Nonnull Entity passenger) {
        return this.getPassengers().size() < 1;
    }

    public boolean setVessel(EntityVessel vessel) {
        if (vessel != null && !Objects.equals(getVesselId(), vessel.getId())) {
            entityData.set(VESSEL_ID, vessel.getId());
            return true;
        }
        return false;
    }
}