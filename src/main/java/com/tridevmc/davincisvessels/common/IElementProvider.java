package com.tridevmc.davincisvessels.common;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IElementProvider<T extends AbstractContainerMenu> extends MenuProvider {

    @OnlyIn(Dist.CLIENT)
    AbstractContainerScreen<T> createScreen(T container, Player player);

    @Override
    default Component getDisplayName() {
        return Component.literal("");
    }
}
