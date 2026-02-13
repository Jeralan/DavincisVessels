package com.tridevmc.davincisvessels.client.control;

import com.google.common.collect.Lists;
import com.tridevmc.compound.config.ConfigType;
import com.tridevmc.compound.config.ConfigValue;
import com.tridevmc.davincisvessels.common.LanguageEntries;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ConfigType(ModConfig.Type.CLIENT)
public class DavincisKeybinds {

    @ConfigValue(comment = "Keybindings should be managed in game.")
    public KeyMapping kbUp, kbDown, kbBrake, kbAlign, kbDisassemble, kbVesselInv;

    public DavincisKeybinds() {
        loadKeybindings();
    }

    private void loadKeybindings() {
        kbUp = new KeyMapping(LanguageEntries.KEY_ASCENT, 0x2D, "Davincis Vessels");
        kbDown = new KeyMapping(LanguageEntries.KEY_DESCENT, 0x2C, "Davincis Vessels");
        kbBrake = new KeyMapping(LanguageEntries.KEY_BRAKE, 0x2E, "Davincis Vessels");
        kbAlign = new KeyMapping(LanguageEntries.KEY_ALIGN, 0x0D, "Davincis Vessels");
        kbDisassemble = new KeyMapping(LanguageEntries.KEY_DISASSEMBLE, 0x2B, "Davincis Vessels");
        kbVesselInv = new KeyMapping(LanguageEntries.KEY_INV, 0x25, "Davincis Vessels");
    }

    public void addToControlsMenu() {
        Minecraft mc = Minecraft.getInstance();
        List<KeyMapping> keyBindings = new ArrayList<>(Arrays.asList(mc.options.keyMappings));
        keyBindings.removeAll(Lists.newArrayList(kbUp, kbDown, kbBrake, kbAlign, kbDisassemble, kbVesselInv));
        keyBindings.addAll(Lists.newArrayList(kbUp, kbDown, kbBrake, kbAlign, kbDisassemble, kbVesselInv));
        KeyMapping[] binds = new KeyMapping[keyBindings.size()];
        mc.options.keyMappings= keyBindings.toArray(binds);
    }

}
