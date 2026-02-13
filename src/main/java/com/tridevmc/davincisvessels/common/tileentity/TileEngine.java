package com.tridevmc.davincisvessels.common.tileentity;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.gui.ContainerEngine;
import com.tridevmc.davincisvessels.client.gui.GuiEngine;
import com.tridevmc.davincisvessels.common.IElementProvider;
import com.tridevmc.davincisvessels.common.api.tileentity.ITileEngineModifier;
import com.tridevmc.davincisvessels.common.entity.VesselCapabilities;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class TileEngine extends BlockEntity implements Container, ITileEngineModifier, IElementProvider<ContainerEngine> {
    public float enginePower;
    public int engineFuelConsumption;
    ItemStack[] itemStacks;
    private int burnTime;
    private boolean running;
    private BlockPos chunkPos;

    public TileEngine(BlockPos pos, BlockState state) {
        super(DavincisVesselsMod.CONTENT.tileTypes.get(TileEngine.class).get(), pos, state);
        itemStacks = new ItemStack[getContainerSize()];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = ItemStack.EMPTY;
        }
        burnTime = 0;
        running = false;
    }

    public TileEngine(float power, int fuelconsumption, BlockPos pos, BlockState state) {
        this(pos, state);

        enginePower = power;
        engineFuelConsumption = fuelconsumption;
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (!tag.contains("fuelConsumption"))
            tag.putInt("fuelConsumption", DavincisVesselsMod.CONFIG.engineConsumptionRate);

        burnTime = tag.getInt("burn");
        engineFuelConsumption = tag.getInt("fuelConsumption");
        enginePower = tag.getFloat("power");
        ListTag list = tag.getList("inv", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag comp = list.getCompound(i);
            int j = comp.getByte("i");
            itemStacks[j] = ItemStack.of(comp);
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("burn", burnTime);
        tag.putInt("fuelConsumption", (short) engineFuelConsumption);
        tag.putFloat("power", enginePower);
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            if (itemStacks[i] != ItemStack.EMPTY) {
                CompoundTag comp = new CompoundTag();
                comp.putByte("i", (byte) i);
                itemStacks[i].save(comp);
                list.add(comp);
            }
        }
        tag.put("inv", list);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag compound = new CompoundTag();
        saveAdditional(compound);
        return ClientboundBlockEntityDataPacket.create(this, x -> compound);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Engine Inventory");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean consumeFuel(int f) {
        if (burnTime >= f) {
            burnTime -= f;
            return true;
        }

        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack is = removeItem(i, 1);
            if (is != ItemStack.EMPTY && is.getCount() > 0) {
                burnTime += is.getBurnTime(RecipeType.SMELTING);
                return consumeFuel(f);
            }
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= 0 && i < 4 ? itemStacks[i] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int i, int n) {
        if (itemStacks[i] != ItemStack.EMPTY) {
            ItemStack itemstack;

            if (itemStacks[i].getCount() <= n) {
                itemstack = itemStacks[i];
                itemStacks[i] = ItemStack.EMPTY;
                setChanged();
                return itemstack;
            }

            itemstack = itemStacks[i].split(n);
            if (itemStacks[i].getCount() <= 0) {
                itemStacks[i] = ItemStack.EMPTY;
            }

            setChanged();
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack content = itemStacks[i].copy();
        itemStacks[i] = ItemStack.EMPTY;
        return content;
    }

    @Override
    public void setItem(int i, @Nonnull ItemStack is) {
        if (i >= 0 && i < 4) {
            itemStacks[i] = is;
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return level != null & level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d) <= 64d;
    }

    @Override
    public void startOpen(@Nonnull Player player) {
    }

    @Override
    public void stopOpen(@Nonnull Player player) {
    }

    @Override
    public boolean canPlaceItem(int i, @Nonnull ItemStack is) {
        return i >= 0 && i < 4 && FurnaceBlockEntity.isFuel(is);
    }

    @Override
    public void clearContent() {
        itemStacks = new ItemStack[getContainerSize()];
    }

    @Override
    public float getPowerIncrement(VesselCapabilities vesselCapabilities) {
        return isRunning() ? enginePower : 0;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos chunkPos) {
        // We don't bother with our parent.

        this.chunkPos = worldPosition;
    }

    @Override
    public EntityMovingWorld getParentMovingWorld() {
        return null;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        // We don't bother with our parent.
    }

    @Override
    public BlockPos getChunkPos() {
        return chunkPos;
    }

    @Override
    public void setChunkPos(BlockPos chunkPos) {

    }

    @Override
    public void tick(MobileChunk mobileChunk) {
        running = consumeFuel(engineFuelConsumption);
    }

    @Override
    public AbstractContainerMenu createMenu(int window, @Nonnull Inventory playerInventory, @Nonnull Player player) {
        return new ContainerEngine(window, this, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AbstractContainerScreen<ContainerEngine> createScreen(ContainerEngine container, Player player) {
        return new GuiEngine(container);
    }
}
