package com.tridevmc.davincisvessels.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;


public class CommandDVTP {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dvtp")
                .requires(p -> p.hasPermission(2))
                .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(CommandDVTP::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> d) throws CommandSyntaxException {
        ServerPlayer player = d.getSource().getPlayer();
        if (player != null && player.getVehicle() instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) player.getVehicle();
            Vec3 location = Vec3Argument.getVec3(d, "location");
            vessel.setPos(location.x, location.y, location.z);
            vessel.alignToGrid(false);
            return 1;
        } else {
            d.getSource().sendFailure(Component.literal("Not steering a vessel, unable to teleport."));
            return -1;
        }
    }

}