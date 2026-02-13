package com.tridevmc.davincisvessels.common.content.block;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import com.tridevmc.davincisvessels.common.tileentity.TileCrate;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

public class BlockCrate extends BaseEntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    private static final VoxelShape SHAPE = Shapes.create(0F, 0F, 0F, 1F, 0.1F, 1F);

    public BlockCrate() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1F).instrument(NoteBlockInstrument.BASS).mapColor(MapColor.WOOD).ignitedByLava());
    }

    public static int getMetaForAxis(Direction.Axis axis) {
        return axis == Direction.Axis.X ? 1 : (axis == Direction.Axis.Z ? 2 : 0);
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    // @Override
    // public BlockRenderLayer getRenderLayer() {
    //     return BlockRenderLayer.SOLID;
    // }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getNearestLookingDirection().getAxis());
    }

    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (world.isClientSide || state.getValue(POWERED))
            return;

        if (entity != null && !(entity instanceof Player || entity instanceof EntityMovingWorld)) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileCrate) {
                if (((TileCrate) te).canCatchEntity() && ((TileCrate) te).getContainedEntity() == null) {
                    ((TileCrate) te).setContainedEntity(entity);
                }
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        return new TileCrate(blockPos, blockState);
    }

    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileCrate) {
            ((TileCrate) te).releaseEntity();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader world, @Nonnull BlockPos pos) {
        BlockPos blockpos = pos.below();
        return canSupportRigidBlock(world, blockpos) || canSupportCenter(world, blockpos, Direction.UP);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean flag) {
        if (world.isClientSide || world.getBlockState(pos).getBlock() != DavincisVesselsMod.CONTENT.blockCrate.get())
            return;

        if (!canSurvive(state, world, pos)) {
            world.destroyBlock(pos, true);
        }

        boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());

        if (powered) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileCrate) {
                ((TileCrate) te).releaseEntity();
                world.setBlock(pos, world.getBlockState(pos).setValue(POWERED, Boolean.TRUE), Block.UPDATE_ALL);
            }
        } else {
            world.setBlock(pos, world.getBlockState(pos).setValue(POWERED, Boolean.FALSE), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS).add(POWERED);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level worldIn, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if (be instanceof TileCrate tc) tc.tick(); };
    }
}
