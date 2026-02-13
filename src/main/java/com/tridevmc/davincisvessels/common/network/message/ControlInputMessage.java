package com.tridevmc.davincisvessels.common.network.message;

import javax.annotation.Nullable;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.SERVER)
public class ControlInputMessage extends Message {

    public EntityVessel vessel;
    public int control;

    public ControlInputMessage(EntityVessel vessel, int control) {
        super();
        this.vessel = vessel;
        this.control = control;
    }

    public ControlInputMessage() {
        super();
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (vessel == null)
            return;

        vessel.getController().updateControl(vessel, sender, control);
    }
}
