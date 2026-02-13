package com.tridevmc.davincisvessels.client.gui;

import com.tridevmc.davincisvessels.common.LanguageEntries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class GuiEngine extends AbstractContainerScreen<ContainerEngine> {
    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("davincisvessels", "textures/gui/engine.png");

    public GuiEngine(ContainerEngine container) {
        super(container, container.player.getInventory(), Component.literal(""));
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mousex, int mousey) {
        int color = 0x404040;
        int row = 8;
        int col0 = 8;

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_ENGINE_TITLE), col0, row, color, false);
        row += 5;

        guiGraphics.drawString(font, I18n.get("container.inventory"), 8, imageHeight - 96 + 2, color, false);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float var1, int var2, int var3) {
        guiGraphics.setColor(1F, 1F, 1F, 1F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

}
