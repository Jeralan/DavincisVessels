package com.tridevmc.davincisvessels.common.network.message;

import javax.annotation.Nullable;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.gui.ContainerHelm;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

/**
 * Created by darkevilmac on 1/29/2017.
 */
@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.CLIENT)
public class AssembleResultMessage extends Message {

    public AssembleResult result;
    public boolean setPrevious;

    public AssembleResultMessage(AssembleResult result, boolean setPrevious) {
        super();
        this.result = result;
        this.setPrevious = setPrevious;
        DavincisVesselsMod.LOG.info("Created assemble result message, " + (setPrevious ? "for previous result!" : "!"));
    }

    public AssembleResultMessage() {
        super();
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (sender != null && sender.containerMenu instanceof ContainerHelm) {
            ContainerHelm helmContainer = (ContainerHelm) sender.containerMenu;
            if (setPrevious) {
                DavincisVesselsMod.LOG.info("Received previous assemble result!");
                DavincisVesselsMod.LOG.info(this.result.getBlockCount());
                helmContainer.helm.setPrevAssembleResult(result);
                helmContainer.helm.getPrevAssembleResult().assemblyInteractor = result.assemblyInteractor;
            } else {
                DavincisVesselsMod.LOG.info("Received assemble result!");
                DavincisVesselsMod.LOG.info(this.result.getBlockCount());
                helmContainer.helm.setAssembleResult(result);
                helmContainer.helm.getAssembleResult().assemblyInteractor = result.assemblyInteractor;
            }
        }
    }
}
