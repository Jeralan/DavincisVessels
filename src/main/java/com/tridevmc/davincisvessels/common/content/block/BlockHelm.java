package com.tridevmc.davincisvessels.common.content.block;

import com.tridevmc.davincisvessels.common.DavincisUIHooks;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHelm extends DirectionalBlock implements EntityBlock {

    public static final BooleanProperty IS_WHEEL = BooleanProperty.create("wheel");

    public BlockHelm(Properties properties) {
        super(properties.sound(SoundType.WOOD));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(IS_WHEEL, false));
    }

    // @Override
    // public BlockRenderLayer getRenderLayer() {
    //     return BlockRenderLayer.CUTOUT_MIPPED;
    // }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        VoxelShape selectedShape = super.getShape(state, worldIn, pos, context);
        if (state == null || state.getValue(FACING) == null)
            return super.getShape(state, worldIn, pos, context);

        double pixelSize = 1D / 16D;
        Direction facing = state.getValue(FACING);
        switch (facing) {
            case NORTH: {
                selectedShape = Shapes.create(pixelSize * 1, 0, pixelSize * 2, 1 - (pixelSize * 1), 1, 1 - (pixelSize * 3));

                return selectedShape;
            }
            case SOUTH: {
                selectedShape = Shapes.create(pixelSize * 1, 0, pixelSize * 3, 1 - (pixelSize * 1), 1, 1 - (pixelSize * 2));

                return selectedShape;
            }
            case WEST: {
                selectedShape = Shapes.create(pixelSize * 2, 0, pixelSize * 1, 1 - (pixelSize * 3), 1, 1 - (pixelSize * 1));

                return selectedShape;
            }
            case EAST: {
                selectedShape = Shapes.create(pixelSize * 3, 0, pixelSize * 1, 1 - (pixelSize * 2), 1, 1 - (pixelSize * 1));

                return selectedShape;
            }
            default: {
                return selectedShape;
            }
        }
    }

    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!player.isCrouching()) {
            TileHelm helm = world.getBlockEntity(pos) instanceof TileHelm ? (TileHelm) world.getBlockEntity(pos) : null;
            if (helm != null && player instanceof ServerPlayer) {
                DavincisUIHooks.openGui(player, helm);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onRemove(state, world, pos, newState, isMoving);
            world.removeBlockEntity(pos);
        }
    }

    @Override
    public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, int id, int param) {
        super.triggerEvent(state, world, pos, id, param);
        BlockEntity tile = world.getBlockEntity(pos);
        return tile != null && tile.triggerEvent(id, param);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        if (context.getPlayer() != null && !(context.getPlayer() instanceof FakePlayer)) {
            // TODO: Achievements are gone.
            //((EntityPlayer) placer).addStat(DavincisVesselsContent.achievementCreateHelm);
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        if (placer != null) {
            worldIn.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
        }
        else {
            worldIn.setBlock(pos, state, 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_WHEEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        return new TileHelm(blockPos, blockState);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 5;
    }
}
