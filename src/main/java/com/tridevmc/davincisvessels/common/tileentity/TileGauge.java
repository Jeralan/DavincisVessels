package com.tridevmc.davincisvessels.common.tileentity;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
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

public class TileGauge extends BlockEntity implements IMovingTile {
    public EntityMovingWorld parentVessel;
    private BlockPos chunkPos;

    public TileGauge(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileHelm.class).get(), pos, state);
        parentVessel = null;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos pos) {
        this.chunkPos = pos;
        parentVessel = movingWorld;
    }

    @Override
    public EntityMovingWorld getParentMovingWorld() {
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
        // No implementation
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
            load(tag);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("vehicle") && level != null) {
            int id = tag.getInt("vehicle");
            Entity entity = level.getEntity(id);
            if (entity instanceof EntityMovingWorld) {
                parentVessel = (EntityMovingWorld) entity;
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
    }

}
