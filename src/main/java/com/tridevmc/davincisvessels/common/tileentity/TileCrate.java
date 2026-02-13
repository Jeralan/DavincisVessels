package com.tridevmc.davincisvessels.common.tileentity;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class TileCrate extends BlockEntity implements IMovingTile {
    private EntityVessel parentVessel;
    private int containedEntityId;
    private Entity containedEntity;
    private int refreshTime;
    private BlockPos chunkPos;

    public TileCrate(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileCrate.class).get(), pos, state);
        parentVessel = null;
        containedEntityId = 0;
        containedEntity = null;
        refreshTime = 0;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos pos) {
        chunkPos = pos;
        parentVessel = (EntityVessel) movingWorld;
    }

    @Override
    public EntityVessel getParentMovingWorld() {
        return parentVessel;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        setParentMovingWorld(entityMovingWorld, new BlockPos(BlockPos.ZERO));
    }

    @Override
    public BlockPos getChunkPos() {
        return chunkPos;
    }

    @Override
    public void setChunkPos(BlockPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void tick(MobileChunk mobileChunk) {
        // We'll try using this experimental tick function (sort of experimental) to keep the entity on the vessel...?
        if (containedEntity == null) {
            if (refreshTime > 0) {
                refreshTime--;
            }
        } else if (!containedEntity.isAlive()) {
            setContainedEntity(null);
        } else {
            containedEntity.setDeltaMovement(0, 0, 0);
            if (parentVessel == null) {
                containedEntity.moveTo(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.15f + containedEntity.getMyRidingOffset(), worldPosition.getZ() + 0.5d);
            } else {
                parentVessel.updatePassengerPosition(containedEntity, worldPosition, 2);
            }

            if (containedEntity.invulnerableTime > 0 || containedEntity.isCrouching()) {
                containedEntity.moveTo(containedEntity.getX(), containedEntity.getY()+1d, containedEntity.getX());
                releaseEntity();
            }
        }
    }

    public boolean canCatchEntity() {
        return refreshTime == 0;
    }

    public void releaseEntity() {
        setContainedEntity(null);
        refreshTime = 60;
    }

    public Entity getContainedEntity() {
        return containedEntity;
    }

    public void setContainedEntity(Entity entity) {
        containedEntity = entity;
        containedEntityId = containedEntity == null ? 0 : containedEntity.getId();
        refreshTime = 0;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag compound = new CompoundTag();
        saveAdditional(compound);
        return ClientboundBlockEntityDataPacket.create(this, x -> compound);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(packet.getTag());
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("contained")) {
            if (level == null) {
                containedEntityId = tag.getInt("contained");
            } else {
                setContainedEntity(level.getEntity(tag.getInt("contained")));
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        if (containedEntity != null) {
            tag.putInt("contained", containedEntity.getId());
        }
    }

    public void tick() {
        if (level.isClientSide) {
            if (parentVessel != null && !parentVessel.isAlive()) {
                parentVessel = null;
            }
            if (containedEntity == null) {
                if (containedEntityId != 0) {
                    setContainedEntity(level.getEntity(containedEntityId));
                }
            }
        }

        if (containedEntity == null) {
            if (refreshTime > 0) {
                refreshTime--;
            }
        } else if (!containedEntity.isAlive()) {
            setContainedEntity(null);
        } else {
            containedEntity.setDeltaMovement(0, 0, 0);
            if (parentVessel == null) {
                containedEntity.moveTo(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.15f + containedEntity.getMyRidingOffset(), worldPosition.getZ() + 0.5d);
            } else {
                parentVessel.updatePassengerPosition(containedEntity, worldPosition, 2);
            }

            if (containedEntity.invulnerableTime > 0 || containedEntity.isCrouching()) {
                containedEntity.moveTo(containedEntity.getX(), containedEntity.getY()+1d, containedEntity.getX());
                releaseEntity();
            }
        }
    }
}
