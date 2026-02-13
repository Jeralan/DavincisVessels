package com.tridevmc.davincisvessels.common.handler;

import com.tridevmc.davincisvessels.common.entity.EntityParachute;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.TickEvent;

public class CommonPlayerTicker {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.player.getVehicle() instanceof EntityParachute && e.player.getVehicle().tickCount < 40) {
            if (e.player.isCrouching()) {
                e.player.setPose(Pose.STANDING);
            }
        }
    }
}
