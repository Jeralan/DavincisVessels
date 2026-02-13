package com.tridevmc.davincisvessels.common.entity;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

public class FuelInventory implements Container {

    private EntityVessel vessel;
    private ItemStack[] contents;

    public FuelInventory(EntityVessel entityvessel) {
        vessel = entityvessel;
        contents = new ItemStack[getContainerSize()];
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.contents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= 0 && i < 4 ? contents[i] : null;
    }

    @Override
    public ItemStack removeItem(int i, int n) {
        if (contents[i] != null) {
            ItemStack itemstack;

            if (contents[i].getCount() <= n) {
                itemstack = contents[i];
                contents[i] = null;
                setChanged();
                return itemstack;
            }

            itemstack = contents[i].split(n);
            if (contents[i].getCount() <= 0) {
                contents[i] = null;
            }

            setChanged();
            return itemstack;
        }
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack content = contents[i].copy();
        contents[i] = null;

        return content;
    }

    @Override
    public void setItem(int i, @Nonnull ItemStack is) {
        if (i >= 0 && i < 4) {
            contents[i] = is;
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.getVehicle() == vessel;
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
    }

}
