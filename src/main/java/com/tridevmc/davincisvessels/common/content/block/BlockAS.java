package com.tridevmc.davincisvessels.common.content.block;

import java.util.function.Function;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockAS extends Block {
    public BlockAS(Function<Properties, Properties> material, SoundType soundType) {
        this(material, soundType, 1F, 1F);
    }

    public BlockAS(Function<Properties, Properties> material, SoundType soundType, float hardness, float resistance) {
        super(material.apply(Block.Properties.of().sound(soundType).strength(hardness, resistance)));
    }
}
