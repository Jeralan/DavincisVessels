package com.tridevmc.davincisvessels.common.content.item;


import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;

public class ItemSecuredBed extends BlockItem {

    public ItemSecuredBed() {
        super(DavincisVesselsMod.CONTENT.blockSecuredBed.get(), new Item.Properties().stacksTo(1));
        // .group(DavincisVesselsMod.CONTENT.itemGroup)
    }

    @Override
    public InteractionResult place(@Nonnull BlockPlaceContext context) {
        BlockState state = this.getPlacementState(context);
        if (state != null && context.getLevel().setBlock(context.getClickedPos(), state, 26)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Block getBlock() {
        return DavincisVesselsMod.CONTENT.blockSecuredBed.get();
    }
}
