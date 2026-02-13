package com.tridevmc.davincisvessels.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.server.ServerStartingEvent;

public class DavincisCommands {
    public static void register(ServerStartingEvent e) {
        CommandDispatcher<CommandSourceStack> dispatcher = e.getServer().getCommands().getDispatcher();
        CommandDisassembleNear.register(dispatcher);
        CommandDisassembleVessel.register(dispatcher);
        CommandDVTP.register(dispatcher);
        CommandVesselInfo.register(dispatcher);
    }
}
