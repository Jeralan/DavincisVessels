package com.tridevmc.davincisvessels.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class CommandDisassembleVessel {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dvdisassemble")
                .requires(p -> p.hasPermission(3))
                .executes(c -> execute(c, false, false))
                .then(Commands.argument("force", BoolArgumentType.bool())).executes(c -> execute(c, BoolArgumentType.getBool(c, "force"), false))
                .then(Commands.argument("force", BoolArgumentType.bool()).then(Commands.argument("drop", BoolArgumentType.bool()))
                        .executes(c -> execute(c, BoolArgumentType.getBool(c, "force"), BoolArgumentType.getBool(c, "drop")))));
    }

    private static int execute(CommandContext<CommandSourceStack> d, boolean force, boolean drop) throws CommandSyntaxException {
        ServerPlayer player = d.getSource().getPlayer();

        if (player != null && player.getVehicle() instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) player.getVehicle();

            if (!vessel.disassemble(force)) {
                if (drop) {
                    vessel.dropAsItems();
                    d.getSource().sendSuccess(() -> Component.literal("Unable to disassemble vessel, dropped as items."), true);
                } else {
                    d.getSource().sendFailure(Component.literal("Failed to disassemble vessel, have you tried using force?"));
                }
            } else {
                d.getSource().sendSuccess(() -> Component.literal("Disassembled vessel."), true);
            }
            return 1;
        } else {
            d.getSource().sendFailure(Component.literal("Not steering a vessel, no disassembly possible."));
            return -1;
        }
    }
}
