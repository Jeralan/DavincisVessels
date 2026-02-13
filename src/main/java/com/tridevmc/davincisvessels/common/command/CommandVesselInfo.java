package com.tridevmc.davincisvessels.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.entity.VesselCapabilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Locale;

public class CommandVesselInfo {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dvinfo").executes(CommandVesselInfo::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> d) throws CommandSyntaxException {
        ServerPlayer player = d.getSource().getPlayer();

        if (player != null && player.getVehicle() instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) player.getVehicle();
            float f = 100F * ((VesselCapabilities) vessel.getMovingWorldCapabilities()).getBalloonCount() / vessel.getMovingWorldCapabilities().getBlockCount();
            d.getSource().sendSuccess(() -> Component.literal(ChatFormatting.GREEN.toString() + ChatFormatting.BOLD.toString() + "Vessel information"), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Airvessel: %b", vessel.getMovingWorldCapabilities().canFly())), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Position: %.2f, %.2f, %.2f", vessel.position().x, vessel.position().y, vessel.position().z)), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Speed: %.2f km/h", vessel.getHorizontalVelocity() * 20 * 3.6F)), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Block count: %d", vessel.getMovingWorldCapabilities().getBlockCount())), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Balloon count: %d", ((VesselCapabilities) vessel.getMovingWorldCapabilities()).getBalloonCount())), true);
            d.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ENGLISH, "Balloon percentage: %.0f%%", f)), true);
            return 1;
        } else {
            d.getSource().sendFailure(Component.literal("Not steering a vessel, no information to gather."));
            return -1;
        }
    }
}
