package com.tridevmc.davincisvessels.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class CommandDisassembleNear {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dvdisassemblenear")
                .requires(p -> p.hasPermission(3))
                .then(Commands.argument("range", DoubleArgumentType.doubleArg(1D, 64D))
                        .executes((c) -> execute(c, DoubleArgumentType.getDouble(c, "range"))))
                .executes((c) -> execute(c, 16D)));
    }

    private static int execute(CommandContext<CommandSourceStack> d, double range) throws CommandSyntaxException {
        ServerLevel world = d.getSource().getLevel();
        AABB area = new AABB(d.getSource().getPosition(), d.getSource().getPosition()).inflate(range, 256, range);
        List<EntityVessel> vessels = world.getEntitiesOfClass(EntityVessel.class, area);

        if (vessels.isEmpty()) {
            d.getSource().sendFailure(Component.literal("Found no vessels within range to disassemble."));
        } else {
            for (EntityVessel vessel : vessels) {
                if (!vessel.disassemble(false)) {
                    d.getSource().sendFailure(Component.literal("Failed to disassemble vessel, dropping as items."));
                    vessel.dropAsItems();
                }
            }
            d.getSource().sendSuccess(() -> Component.literal(String.format("Disassembled %s vessels.", vessels.size())), true);
        }

        return vessels.size();
    }

}
