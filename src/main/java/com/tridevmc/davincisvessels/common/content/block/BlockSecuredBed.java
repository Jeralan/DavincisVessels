package com.tridevmc.davincisvessels.common.content.block;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.TileEntitySecuredBed;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockSecuredBed extends BedBlock {

    public BlockSecuredBed() {
        super(DyeColor.RED, Block.Properties.of().sound(SoundType.WOOD).strength(0.2F).instrument(NoteBlockInstrument.GUITAR).ignitedByLava());
    }

    private Player getPlayerInBed(Level world, BlockPos pos) {
        return world.players().stream().filter((p) -> p.isSleeping() && p.getSleepingPos().get().equals(pos)).findAny().orElse(null);
    }

    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (state.getValue(PART) != BedPart.HEAD) {
                pos = pos.relative(state.getValue(FACING));
                state = world.getBlockState(pos);
                if (state.getBlock() != this) {
                    return InteractionResult.SUCCESS;
                }
            }

            TileEntitySecuredBed bed = world.getBlockEntity(pos) instanceof TileEntitySecuredBed ? (TileEntitySecuredBed) world.getBlockEntity(pos) : null;
            if (world.dimensionType().bedWorks()) {
                if (bed.occupied) {
                    List<Villager> list = world.getEntitiesOfClass(Villager.class, new AABB(pos), LivingEntity::isSleeping);
                    if (list.isEmpty()) {
                        player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
                    } else {
                        list.get(0).stopSleeping();
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    bed.setPlayer(player);
                    player.startSleepInBed(pos).ifLeft((result) -> {
                        if (result.getMessage() != null) {
                            player.displayClientMessage(result.getMessage(), true);
                        }
                    });
                    return InteractionResult.SUCCESS;
                }
            } else {
                world.removeBlock(pos, false);
                BlockPos blockpos = pos.relative(state.getValue(FACING).getOpposite());
                if (world.getBlockState(blockpos).getBlock() == this) {
                    world.removeBlock(blockpos, false);
                }

                Vec3 vec3 = pos.getCenter();
                world.explode(null, world.damageSources().badRespawnPointExplosion(vec3), null, new Vec3((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D), 5.0F, true, Level.ExplosionInteraction.BLOCK);
                return InteractionResult.SUCCESS;
            }
        }
    }


    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return DavincisVesselsMod.CONTENT.itemSecuredBed.get().getDefaultInstance();
    }

    @Override
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
        return Collections.singletonList(state.getValue(PART) == BedPart.HEAD ? DavincisVesselsMod.CONTENT.itemSecuredBed.get().getDefaultInstance() : Items.AIR.getDefaultInstance());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState state) {
        return state.getValue(PART) == BedPart.HEAD ? new TileEntitySecuredBed(blockPos, state) : null;
    }

    // @Override
    // public boolean isBed(BlockState state, Level world, BlockPos pos, @Nullable Entity player) {
    //     return state.getBlock() instanceof BedBlock;
    // }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

}
