package com.tridevmc.davincisvessels.client.gui;

import javax.annotation.Nonnull;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.tileentity.TileEngine;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

public class ContainerEngine extends AbstractContainerMenu {
    public final TileEngine engine;
    public final Player player;

    public ContainerEngine(int window, TileEngine engine, Player player) {
        super(DavincisVesselsMod.CONTENT.universalContainerType, window);
        this.engine = engine;
        this.player = player;

        addSlot(new SlotFuel(engine, 0, 26, 23));
        addSlot(new SlotFuel(engine, 1, 44, 23));
        addSlot(new SlotFuel(engine, 2, 26, 41));
        addSlot(new SlotFuel(engine, 3, 44, 41));

        bindPlayerInventory(player.getInventory());
    }

    protected void bindPlayerInventory(Inventory inventoryplayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventoryplayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inventoryplayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return engine.stillValid(player);
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when
     * someone does that.
     */
    @Override
    public ItemStack quickMoveStack(@Nonnull Player par1EntityPlayer, int slotNum) {
        ItemStack stackClone = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            stackClone = stack.copy();

            if (slotNum < this.engine.getContainerSize()) {
                if (!this.moveItemStackTo(stack, this.engine.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, this.engine.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stackClone;
    }

    public static class SlotFuel extends Slot {
        public SlotFuel(Container inventory, int id, int x, int y) {
            super(inventory, id, x, y);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack itemstack) {
            return FurnaceBlockEntity.isFuel(itemstack);
        }
    }
}
