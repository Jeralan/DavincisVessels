package com.tridevmc.davincisvessels.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.datafixers.util.Pair;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.LanguageEntries;
import com.tridevmc.davincisvessels.common.network.message.AnchorPointMessage;
import com.tridevmc.davincisvessels.common.tileentity.BlockLocation;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;


public class GuiAnchorPoint extends AbstractContainerScreen<ContainerAnchorPoint> {

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("davincisvessels", "textures/gui/anchorPoint.png");
    public TileAnchorPoint anchorPoint;
    private int selectedRelation;
    private String[] relations;
    private GuiButtonHooked btnLink, btnSwitch, btnNextRelation, btnPrevRelation;

    public GuiAnchorPoint(ContainerAnchorPoint container) {
        super(container, container.player.getInventory(), Component.literal(""));
        this.anchorPoint = container.anchorPoint;

        imageWidth = 256;
        imageHeight = 220;
    }

    @Override
    public void init() {
        super.init();

        this.clearWidgets();
        Font fontRenderer = Minecraft.getInstance().font;
        int linkWidth = fontRenderer.width(I18n.get(LanguageEntries.GUI_ANCHOR_LINK)) + 6;
        int switchWidth = fontRenderer.width(I18n.get(LanguageEntries.GUI_ANCHOR_SWITCH)) + 6;
        int width = linkWidth > switchWidth ? linkWidth : switchWidth;

        int linkX = leftPos + 83;
        int linkY = topPos + 98;

        btnLink = new GuiButtonHooked(linkX, linkY,
                width, 20, Component.literal(I18n.get(LanguageEntries.GUI_ANCHOR_LINK)));
        btnLink.addHook(((mX, mY) -> new AnchorPointMessage(anchorPoint, TileAnchorPoint.AnchorPointAction.LINK).sendToServer()));
        btnLink.active = anchorPoint.content != null;

        int switchX = leftPos + 86 + width;
        int switchY = topPos + 98;

        btnSwitch = new GuiButtonHooked(switchX, switchY,
                width, 20, Component.literal(I18n.get(LanguageEntries.GUI_ANCHOR_SWITCH)));
        btnSwitch.addHook(((mX, mY) -> new AnchorPointMessage(anchorPoint, TileAnchorPoint.AnchorPointAction.LINK).sendToServer()));

        btnPrevRelation = new LongNarrowButton(leftPos + 70, topPos + 73, true);
        btnPrevRelation.addHook((mX, mY) -> {
            if (selectedRelation > relations.length - 1) {
                selectedRelation--;
            } else if (selectedRelation == 0) {
                selectedRelation = relations.length - 1;
            }
        });
        btnNextRelation = new LongNarrowButton(leftPos + 70, topPos + 50, false);
        btnNextRelation.addHook((mX, mY) -> {
            if (selectedRelation < relations.length - 1) {
                selectedRelation++;
            } else if (selectedRelation == relations.length - 1) {
                selectedRelation = 0;
            }
        });

        addRenderableWidget(btnLink);
        addRenderableWidget(btnSwitch);
        addRenderableWidget(btnPrevRelation);
        addRenderableWidget(btnNextRelation);

        relations = new String[anchorPoint.getInstance().getRelatedAnchors().size()];

        int index = 0;
        for (Map.Entry<UUID, BlockLocation> e : anchorPoint.getInstance().getRelatedAnchors().entrySet()) {
            relations[index] = I18n.get(LanguageEntries.GUI_ANCHOR_RELATED, e.getValue().getPos().toString().substring(9)
                    .replace("}", "").replaceAll("=", ":"));
            index++;
        }

        if (relations.length == 0) {
            relations = new String[]{I18n.get(LanguageEntries.GUI_ANCHOR_NORELATIONS)};
        }
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().mulPose(new Quaternionf(3, 0, 1, 0));
        guiGraphics.pose().scale(2.75F, 2.75F, 2.75F);
        guiGraphics.pose().translate(-0.5, 9, 0);
        Lighting.setupForFlatItems();
        guiGraphics.renderItem(new ItemStack(DavincisVesselsMod.CONTENT.blockAnchorPoint.get(), 1), 0, 0);
        Lighting.setupFor3DItems();
        guiGraphics.pose().popPose();

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_ANCHOR_POS, anchorPoint.getBlockPos()
                .toString().substring(9).replace("}", "").replaceAll("=", ":")), 78, 30 - 10, 0);
        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_ANCHOR_TYPE, anchorPoint.getInstance().getType()
                .toString()), 78, 45 - 10, 0);
        guiGraphics.drawString(font, relations[selectedRelation],
                156 - (font.width(relations[selectedRelation]) / 2), 64, 0);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float var1, int var2, int var3) {
        guiGraphics.setColor(1F, 1F, 1F, 1F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURES, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        btnLink.active = anchorPoint.content != null;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    public class LongNarrowButton extends GuiButtonHooked {

        private final boolean down;

        public LongNarrowButton(int x, int y, boolean down) {
            super(x, y, 175, 12, Component.literal(""));

            this.down = down;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                if (minecraft != null) {
                    minecraft.getTextureManager().bindForSetup(GUI_TEXTURES);
                }
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                boolean mouseOver = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
                int yOffset = 220 + (active ? 12 : 0);
                int xOffset = 0;

                if (active) {
                    if (mouseOver) {
                        yOffset += 12;
                    }
                }

                guiGraphics.blit(WIDGETS_LOCATION, this.getX(), this.getY(), xOffset, yOffset, this.width, this.height);
                int arrowX = 175 + (down ? 32 : 0);
                if (mouseOver) {
                    guiGraphics.blit(WIDGETS_LOCATION, this.getX() + (width / 2) - 8, this.getY() - 1, arrowX + 16, 220, 16, 12);
                } else {
                    guiGraphics.blit(WIDGETS_LOCATION, this.getX() + (width / 2) - 8, this.getY() - 1, arrowX, 220, 16, 12);
                }
            }
        }
    }

}
