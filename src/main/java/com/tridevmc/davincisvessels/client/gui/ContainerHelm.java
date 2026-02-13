package com.tridevmc.davincisvessels.client.gui;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerHelm extends AbstractContainerMenu {
    public final TileHelm helm;
    public final Player player;

    public ContainerHelm(int window, TileHelm helm, Player player) {
        super(DavincisVesselsMod.CONTENT.universalContainerType, window);
        this.helm = helm;
        this.player = player;

        bindPlayerInventory(player.getInventory());
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.level().getBlockEntity(helm.getBlockPos()) == helm && helm.getBlockPos().distSqr(player.blockPosition()) < 25D;
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int xOff = 40;
                int yOff = 90;
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + xOff, 84 + i * 18 + yOff));
            }
        }

        for (int i = 0; i < 9; i++) {
            int xOff = 40;
            int yOff = 90;
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18 + xOff, 142 + yOff));
        }
    }

    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);

        //null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();

            //merges the item into player inventory since its in the tile
            if (slot < 9) {
                if (!this.moveItemStackTo(stackInSlot, 0, 35, true)) {
                    return ItemStack.EMPTY;
                }
            }
            //places it into the tile is possible since its in the player inventory
            else if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.getCount() == 0) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }

            if (stackInSlot.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slotObject.onTake(player, stackInSlot);
        }
        return stack;
    }
}
