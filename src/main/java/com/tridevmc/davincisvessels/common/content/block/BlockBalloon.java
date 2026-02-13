package com.tridevmc.davincisvessels.common.content.block;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class BlockBalloon extends Block {
    public BlockBalloon(DyeColor color) {
        super(Block.Properties.of().mapColor(color)
                .sound(SoundType.WOOL)
                .strength(0.35F, 1F)
                .instrument(NoteBlockInstrument.GUITAR)
                .ignitedByLava());
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 30;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 60;
    }
}
