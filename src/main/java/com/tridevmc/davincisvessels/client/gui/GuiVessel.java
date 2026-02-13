package com.tridevmc.davincisvessels.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tridevmc.davincisvessels.common.LanguageEntries;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.network.message.RequestSubmerseMessage;
import com.tridevmc.movingworld.common.network.MovingWorldClientAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

public class GuiVessel extends AbstractContainerScreen<ContainerVessel> {
    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("davincisvessels", "textures/gui/vesselinv.png");

    public final EntityVessel vessel;
    public final Player player;

    private GuiButtonHooked btnDisassemble, btnAlign, btnSubmersible;

    public GuiVessel(ContainerVessel container) {
        super(container, container.player.getInventory(), Component.literal(""));
        vessel = container.vessel;
        player = container.player;
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        btnDisassemble = new GuiButtonHooked(leftPos + 4, topPos + 20, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_VESSELINV_DECOMPILE)));
        btnDisassemble.addHook(((mX, mY) -> {
            MovingWorldClientAction.DISASSEMBLE.sendToServer(vessel);
            if (minecraft != null) { 
                minecraft.popGuiLayer();
            }
        }));
        btnDisassemble.active = vessel.getDisassembler().canDisassemble(vessel.getAssemblyInteractor());
        addRenderableWidget(btnDisassemble);

        btnAlign = new GuiButtonHooked(leftPos + 4, topPos + 40, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_VESSELINV_ALIGN)));
        btnAlign.addHook(((mX, mY) -> {
            MovingWorldClientAction.ALIGN.sendToServer(vessel);
            vessel.alignToGrid(true);
        }));
        addRenderableWidget(btnAlign);

        btnSubmersible = new GuiButtonSubmersible(leftPos + imageWidth + 2, topPos);
        btnSubmersible.addHook(((mX, mY) -> {
            if (((GuiButtonSubmersible) btnSubmersible).canDo) {
                GuiButtonSubmersible subButton = (GuiButtonSubmersible) btnSubmersible;
                new RequestSubmerseMessage(vessel, !subButton.submerse).sendToServer();
                subButton.submerse = !subButton.submerse;
            }
        }));
        ((GuiButtonSubmersible) btnSubmersible).canDo = vessel.canSubmerge();
        if (vessel.canSubmerge())
            ((GuiButtonSubmersible) btnSubmersible).submerse = vessel.getSubmerge();
        else
            ((GuiButtonSubmersible) btnSubmersible).submerse = false;
        addRenderableWidget(btnSubmersible);

    }

    @Override
    public void containerTick() {
        if (this.minecraft != null && this.minecraft.player != null) super.containerTick();

        if (btnDisassemble == null || btnAlign == null) {
            return;
        }

        btnDisassemble.setX(leftPos+4);
        btnAlign.setX(leftPos + 4);
        int y = topPos + 20;
        btnDisassemble.setY(y);
        y += 20;
        btnAlign.setY(y);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mousex, int mousey) {
        int color = 0x404040;
        int row = 8;
        int col0 = 8;

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_VESSELINV_TITLE) + " - " + vessel.getInfo().getName(), col0, row, color);
        row += 5;

        guiGraphics.drawString(font, I18n.get("container.inventory"), 8, imageHeight - 96 + 2, color);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float var1, int var2, int var3) {
        guiGraphics.pose().pushPose();
        guiGraphics.setColor(1F, 1F, 1F, 1F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        guiGraphics.pose().popPose();
    }

    public static class GuiButtonSubmersible extends GuiButtonHooked {

        public boolean submerse = false;

        public boolean canDo = true;

        public GuiButtonSubmersible(int x, int y) {
            super(x, y, 32, 32, Component.literal(""));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                ResourceLocation texture = new ResourceLocation("davincisvessels", "textures/gui/submerse.png");
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                boolean mouseOver = mouseX >= this.getX() && mouseY >= this.getX() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
                int yOffset = 0;
                int xOffset = 0;

                if (canDo) {
                    if (mouseOver) {
                        yOffset += 32;
                    }

                    if (!submerse) {
                        xOffset += 32;
                    }
                } else {
                    yOffset = 64;
                    xOffset = 0;
                }

                guiGraphics.blit(texture, this.getX(), this.getY(), xOffset, yOffset, this.width, this.height);

                if (mouseOver) {
                    String message = !canDo ? "Can't Submerse" : (submerse ? "Submerse Vessel" : "Don't Submerse Vessel");
                    int stringWidth = Minecraft.getInstance().font.width(message);
                    drawHoveringText(texture, guiGraphics, Lists.newArrayList(message),
                            mouseX + (stringWidth / 2) + 32, mouseY - 12, Minecraft.getInstance().font);
                }
            }
        }

        protected void drawHoveringText(ResourceLocation texture, GuiGraphics guiGraphics, List<String> textLines, int x, int y, Font font) {
            if (!textLines.isEmpty()) {
                // GlStateManager.disableRescaleNormal();
                Lighting.setupFor3DItems();
                RenderSystem.disableDepthTest();
                int k = 0;
                Iterator<String> iterator = textLines.iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    int l = font.width(s);

                    if (l > k) {
                        k = l;
                    }
                }

                int j2 = x;
                int k2 = y;
                int i1 = 8;

                if (textLines.size() > 1) {
                    i1 += 2 + (textLines.size() - 1) * 10;
                }

                if (j2 + k > this.width) {
                    j2 -= 28 + k;
                }

                guiGraphics.pose().translate(0, 0, 300);
                int j1 = -267386864;
                guiGraphics.blit(texture, j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
                guiGraphics.blit(texture, j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
                guiGraphics.blit(texture, j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
                guiGraphics.blit(texture, j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
                guiGraphics.blit(texture, j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
                int k1 = 1347420415;
                int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
                guiGraphics.blit(texture, j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
                guiGraphics.blit(texture, j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
                guiGraphics.blit(texture, j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
                guiGraphics.blit(texture, j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

                for (int i2 = 0; i2 < textLines.size(); ++i2) {
                    String s1 = (String) textLines.get(i2);
                    guiGraphics.drawString(font, s1, j2, k2, -1, true);

                    if (i2 == 0) {
                        k2 += 2;
                    }

                    k2 += 10;
                }

                guiGraphics.pose().translate(0, 0, 0);
                RenderSystem.enableDepthTest();
                Lighting.setupForFlatItems();
            }
        }

    }
}
