package com.tridevmc.davincisvessels.common.network.message;

import javax.annotation.Nullable;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.common.DavincisUIHooks;
import com.tridevmc.davincisvessels.common.IElementProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;

@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.SERVER)
public class OpenGuiMessage extends Message {

    public int entityId;

    public OpenGuiMessage(int entityId) {
        super();
        this.entityId = entityId;
    }

    public OpenGuiMessage() {
        super();
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (!(sender instanceof ServerPlayer))
            return;
        DavincisUIHooks.openGui(sender, (IElementProvider) sender.level().getEntity(entityId));
    }
}
