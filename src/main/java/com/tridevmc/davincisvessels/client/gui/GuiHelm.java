package com.tridevmc.davincisvessels.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.LanguageEntries;
import com.tridevmc.davincisvessels.common.entity.VesselAssemblyInteractor;
import com.tridevmc.davincisvessels.common.network.HelmClientAction;
import com.tridevmc.davincisvessels.common.network.message.HelmActionMessage;
import com.tridevmc.davincisvessels.common.network.message.RenameVesselMessage;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult.ResultType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import javax.annotation.Nonnull;

import static com.tridevmc.movingworld.common.chunk.assembly.AssembleResult.ResultType.*;

public class GuiHelm extends AbstractContainerScreen<ContainerHelm> {
    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("davincisvessels", "textures/gui/vesselstatus.png");

    public final TileHelm helm;
    public final Player player;

    private GuiButtonHooked btnRename, btnAssemble, btnUndo, btnMount;
    private EditBox txtVesselName;
    private boolean busyCompiling;

    public GuiHelm(ContainerHelm container) {
        super(container, container.player.getInventory(), Component.literal(""));
        helm = container.helm;
        player = container.player;

        imageWidth = 256;
        imageHeight = 256;
    }

    @Override
    public void init() {
        super.init();

        int btnx = leftPos - 100;
        int btny = topPos + 20;
        this.clearWidgets();

        btnRename = new GuiButtonHooked(btnx, btny, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_STATUS_RENAME)));
        btnRename.addHook((mX, mY) -> {
            if (txtVesselName.canConsumeInput()) {
                btnRename.setMessage(Component.literal(I18n.get(LanguageEntries.GUI_STATUS_RENAME)));
                helm.getInfo().setName(txtVesselName.getValue());
                txtVesselName.setFocused(false);
                txtVesselName.setEditable(false);
                new RenameVesselMessage(helm, helm.getInfo().getName()).sendToServer();
            } else {
                btnRename.setMessage(Component.literal(I18n.get(LanguageEntries.GUI_STATUS_DONE)));
                txtVesselName.setEditable(true);
                txtVesselName.setFocused(true);
            }
        });
        addRenderableWidget(btnRename);

        btnAssemble = new GuiButtonHooked(btnx, btny += 20, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_STATUS_COMPILE)));
        btnAssemble.addHook(((mX, mY) -> {
            new HelmActionMessage(helm, HelmClientAction.ASSEMBLE).sendToServer();
            helm.setAssembleResult(null);
            busyCompiling = true;
        }));
        addRenderableWidget(btnAssemble);

        btnUndo = new GuiButtonHooked(btnx, btny += 20, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_STATUS_UNDO)));
        btnUndo.active = helm.getPrevAssembleResult() != null && helm.getPrevAssembleResult().getType() != RESULT_NONE;
        btnUndo.addHook((mX, mY) -> new HelmActionMessage(helm, HelmClientAction.UNDOCOMPILE).sendToServer());
        addRenderableWidget(btnUndo);

        btnMount = new GuiButtonHooked(btnx, btny += 20, 100, 20, Component.literal(I18n.get(LanguageEntries.GUI_STATUS_MOUNT)));
        btnMount.active = helm.getAssembleResult() != null && helm.getAssembleResult().getType() == RESULT_OK;
        btnMount.addHook((mX, mY) -> new HelmActionMessage(helm, HelmClientAction.MOUNT).sendToServer());
        addRenderableWidget(btnMount);

        txtVesselName = new EditBox(font, leftPos + 8 + imageWidth / 2, topPos + 21, 110, 10, Component.literal(""));
        txtVesselName.setMaxLength(127);
        txtVesselName.setVisible(true);
        txtVesselName.setEditable(false);
        txtVesselName.setTextColor(0xFFFFFF);
        txtVesselName.setValue(helm.getInfo().getName());
        addRenderableWidget(txtVesselName);
    }

    @Override 
    public boolean keyPressed(int i, int j, int k) {
        InputConstants.Key mouseKey = InputConstants.getKey(i, j);
        if ((this.minecraft == null || this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) && txtVesselName.canConsumeInput()) {
            return false;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        btnUndo.active = helm.getPrevAssembleResult() != null && helm.getPrevAssembleResult().getType() != RESULT_NONE;
        btnMount.active = helm.getAssembleResult() != null && helm.getAssembleResult().getType() == RESULT_OK;

        btnRename.setX(leftPos - 100);
        btnAssemble.setX(leftPos - 100);
        btnUndo.setX(leftPos - 100);

        int y = topPos + 20;
        btnRename.setY(y);
        y += 20;
        btnAssemble.setY(y);
        y += 20;
        btnUndo.setY(y);
        y += 20;
        btnMount.setY(y);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mousex, int mousey) {
        AssembleResult result = helm.getAssembleResult();

        int colorTitle = 0x404040;
        int row = 8;
        int col0 = 8;
        int col1 = col0 + imageWidth / 2;

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_TITLE), col0, row, colorTitle, false);
        row += 5;
        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_NAME), col0, row += 10, colorTitle, false);

        ResultType rType;
        int rblocks;
        int rballoons;
        int rtes;
        float rmass;

        if (result == null || (result != null && result.assemblyInteractor == null) || (result != null && result.assemblyInteractor != null && !(result.assemblyInteractor instanceof VesselAssemblyInteractor))) {
            rType = busyCompiling ? RESULT_BUSY_COMPILING : RESULT_NONE;
            rblocks = rballoons = rtes = 0;
            rmass = 0f;
        } else {
            rType = result.getType();
            rblocks = result.getBlockCount();
            rballoons = ((VesselAssemblyInteractor) result.assemblyInteractor).getBalloonCount();
            rtes = result.getTileEntityCount();
            rmass = result.getMass();
            if (rType != RESULT_NONE) {
                busyCompiling = false;
            }
        }

        String resultName;
        int valueColor;
        switch (rType) {
            case RESULT_NONE:
                valueColor = colorTitle;
                resultName = LanguageEntries.GUI_STATUS_RESULT_NONE;
                break;
            case RESULT_OK:
                valueColor = 0x40A000;
                resultName = LanguageEntries.GUI_STATUS_RESULT_OKAY;
                break;
            case RESULT_OK_WITH_WARNINGS:
                valueColor = 0xFFAA00;
                resultName = LanguageEntries.GUI_STATUS_RESULT_OKAYWARN;
                break;
            case RESULT_MISSING_MARKER:
                valueColor = 0xB00000;
                resultName = LanguageEntries.GUI_STATUS_RESULT_MISSINGMARKER;
                break;
            case RESULT_BLOCK_OVERFLOW:
                valueColor = 0xB00000;
                resultName = LanguageEntries.GUI_STATUS_RESULT_OVERFLOW;
                break;
            case RESULT_ERROR_OCCURED:
                valueColor = 0xB00000;
                resultName = LanguageEntries.GUI_STATUS_RESULT_ERROR;
                break;
            case RESULT_BUSY_COMPILING:
                valueColor = colorTitle;
                resultName = LanguageEntries.GUI_STATUS_RESULT_BUSY;
                break;
            case RESULT_INCONSISTENT:
                valueColor = 0xB00000;
                resultName = LanguageEntries.GUI_STATUS_RESULT_INCONSISTENT;
                break;
            default:
                valueColor = colorTitle;
                resultName = LanguageEntries.GUI_STATUS_RESULT_NONE;
                break;
        }

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_COMPILERESULT), col0, row += 10, colorTitle, false);
        guiGraphics.drawString(font, I18n.get(resultName), col1, row, valueColor, false);

        float balloonratio = (float) rballoons / rblocks;
        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_VESSELTYPE), col0, row += 10, colorTitle, false);
        if (rblocks == 0) {
            guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_TYPEUNKNOWN), col1, row, colorTitle, false);
        } else {
            guiGraphics.drawString(font, I18n.get(balloonratio > DavincisVesselsMod.CONFIG.flyBalloonRatio ? LanguageEntries.GUI_STATUS_TYPEAIRVESSEL : LanguageEntries.GUI_STATUS_TYPEBOAT), col1, row, colorTitle, false);
        }

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_COUNTBLOCK), col0, row += 10, colorTitle, false);
        guiGraphics.drawString(font, String.valueOf(rblocks), col1, row, colorTitle, false);

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_COUNTBALLOON), col0, row += 10, colorTitle, false);
        guiGraphics.drawString(font, String.valueOf(rballoons) + " (" + (int) (balloonratio * 100f) + "%)", col1, row, colorTitle, false);

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_COUNTTILE), col0, row += 10, colorTitle, false);
        guiGraphics.drawString(font, String.valueOf(rtes), col1, row, colorTitle, false);

        guiGraphics.drawString(font, I18n.get(LanguageEntries.GUI_STATUS_MASS), col0, row += 10, colorTitle, false);
        guiGraphics.drawString(font, String.format(Locale.ROOT, "%.1f %s", rmass, Component.literal(I18n.get(LanguageEntries.GUI_STATUS_MASSUNIT))), col1, row, colorTitle, false);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mX, int mY) {
        guiGraphics.setColor(1F, 1F, 1F, 1F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public boolean charTyped(char c, int k) {
        if (!super.charTyped(c, k)) {
            if (k == 28 && txtVesselName.isFocused()) {
                btnRename.onClick(0, 0);
                return true;
            }
        }
        return false;
    }
}
