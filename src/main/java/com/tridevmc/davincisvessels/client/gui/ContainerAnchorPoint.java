package com.tridevmc.davincisvessels.client.gui;


import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerAnchorPoint extends AbstractContainerMenu {
    public final TileAnchorPoint anchorPoint;
    public final Player player;

    public ContainerAnchorPoint(int window, TileAnchorPoint anchorPoint, Player player) {
        super(DavincisVesselsMod.CONTENT.universalContainerType, window);
        this.anchorPoint = anchorPoint;
        this.player = player;

        bindPlayerInventory(player.getInventory());
        addSlot(new SlotAnchor(this.anchorPoint, 0, 32 + 16, 64 + 36));
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return anchorPoint.stillValid(player);
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 48 + j * 18, 138 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inventoryPlayer, i, 48 + i * 18, 196));
        }
    }

    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slotNum) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotNum < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public class SlotAnchor extends Slot {
        public SlotAnchor(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack itemstack) {
            return TileAnchorPoint.isItemAnchor(itemstack);
        }
    }

}
