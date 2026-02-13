package com.tridevmc.davincisvessels.client.gui;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class GuiButtonHooked extends ExtendedButton {
    private List<IClickHook> hooks = Lists.newArrayList();

    public GuiButtonHooked(int xPos, int yPos, int width, int height, Component displayString) {
        super(xPos, yPos, width, height, displayString, p -> {
        });
    }

    public void addHook(IClickHook hook) {
        this.hooks.add(hook);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.hooks.forEach(h -> h.onClick(mouseX, mouseY));
    }
}
