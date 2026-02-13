package com.tridevmc.davincisvessels.common.control;

import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.world.entity.player.Player;

public class VesselControllerCommon {
    private int vesselControl = 0;

    public void updateControl(EntityVessel vessel, Player player, int i) {
        vesselControl = i;
    }

    public int getVesselControl() {
        return vesselControl;
    }
}
