package com.tridevmc.davincisvessels.common.network.marshallers;

import java.nio.charset.Charset;

import com.tridevmc.compound.network.marshallers.Marshaller;
import com.tridevmc.compound.network.marshallers.RegisteredMarshaller;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.movingworld.MovingWorldMod;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@RegisteredMarshaller(channel = "davincisvessels", acceptedTypes = {BlockEntity.class}, ids = {"tile", "tileentity"})
public class TileEntityMarshaller extends Marshaller<BlockEntity> {

    @Override
    public BlockEntity readFrom(ByteBuf in) {
        BlockEntity tileEntity = null;

        if (in.readBoolean()) {
            int len = in.readInt();
            String dimID = in.readCharSequence(len, Charset.defaultCharset()).toString();
            Level world = MovingWorldMod.PROXY.getWorld(dimID);

            BlockPos blockPos = BlockPos.of(in.readLong());

            tileEntity = world.getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
        }

        return tileEntity;
    }

    @Override
    public void writeTo(ByteBuf out, BlockEntity tileEntity) {
        if (tileEntity == null || tileEntity.getLevel() == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            String dimID = tileEntity.getLevel().dimension().toString();
            out.writeInt(dimID.length());
            out.writeCharSequence(dimID, Charset.defaultCharset());
            out.writeLong(tileEntity.getBlockPos().asLong());
        }
    }
}
