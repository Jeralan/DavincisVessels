package com.tridevmc.davincisvessels.common.network.message;

import javax.annotation.Nullable;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.network.HelmClientAction;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

/**
 * Created by darkevilmac on 2/2/2017.
 */
@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.SERVER)
public class HelmActionMessage extends Message {

    public TileHelm helm;
    public HelmClientAction action;

    public HelmActionMessage() {
        super();
    }

    public HelmActionMessage(TileHelm helm, HelmClientAction action) {
        super();
        this.helm = helm;
        this.action = action;
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (helm == null)
            return;

        switch (action) {
            case ASSEMBLE:
                helm.assembleMovingWorld(sender);
                break;
            case MOUNT:
                helm.mountMovingWorld(sender, helm.getMovingWorld(helm.getLevel()));
                break;
            case UNDOCOMPILE:
                helm.undoCompilation(sender);
                break;
            default:
                break;
        }
    }
}
