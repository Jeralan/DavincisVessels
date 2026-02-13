package com.tridevmc.davincisvessels.common.api.block;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IBlockBalloon {

    /**
     * How many balloon blocks is this block equivalent to?
     *
     * @param tileEntity null if not applicable.
     */
    int getBalloonWorth(BlockEntity tileEntity);

}
