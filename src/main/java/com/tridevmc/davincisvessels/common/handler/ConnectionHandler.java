package com.tridevmc.davincisvessels.common.handler;

import com.tridevmc.davincisvessels.common.entity.EntityParachute;
import com.tridevmc.davincisvessels.common.entity.EntitySeat;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.tileentity.TileEntitySecuredBed;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.UUID;

public class ConnectionHandler {

    public static HashMap<UUID, TileEntitySecuredBed> playerBedMap = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.isCanceled())
            return;
        if (event.getEntity() != null && event.getEntity().level() != null && !event.getEntity().level().isClientSide) {
            handleParachuteLogout(event);

            if (event.getEntity().getVehicle() != null && event.getEntity().getVehicle() instanceof EntityVessel
                    && !event.getEntity().level().getServer().isSingleplayer()) {
                ((EntityVessel) event.getEntity().getVehicle()).disassemble(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.isCanceled())
            return;
        if (event.getEntity() != null && event.getEntity().level() != null && !event.getEntity().level().isClientSide) {
            handleParachuteLogin(event);
            handleBedLogin(event);
        }
    }

    private void handleBedLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (playerBedMap.containsKey(event.getEntity().getGameProfile().getId())) {
            TileEntitySecuredBed bed = playerBedMap.get(event.getEntity().getGameProfile().getId());
            bed.setPlayer(event.getEntity());
            bed.moveBed(bed.getBlockPos());
        }
    }

    private void handleParachuteLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level worldObj = player.level();
        if (player.getPersistentData().getBoolean("reqParachute")) {
            CompoundTag nbt = player.getPersistentData().getCompound("parachuteInfo");

            double vecX = nbt.getDouble("vecX");
            double vecY = nbt.getDouble("vecY");
            double vecZ = nbt.getDouble("vecZ");
            double vesselX = nbt.getDouble("vesselX");
            double vesselY = nbt.getDouble("vesselY");
            double vesselZ = nbt.getDouble("vesselZ");
            double motionX = nbt.getDouble("motionX");
            double motionY = nbt.getDouble("motionY");
            double motionZ = nbt.getDouble("motionZ");
            Vec3dMod vec = new Vec3dMod(vecX, vecY, vecZ);
            Vec3dMod vesselVec = new Vec3dMod(vesselX, vesselY, vesselZ);
            Vec3dMod motionVec = new Vec3dMod(motionX, motionY, motionZ);

            EntityParachute parachute = new EntityParachute(worldObj, player, vec, vesselVec, motionVec);
            ((ServerLevel) worldObj).addFreshEntity(parachute);

            player.getPersistentData().remove("parachuteInfo");
            player.getPersistentData().putBoolean("reqParachute", false);
        }
    }

    private void handleParachuteLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getVehicle() != null && event.getEntity().getVehicle() instanceof EntitySeat) {
            Player player = event.getEntity();
            EntitySeat seat = (EntitySeat) player.getVehicle();
            EntityVessel vessel = seat.getVessel();

            player.stopRiding();
            if (vessel != null && seat.getChunkPos() != null) {
                CompoundTag nbt = new CompoundTag();

                Vec3dMod vec = new Vec3dMod(seat.getChunkPos().getX() - vessel.getMobileChunk().getCenterX(),
                        seat.getChunkPos().getY() - vessel.getMobileChunk().minY(),
                        seat.getChunkPos().getZ() - vessel.getMobileChunk().getCenterZ());
                vec = vec.rotateAroundY((float) Math.toRadians(vessel.getYRot()));

                nbt.putDouble("vecX", vec.x);
                nbt.putDouble("vecY", vec.y);
                nbt.putDouble("vecZ", vec.z);
                nbt.putDouble("vesselX", vessel.getX());
                nbt.putDouble("vesselY", vessel.getY());
                nbt.putDouble("vesselZ", vessel.getZ());
                nbt.putDouble("motionX", vessel.getDeltaMovement().x);
                nbt.putDouble("motionY", vessel.getDeltaMovement().y);
                nbt.putDouble("motionZ", vessel.getDeltaMovement().z);
                player.getPersistentData().put("parachuteInfo", nbt);
                player.getPersistentData().putBoolean("reqParachute", true);
            }
        }
    }

}
