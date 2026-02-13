package com.tridevmc.davincisvessels.client.control;

import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.network.message.OpenGuiMessage;
import com.tridevmc.movingworld.common.network.MovingWorldClientAction;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.TickEvent;

@OnlyIn(Dist.CLIENT)
public class VesselKeyHandler {
    private DavincisKeybinds keybinds;
    private boolean kbVesselGuiPrevState, kbDisassemblePrevState, kbAlignPrevState;

    public VesselKeyHandler(DavincisKeybinds cfg) {
        keybinds = cfg;
        kbVesselGuiPrevState = kbDisassemblePrevState = false;

    }

    @SubscribeEvent
    public void keyPress(InputEvent.Key e) {
    }

    @SubscribeEvent
    public void updateControl(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.START && e.side == LogicalSide.CLIENT
                && e.player == Minecraft.getInstance().player
                && e.player.getVehicle() instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) e.player.getVehicle();
            if (keybinds.kbVesselInv.isDown() && !kbVesselGuiPrevState && vessel != null) {
                new OpenGuiMessage(vessel.getId()).sendToServer();
            }
            kbVesselGuiPrevState = keybinds.kbVesselInv.isDown();

            if (keybinds.kbDisassemble.isDown() && !kbDisassemblePrevState) {
                MovingWorldClientAction.DISASSEMBLE.sendToServer(vessel);
            }
            kbDisassemblePrevState = keybinds.kbDisassemble.isDown();

            if (keybinds.kbAlign.isDown() && !kbAlignPrevState) {
                MovingWorldClientAction.ALIGN.sendToServer(vessel);
            }
            kbAlignPrevState = keybinds.kbAlign.isDown();

            int c = getControlCode();
            if (vessel != null && c != vessel.getController().getVesselControl()) {
                vessel.getController().updateControl(vessel, e.player, c);
            }
        }
    }


    public int getControlCode() {
        if (keybinds.kbAlign.isDown()) return 4;
        if (keybinds.kbBrake.isDown()) return 3;
        int vert = 0;
        if (keybinds.kbUp.isDown()) vert++;
        if (keybinds.kbDown.isDown()) vert--;
        return vert == 0 ? 0 : vert < 0 ? 1 : vert > 0 ? 2 : 0;
    }
}
