package com.tridevmc.davincisvessels.common.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BlockLocation {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    /*
     * dim is a ResourceKey<Level> toString
     */

    public BlockLocation(BlockPos pos, ResourceKey<Level> dim) {
        this.pos = pos;
        this.dim = dim;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ResourceKey<Level> getDim() {
        return dim;
    }
}
