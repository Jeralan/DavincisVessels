package com.tridevmc.davincisvessels.common.tileentity;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.handler.ConnectionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TileEntitySecuredBed extends BlockEntity {

    public boolean occupied;
    public boolean doMove;
    private UUID playerID;

    public TileEntitySecuredBed(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileEntitySecuredBed.class).get(), pos, state);
    }

    public void setPlayer(Player player) {
        if (level != null && !level.isClientSide) {
            if (player != null) {
                this.playerID = player.getGameProfile().getId();
                addToConnectionMap(playerID);
            } else {
                this.playerID = null;
            }
            doMove = false;
        }
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public void addToConnectionMap(UUID idForMap) {
        if (level != null && !level.isClientSide && idForMap != null) {
            if (ConnectionHandler.playerBedMap.containsKey(idForMap)) {
                TileEntitySecuredBed prevBed = ConnectionHandler.playerBedMap.get(idForMap);
                if (!prevBed.worldPosition.equals(worldPosition) && (prevBed.getLevel() == null || (prevBed.getLevel().dimensionType() != level.dimensionType()))) {
                    prevBed.setPlayer(null);
                    ConnectionHandler.playerBedMap.remove(idForMap);
                }
            } else {
                ConnectionHandler.playerBedMap.put(idForMap, this);
            }
        }
    }

    public void moveBed(BlockPos newPos) {
        if (level != null && level.isClientSide)
            return;

        if (playerID != null) {
            addToConnectionMap(playerID);

            if (!doMove || level == null)
                return;

            Player player = level.getPlayerByUUID(playerID);
            if (player != null) {
                ((ServerPlayer) player).setRespawnPosition(level.dimension(), newPos, ((ServerPlayer) player).getRespawnAngle(), true, false);
                doMove = false;
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        if (playerID != null)
            tag.putUUID("uuid", playerID);

        tag.putBoolean("doMove", doMove);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("uuidMost") && tag.contains("uuidLeast"))
            playerID = tag.getUUID("uuid");

        doMove = tag.getBoolean("doMove");

        if (playerID != null && !ConnectionHandler.playerBedMap.containsKey(playerID) && doMove) {
            ConnectionHandler.playerBedMap.put(playerID, this);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag writtenTag = new CompoundTag();
        this.saveAdditional(writtenTag);
        return writtenTag;
    }

}
