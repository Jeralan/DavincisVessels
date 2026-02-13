package com.tridevmc.davincisvessels.common.network.message;

import javax.annotation.Nullable;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.LogicalSide;

/**
 * Created by darkevilmac on 1/29/2017.
 */
@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.SERVER)
public class RequestSubmerseMessage extends Message {

    public EntityVessel vessel;
    public boolean doSumberse;

    public RequestSubmerseMessage(EntityVessel vessel, boolean doSumberse) {
        super();
        this.vessel = vessel;
        this.doSumberse = doSumberse;
    }

    public RequestSubmerseMessage() {
        super();
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (vessel != null) {
            if (doSumberse && !vessel.canSubmerge()) {
                if (sender instanceof ServerPlayer) {
                    ((ServerPlayer) sender).connection.disconnect(Component.literal("Invalid submerse request!" +
                            "\nCheating to go underwater... reconsider your life choices."));
                    DavincisVesselsMod.LOG.warn("A user tried to submerse in a vessel that can't, user info: " + sender.getGameProfile().toString());
                }
                return;
            }

            vessel.setSubmerge(doSumberse);
            // TODO: Achievements are gone.
            //sender.addStat(DavincisVesselsContent.achievementSubmerseVessel);
        }
    }
}
