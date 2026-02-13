package com.tridevmc.davincisvessels.common.tileentity;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.gui.ContainerHelm;
import com.tridevmc.davincisvessels.client.gui.GuiHelm;
import com.tridevmc.davincisvessels.common.IElementProvider;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.entity.VesselAssemblyInteractor;
import com.tridevmc.davincisvessels.common.network.message.AssembleResultMessage;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.entity.MovingWorldInfo;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileHelm extends TileMovingMarkingBlock implements IElementProvider<ContainerHelm> {

    public boolean submerge;
    private VesselAssemblyInteractor interactor;
    private EntityVessel activeVessel;
    private MovingWorldInfo info;
    private BlockPos chunkPos;

    public TileHelm(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileHelm.class).get(), pos, state);
        activeVessel = null;
    }

    @Override
    public void assembledMovingWorld(Player player, boolean returnVal) {
        sendAssembleResult(player, false);
        sendAssembleResult(player, true);

        // TODO: Achievements are gone.
        //player.addStat(getAssembleResult().isOK() ? DavincisVesselsContent.achievementAssembleSuccess : DavincisVesselsContent.achievementAssembleFailure);
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos chunkPos) {
        chunkPos = worldPosition;
        activeVessel = (EntityVessel) movingWorld;
    }

    @Override
    public EntityVessel getParentMovingWorld() {
        return activeVessel;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        setParentMovingWorld(entityMovingWorld, new BlockPos(BlockPos.ZERO));
    }

    @Override
    public BlockPos getChunkPos() {
        return chunkPos;
    }

    @Override
    public void setChunkPos(BlockPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void tick(MobileChunk mobileChunk) {
        // No implementation
    }

    @Override
    @Nonnull
    public MovingWorldAssemblyInteractor getInteractor() {
        if (interactor == null) {
            interactor = new VesselAssemblyInteractor();
        }
        return interactor;
    }

    @Override
    public void setInteractor(@Nonnull MovingWorldAssemblyInteractor interactor) {
        this.interactor = (VesselAssemblyInteractor) interactor;
    }

    @Override
    @Nonnull
    public MovingWorldInfo getInfo() {
        if (this.info == null)
            this.info = new MovingWorldInfo();
        return info;
    }

    @Override
    public void setInfo(@Nonnull MovingWorldInfo info) {
        this.info = info;
    }

    @Override
    public int getMaxBlocks() {
        return DavincisVesselsMod.CONFIG.maxVesselChunkBlocks;
    }

    @Override
    public EntityMovingWorld getMovingWorld(Level worldObj) {
        return new EntityVessel(worldObj);
    }

    @Override
    public void mountedMovingWorld(Player player, EntityMovingWorld movingWorld, MountStage stage) {
        switch (stage) {
            case PREMSG: {
                sendAssembleResult(player, false);
            }
            case PRERIDE: {
            }
            case POSTRIDE: {
                // TODO: Achievements are gone
                // player.addStat(DavincisVesselsContent.achievementAssembleMount);
            }
        }
    }

    @Override
    public void undoCompilation(Player player) {
        super.undoCompilation(player);
        sendAssembleResult(player, false);
        sendAssembleResult(player, true);
    }

    @Override
    public MovingWorldAssemblyInteractor getNewAssemblyInteractor() {
        return new VesselAssemblyInteractor();
    }

    public void sendAssembleResult(Player player, boolean sendPrev) {
        if (level != null && !level.isClientSide) {
            AssembleResult res;
            if (sendPrev) {
                res = getPrevAssembleResult();
            } else {
                res = getAssembleResult();
            }

            if (res == null) {
                res = new AssembleResult(AssembleResult.ResultType.RESULT_NONE, null);
                res.assemblyInteractor = this.getNewAssemblyInteractor();
            }

            new AssembleResultMessage(res, sendPrev).sendToAllTracking(this);
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("submergeVesselOnAssemble", submerge);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        submerge = tag.getBoolean("submergeVesselOnAssemble");
    }

    @Override
    public AbstractContainerMenu createMenu(int window, @Nonnull Inventory playerInventory, @Nonnull Player player) {
        return new ContainerHelm(window, this, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AbstractContainerScreen<ContainerHelm> createScreen(ContainerHelm container, Player player) {
        return new GuiHelm(container);
    }
}
