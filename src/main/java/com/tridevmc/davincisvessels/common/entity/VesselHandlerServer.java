package com.tridevmc.davincisvessels.common.entity;

import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.entity.MovingWorldHandlerServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

public class VesselHandlerServer extends MovingWorldHandlerServer {

    private EntityMovingWorld movingWorld;
    private boolean firstChunkUpdate;

    public VesselHandlerServer(EntityVessel entityvessel) {
        super(entityvessel);
        firstChunkUpdate = true;
    }

    @Override
    public EntityMovingWorld getMovingWorld() {
        return movingWorld;
    }

    @Override
    public void setMovingWorld(EntityMovingWorld movingWorld) {
        this.movingWorld = movingWorld;
    }

    @Override
    public boolean processInitialInteract(Player player, InteractionHand hand) {
        return movingWorld.getMovingWorldCapabilities().mountEntity(player);
    }

    @Override
    public void onChunkUpdate() {
        super.onChunkUpdate();
        if (firstChunkUpdate) {
            ((VesselCapabilities) movingWorld.getMovingWorldCapabilities()).spawnSeatEntities();
            movingWorld.getEntityData().set(EntityVessel.CAN_SUBMERGE, ((VesselCapabilities) movingWorld.getMovingWorldCapabilities()).canSubmerge());
        }
    }
}