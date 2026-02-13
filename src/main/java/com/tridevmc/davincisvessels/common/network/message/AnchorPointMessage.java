package com.tridevmc.davincisvessels.common.network.message;

import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.AnchorInstance;
import com.tridevmc.davincisvessels.common.tileentity.BlockLocation;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

@RegisteredMessage(channel = "davincisvessels", destination = LogicalSide.SERVER)
public class AnchorPointMessage extends Message {

    public TileAnchorPoint anchorPoint;
    public TileAnchorPoint.AnchorPointAction action;

    public AnchorPointMessage(TileAnchorPoint anchorPoint, TileAnchorPoint.AnchorPointAction action) {
        super();
        this.anchorPoint = anchorPoint;
        this.action = action;
    }

    public AnchorPointMessage() {
        super();
    }

    @Override
    public void handle(@Nullable Player sender) {
        if (anchorPoint == null)
            return;

        Level world = null;
        if (sender != null) {
            world = sender.level();
        }


        if (action == TileAnchorPoint.AnchorPointAction.SWITCH) {
            /*
              Clear the entries as well as notify the entries to clear us from them.
              Then switch mode.
             */
            for (HashMap.Entry<UUID, BlockLocation> e : anchorPoint.getInstance().getRelatedAnchors().entrySet()) {
                if (world != null && world.getBlockEntity(e.getValue().getPos()) != null && world.getBlockEntity(e.getValue().getPos()) instanceof TileAnchorPoint) {
                    TileAnchorPoint entryAnchorPoint = (TileAnchorPoint) world.getBlockEntity(e.getValue().getPos());
                    if (entryAnchorPoint != null && entryAnchorPoint.getUpdatePacket() != null) {
                        ((ServerPlayer) sender).connection.send(entryAnchorPoint.getUpdatePacket());
                    }
                } else {
                    DavincisVesselsMod.LOG.error("Invalid entries in anchor tile: " + anchorPoint.toString() + ", cleaning.");
                }
            }

            anchorPoint.getInstance().clearRelations();
            anchorPoint.getInstance().setType(anchorPoint.getInstance().getType().opposite());
            anchorPoint.getInstance().setIdentifier(UUID.randomUUID());
            anchorPoint.setChanged();
        } else if (anchorPoint.content != null) {
            /*
              As a note, we don't set the relation of our own anchor because the anchor we
              would relate it to has yet to be placed, we set this info when the anchor is placed.
             */
            if (anchorPoint.getInstance().getType() == AnchorInstance.InstanceType.LAND) {
                if (anchorPoint.content.getTag() == null) {
                    anchorPoint.content.setTag(new CompoundTag());
                }
                if (anchorPoint.content.getTag().contains("INSTANCE"))
                    anchorPoint.content.getTag().remove("INSTANCE");
                AnchorInstance itemAnchorInstanceTag = new AnchorInstance();

                itemAnchorInstanceTag.setType(AnchorInstance.InstanceType.VESSEL);
                itemAnchorInstanceTag.setIdentifier(UUID.randomUUID());
                itemAnchorInstanceTag.addRelation(anchorPoint.getInstance().getIdentifier(),
                        new BlockLocation(anchorPoint.getBlockPos(), sender.level().dimension()));
                anchorPoint.content.getTag().put("INSTANCE", itemAnchorInstanceTag.serializeNBT());
            } else {
                if (anchorPoint.content.getTag() == null) {
                    anchorPoint.content.setTag(new CompoundTag());
                }
                if (anchorPoint.content.getTag().contains("INSTANCE"))
                    anchorPoint.content.getTag().remove("INSTANCE");

                AnchorInstance itemAnchorInstanceTag = new AnchorInstance();

                itemAnchorInstanceTag.setType(AnchorInstance.InstanceType.LAND);
                itemAnchorInstanceTag.setIdentifier(UUID.randomUUID());
                itemAnchorInstanceTag.addRelation(anchorPoint.getInstance().getIdentifier(),
                        new BlockLocation(anchorPoint.getBlockPos(), sender.level().dimension()));
                anchorPoint.content.getTag().put("INSTANCE", itemAnchorInstanceTag.serializeNBT());
            }
        }
        anchorPoint.setChanged();
        if (world instanceof ServerLevel)
            world.getChunk(anchorPoint.getBlockPos()).setUnsaved(true);
    }
}
