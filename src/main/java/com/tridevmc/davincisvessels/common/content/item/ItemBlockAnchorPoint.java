package com.tridevmc.davincisvessels.common.content.item;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.LanguageEntries;
import com.tridevmc.davincisvessels.common.tileentity.AnchorInstance;
import com.tridevmc.davincisvessels.common.tileentity.BlockLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockAnchorPoint extends BlockItem {
    public Block blockAnchorPoint;

    public ItemBlockAnchorPoint(Block block) {
        super(block, new Item.Properties().stacksTo(1));
        this.blockAnchorPoint = block;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level playerIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag advanced) {
        if (stack.getTag() == null || !stack.getTag().contains("INSTANCE"))
            return;

        if (Screen.hasShiftDown()) {
            AnchorInstance instance = new AnchorInstance();
            instance.deserializeNBT(stack.getTag().getCompound("INSTANCE"));
            String readablePosition = ((BlockLocation) instance.getRelatedAnchors().values().toArray()[0]).getPos()
                    .toString().substring(9).replace("}", "").replaceAll("=", ":");
            tooltip.add(Component.translatable(LanguageEntries.GUI_ANCHOR_POS, ChatFormatting.YELLOW + readablePosition));
            tooltip.add(Component.translatable(LanguageEntries.GUI_ANCHOR_TYPE, ChatFormatting.YELLOW + instance.getType().toString()));
        } else {
            tooltip.add(Component.literal(ChatFormatting.BLUE.toString() + ChatFormatting.BOLD.toString() + ChatFormatting.UNDERLINE.toString()
                    + I18n.get(LanguageEntries.GUI_ITEM_TOOLTIP_SHIFT)));
        }
    }

    @Override
    public Block getBlock() {
        return this.blockAnchorPoint;
    }
}
