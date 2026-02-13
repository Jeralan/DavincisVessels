package com.tridevmc.davincisvessels.common.tileentity;

import com.google.common.collect.Lists;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.gui.ContainerAnchorPoint;
import com.tridevmc.davincisvessels.client.gui.GuiAnchorPoint;
import com.tridevmc.davincisvessels.common.IElementProvider;
import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class TileAnchorPoint extends BlockEntity implements IMovingTile, Container, IElementProvider<ContainerAnchorPoint> {

    public ItemStack content;
    public BlockPos chunkPos;
    private AnchorInstance instance;
    private EntityMovingWorld activeVessel;

    public TileAnchorPoint(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileAnchorPoint.class).get(), pos, state);
        activeVessel = null;
        instance = new AnchorInstance();
        content = ItemStack.EMPTY;
    }

    public static boolean isItemAnchor(ItemStack itemstack) {
        return itemstack != ItemStack.EMPTY && Objects.equals(itemstack.getItem(), Item.BY_BLOCK.get(DavincisVesselsMod.CONTENT.blockAnchorPoint));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, x -> this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag out = new CompoundTag();
        this.saveAdditional(out);
        return out;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
        level.blockEntityChanged(getBlockPos());

        if (FMLEnvironment.dist.isClient()) {
            if (Minecraft.getInstance().screen instanceof GuiAnchorPoint) {
                GuiAnchorPoint activeGUI = (GuiAnchorPoint) Minecraft.getInstance().screen;
                if (Objects.equals(activeGUI.anchorPoint.getBlockPos(), worldPosition)) {
                    activeGUI.init();
                }
            }
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (level != null && tag.contains("vehicle") && level != null) {
            int id = tag.getInt("vehicle");
            Entity entity = level.getEntity(id);
            if (entity instanceof EntityMovingWorld) {
                activeVessel = (EntityMovingWorld) entity;
            }
        }

        CompoundTag instanceCompound = tag.getCompound("INSTANCE");
        if (instanceCompound.getBoolean("INSTANCE")) {
            instance = new AnchorInstance();
            instance.deserializeNBT(instanceCompound);
        }

        if (tag.contains("item")) {
            content = ItemStack.of(tag.getCompound("item"));
        } else {
            content = ItemStack.EMPTY;
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        if (activeVessel != null && !activeVessel.isAlive()) {
            tag.putInt("vehicle", activeVessel.getId());
        }

        if (instance != null) {
            CompoundTag instanceCompound = instance.serializeNBT();
            tag.put("INSTANCE", instanceCompound);
        }

        if (content == ItemStack.EMPTY) {
            tag.remove("item");
        } else {
            content.save(tag.getCompound("item"));
        }
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos chunkPos) {
        chunkPos = worldPosition;
        activeVessel = movingWorld;
    }

    @Override
    public EntityMovingWorld getParentMovingWorld() {
        return activeVessel;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        setParentMovingWorld(entityMovingWorld, new BlockPos(BlockPos.ZERO));
    }

    public AnchorInstance getInstance() {
        return instance;
    }

    public void setInstance(AnchorInstance instance) {
        this.instance = instance;
        this.instance.setChanged(true);
    }

    @Override
    public BlockPos getChunkPos() {
        return chunkPos;
    }

    @Override
    public void setChunkPos(BlockPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void tick(MobileChunk mobileChunk) {
    }

    @Override
    public String toString() {
        return String.format("TileAnchorPoint at {X: %s Y: %s Z: %s} with state {%s} and INSTANCE {%s}", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), level.getBlockState(worldPosition), instance.toString());
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Nullable
    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        } else return content;
    }

    @Nullable
    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack splitResult = ContainerHelper.removeItem(Lists.newArrayList(content), index, count);
        content = splitResult;
        return splitResult;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Nullable
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack removeResult = ContainerHelper.takeItem(Lists.newArrayList(content), index);
        content = removeResult;
        return removeResult;
    }

    @Override
    public void setItem(int index, @Nullable ItemStack stack) {
        if (index != 0)
            throw new IndexOutOfBoundsException();

        this.content = stack;
        if (stack != ItemStack.EMPTY && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return level != null && this.level.getBlockEntity(this.worldPosition) != this ? false : player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void startOpen(@Nonnull Player player) {

    }

    @Override
    public void stopOpen(@Nonnull Player player) {

    }

    @Override
    public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
        boolean accepted = index == 0 &&
                (stack == ItemStack.EMPTY || Objects.equals(stack.getItem(), Item.BY_BLOCK.get(DavincisVesselsMod.CONTENT.blockAnchorPoint)));
        return accepted;
    }

    @Override
    public void clearContent() {
        content = ItemStack.EMPTY;
    }

    public void tick() {
        if (instance != null && instance.hasChanged()) {
            instance.setChanged(false);

            if (level instanceof ServerLevel) {
                level.getChunk(worldPosition).setUnsaved(true);
                setChanged();
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AbstractContainerScreen<ContainerAnchorPoint> createScreen(ContainerAnchorPoint container, Player player) {
        return new GuiAnchorPoint(container);
    }

    @Nullable
    @Override
    public ContainerAnchorPoint createMenu(int window, @Nonnull Inventory playerInv, @Nonnull Player player) {
        return new ContainerAnchorPoint(window, this, player);
    }

    public enum AnchorPointAction {
        LINK, SWITCH
    }
}
