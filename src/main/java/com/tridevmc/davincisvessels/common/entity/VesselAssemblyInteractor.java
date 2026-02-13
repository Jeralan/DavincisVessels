package com.tridevmc.davincisvessels.common.entity;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.api.block.IBlockBalloon;
import com.tridevmc.davincisvessels.common.content.block.BlockHelm;
import com.tridevmc.davincisvessels.common.handler.ConnectionHandler;
import com.tridevmc.davincisvessels.common.tileentity.AnchorInstance;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import com.tridevmc.davincisvessels.common.tileentity.TileEntitySecuredBed;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.CanAssemble;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.tridevmc.movingworld.common.chunk.assembly.AssembleResult.ResultType.RESULT_NONE;

public class VesselAssemblyInteractor extends MovingWorldAssemblyInteractor {

    private static List<Entry<ResourceKey<Block>, Block>> allowedBlocks = ForgeRegistries.BLOCKS.getEntries().stream()
            .filter(e -> e.getKey().registry().equals(new ResourceLocation(DavincisVesselsMod.MOD_ID)) && !e.getKey().location().equals("buffer"))
            .collect(Collectors.toList());

    private int balloonCount;

    public VesselAssemblyInteractor() {
    }

    @Override
    public void toByteBuf(ByteBuf byteBuf) {
        byteBuf.writeInt(getBalloonCount());
    }

    @Override
    public MovingWorldAssemblyInteractor fromByteBuf(byte resultCode, ByteBuf buf) {
        if (resultCode == RESULT_NONE.toByte()) {
            return new VesselAssemblyInteractor();
        }
        int balloons = buf.readInt();

        VesselAssemblyInteractor assemblyInteractor = new VesselAssemblyInteractor();
        assemblyInteractor.setBalloonCount(balloons);

        return assemblyInteractor;
    }

    @Override
    public MovingWorldAssemblyInteractor fromNBT(CompoundTag tag, Level world) {
        VesselAssemblyInteractor mov = new VesselAssemblyInteractor();
        mov.setBalloonCount(tag.getInt("balloonCount"));
        return mov;
    }

    @Override
    public void blockAssembled(LocatedBlock locatedBlock) {
        Block block = locatedBlock.state.getBlock();
        if (block instanceof IBlockBalloon) {
            try {
                balloonCount += ((IBlockBalloon) block).getBalloonWorth(locatedBlock.tile);
            } catch (NullPointerException e) {
                MovingWorldMod.LOG.error("IBlockBalloon didn't check if something was null or not, report to mod author of the following block, " + block.toString());
            }
        } else if (DavincisVesselsMod.BLOCK_CONFIG.isBalloon(block)) {
            balloonCount++;
        }
    }

    @Override
    public void blockDisassembled(LocatedBlock locatedBlock) {
        super.blockDisassembled(locatedBlock); // Currently unimplemented but leaving there just in case.

        if (locatedBlock.state.getBlock() == DavincisVesselsMod.CONTENT.blockSecuredBed.get()) {
            if (locatedBlock.tile instanceof TileEntitySecuredBed) {
                TileEntitySecuredBed securedBed = (TileEntitySecuredBed) locatedBlock.tile;

                securedBed.doMove = true;
                ConnectionHandler.playerBedMap.remove(securedBed.getPlayerID());
                securedBed.addToConnectionMap(securedBed.getPlayerID());
                securedBed.moveBed(locatedBlock.pos);
            }
        }
    }

    @Override
    public boolean isBlockMovingWorldMarker(Block block) {
        if (block != null)
            return block == DavincisVesselsMod.CONTENT.blockHelm.get();
        else
            return false;
    }

    @Override
    public boolean isTileMovingWorldMarker(BlockEntity tile) {
        if (tile != null)
            return tile instanceof TileHelm;
        else
            return false;
    }

    @Override
    public CanAssemble isBlockAllowed(Level world, LocatedBlock lb) {
        BlockState state = lb.state;
        CanAssemble canAssemble = super.isBlockAllowed(world, lb);

        if (DavincisVesselsMod.BLOCK_CONFIG.isSticky(state.getBlock()))
            canAssemble.assembleThenCancel = true;

        if (lb.tile instanceof TileAnchorPoint
                && ((TileAnchorPoint) lb.tile).getInstance().getType() == AnchorInstance.InstanceType.LAND)
            canAssemble.justCancel = true;

        if (canAssemble.justCancel) {
            canAssemble.justCancel = !allowedBlocks.contains(lb.getBlock());
        } else {
            canAssemble.justCancel = lb.getBlock() == DavincisVesselsMod.CONTENT.blockBuffer.get();
        }

        return canAssemble;
    }

    @Override
    public Direction getFrontDirection(LocatedBlock marker) {
        return marker.state.getValue(BlockHelm.FACING).getOpposite();
    }

    public int getBalloonCount() {
        return balloonCount;
    }

    public void setBalloonCount(int balloonCount) {
        this.balloonCount = balloonCount;
    }

    @Override
    public void writeNBTFully(CompoundTag tag) {
        writeNBTMetadata(tag);
    }

    @Override
    public void writeNBTMetadata(CompoundTag tag) {
        tag.putInt("balloonCount", getBalloonCount());
    }
}
