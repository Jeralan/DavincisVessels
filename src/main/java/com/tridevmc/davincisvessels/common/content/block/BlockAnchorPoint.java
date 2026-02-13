package com.tridevmc.davincisvessels.common.content.block;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.DavincisUIHooks;
import com.tridevmc.davincisvessels.common.tileentity.AnchorInstance;
import com.tridevmc.davincisvessels.common.tileentity.BlockLocation;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class BlockAnchorPoint extends BaseEntityBlock {

    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);

    private VoxelShape xShape, zShape;

    public BlockAnchorPoint() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1F).instrument(NoteBlockInstrument.BASS).mapColor(MapColor.WOOD).ignitedByLava());
    }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        Direction.Axis axis = (Direction.Axis) state.getValue(AXIS);
        if (axis == Direction.Axis.X && xShape == null) {
            xShape = Shapes.create(0.375f, 0.0F, 0, 0.625f, 1.0F, 1F);
        } else if (axis == Direction.Axis.Z && zShape == null) {
            zShape = Shapes.create(0, 0.0F, 0.375f, 1F, 1.0F, 0.625f);
        }
        return axis == Direction.Axis.X ? xShape : zShape;
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        Direction[] adirection = context.getNearestLookingDirections();
        for (int i = 0; i < adirection.length; i++) {
            Direction.Axis axis = adirection[i].getAxis();
            if (axis == Direction.Axis.X || axis == Direction.Axis.Z) {
                return this.defaultBlockState().setValue(AXIS, axis);
            }
        }
        throw new IllegalStateException("No X or Z direction found on block placement");
    }

    @Override
    public void setPlacedBy(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        if (worldIn != null && !worldIn.isClientSide && worldIn.getBlockEntity(pos) != null && worldIn.getBlockEntity(pos) instanceof TileAnchorPoint) {
            if (stack != null && stack.getTag() != null && stack.getTag().contains("INSTANCE")) {
                TileAnchorPoint anchorPoint = (TileAnchorPoint) worldIn.getBlockEntity(pos);
                AnchorInstance instance = new AnchorInstance();
                instance.deserializeNBT(stack.getTag().getCompound("INSTANCE"));
                instance.setIdentifier(UUID.randomUUID());
                anchorPoint.setInstance(instance);

                for (Map.Entry<UUID, BlockLocation> relation : anchorPoint.getInstance().getRelatedAnchors().entrySet()) {
                    Level relationWorld = worldIn.getServer().getLevel(relation.getValue().getDim());
                    BlockState relationState = relationWorld.getBlockState(relation.getValue().getPos());
                    if (relationState.getBlock().equals(DavincisVesselsMod.CONTENT.blockAnchorPoint)) {
                        BlockEntity relationTile = relationWorld.getBlockEntity(relation.getValue().getPos());
                        if (relationTile instanceof TileAnchorPoint) {
                            TileAnchorPoint relationAnchor = (TileAnchorPoint) relationTile;
                            if (!relationAnchor.getInstance().getType().equals(anchorPoint.getInstance().getType())) {
                                if (relationAnchor.getInstance().getIdentifier().equals(relation.getKey())) {
                                    relationAnchor.getInstance().addRelation(anchorPoint.getInstance().getIdentifier(), new BlockLocation(pos, worldIn.dimension()));
                                    relationAnchor.setChanged();
                                    if (worldIn instanceof ServerLevel)
                                        worldIn.getChunk(relationAnchor.getChunkPos()).setUnsaved(true);

                                    continue;
                                }
                            }
                        }
                    }
                    anchorPoint.getInstance().removeRelation(relation.getKey());
                    break;
                }
            }
        }
    }


    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!player.isCrouching()) {
            TileAnchorPoint anchor = worldIn.getBlockEntity(pos) instanceof TileAnchorPoint ? (TileAnchorPoint) worldIn.getBlockEntity(pos) : null;
            if (anchor != null && player instanceof ServerPlayer) {
                DavincisUIHooks.openGui((ServerPlayer) player, anchor, pos);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    // @Override
    // public RenderType getRenderLayer() {
    //     return BlockRenderLayer.SOLID;
    // }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        return new TileAnchorPoint(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level worldIn, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if (be instanceof TileAnchorPoint ap) ap.tick(); };
    }
}
