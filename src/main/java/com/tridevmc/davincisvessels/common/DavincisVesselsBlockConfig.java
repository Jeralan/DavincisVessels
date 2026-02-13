package com.tridevmc.davincisvessels.common;

import com.google.common.collect.Lists;
import com.tridevmc.compound.config.ConfigType;
import com.tridevmc.compound.config.ConfigValue;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.content.block.BlockBalloon;
import com.tridevmc.davincisvessels.common.content.block.BlockSeat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

@ConfigType(ModConfig.Type.SERVER)
public class DavincisVesselsBlockConfig {

    @ConfigValue(comment = "A list of blocks that will function as balloons.")
    public List<Block> balloonAlternatives = new ArrayList<>(DavincisVesselsMod.CONTENT.balloonBlocks.stream().map(x -> x.get()).toList());
    @ConfigValue(comment = "A list of blocks that will behave as seats.")
    public List<Block> seats = Lists.newArrayList(DavincisVesselsMod.CONTENT.blockSeat.get());
    @ConfigValue(comment = "Blocks that behave like a sticky buffer, they stop assembly when reached.")
    public List<Block> stickyObjects = Lists.newArrayList(DavincisVesselsMod.CONTENT.blockStickyBuffer.get(), Blocks.LEVER, Blocks.STONE_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.OAK_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.BIRCH_BUTTON, Blocks.ACACIA_BUTTON);

    public boolean isBalloon(Block block) {
        return block instanceof BlockBalloon || balloonAlternatives.contains(block);
    }

    public boolean isSeat(Block block) {
        return block instanceof BlockSeat || seats.contains(block);
    }

    public boolean isSticky(Block block) {
        return block == DavincisVesselsMod.CONTENT.blockStickyBuffer.get() || stickyObjects.contains(block);
    }
}
