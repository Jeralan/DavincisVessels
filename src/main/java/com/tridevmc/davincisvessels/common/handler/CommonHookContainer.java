package com.tridevmc.davincisvessels.common.handler;


import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.content.block.BlockHelm;
import com.tridevmc.davincisvessels.common.entity.EntitySeat;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.tileentity.TileCrate;
import com.tridevmc.davincisvessels.common.tileentity.TileEntitySecuredBed;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.event.DisassembleBlockEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class CommonHookContainer {
    @SubscribeEvent
    public void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() != null) {
            int x = Mth.floor(event.getTarget().getX());
            int y = Mth.floor(event.getTarget().getY());
            int z = Mth.floor(event.getTarget().getZ());

            BlockEntity te = event.getEntity().level().getBlockEntity(new BlockPos(x, y, z));
            if (te instanceof TileCrate && ((TileCrate) te).getContainedEntity() == event.getTarget()) {
                ((TileCrate) te).releaseEntity();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerSpawnSet(PlayerSetSpawnEvent e) {
        if (e.isCanceled())
            return;

        if (e.getEntity().getGameProfile() != null && e.getEntity().getGameProfile().getId() != null &&
                ConnectionHandler.playerBedMap.containsKey(e.getEntity().getGameProfile().getId())) {
            //Spawn for the player is changing and they use a secured bed, clear the map of the player.

            TileEntitySecuredBed bed = ConnectionHandler.playerBedMap.get(e.getEntity().getGameProfile().getId());

            if (bed.getBlockPos().equals(e.getNewSpawn()))
                return;

            ConnectionHandler.playerBedMap.remove(e.getEntity().getGameProfile().getId());
        }
    }

    @SubscribeEvent
    public void onDisassembleBlock(DisassembleBlockEvent event) {
        // Used to transform user position when a vessel is disassembled.
        if (event.movingWorld instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) event.movingWorld;
            LocatedBlock lb = event.block;
            if (lb.state.getBlock() == DavincisVesselsMod.CONTENT.blockHelm.get()) {
                Entity passenger = vessel.controllingPassenger != null ? vessel.controllingPassenger : vessel.prevRiddenByEntity;

                if (passenger != null) {
                    BlockPos position = lb.pos.relative(lb.state.getValue(BlockHelm.FACING));
                    passenger.stopRiding();
                    passenger.moveTo(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D);
                }
            } else if (DavincisVesselsMod.BLOCK_CONFIG.isSeat(lb.state.getBlock())) {
                Optional<EntitySeat> matchingSeatEntity = vessel.capabilities.getSeats().stream().filter(s -> s.getChunkPos().equals(lb.posNoOffset)).findFirst();

                if (matchingSeatEntity.isPresent()) {
                    EntitySeat matchingSeat = matchingSeatEntity.get();
                    if (matchingSeat.getControllingPassenger() != null) {
                        matchingSeat.getControllingPassenger().stopRiding();
                        matchingSeat.getControllingPassenger().moveTo(lb.pos.getX() + 0.5D, lb.pos.getY() + 0.5D, lb.pos.getZ() + 0.5D);
                    }
                }
            }
        }
    }
}
