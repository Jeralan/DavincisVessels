package com.tridevmc.davincisvessels;

import com.tridevmc.compound.config.CompoundConfig;
import com.tridevmc.compound.network.core.CompoundNetwork;
import com.tridevmc.davincisvessels.client.ClientProxy;
import com.tridevmc.davincisvessels.common.CommonProxy;
import com.tridevmc.davincisvessels.common.DavincisVesselsBlockConfig;
import com.tridevmc.davincisvessels.common.DavincisVesselsConfig;
import com.tridevmc.davincisvessels.common.content.DavincisVesselsContent;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DavincisVesselsMod.MOD_ID)
public class DavincisVesselsMod {
    public static final String MOD_ID = "davincisvessels";
    public static final String RESOURCE_DOMAIN = "davincisvessels:";
    public static final Logger LOG = LogManager.getLogger("DavincisVessels");
    public static final DavincisVesselsContent CONTENT = new DavincisVesselsContent();
    public static DavincisVesselsMod INSTANCE;
    public static CommonProxy PROXY;
    public static DavincisVesselsConfig CONFIG;
    public static DavincisVesselsBlockConfig BLOCK_CONFIG;
    public static FMLJavaModLoadingContext CONTEXT;

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    public DavincisVesselsMod(FMLJavaModLoadingContext context) {
        CONTEXT = context;
        DavincisVesselsMod.INSTANCE = this;
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        ModContainer container = context.getContainer();
        CONFIG = CompoundConfig.of(DavincisVesselsConfig.class, container, "davincis-main.toml");
        CONTENT.onEntityRegister(ENTITY_TYPES);
        ENTITY_TYPES.register(context.getModEventBus());

        CONTENT.onBlockRegister(BLOCKS, CREATIVE_MODE_TAB);
        BLOCKS.register(context.getModEventBus());
        CREATIVE_MODE_TAB.register(context.getModEventBus());

        CONTENT.onItemRegister(ITEMS);
        ITEMS.register(context.getModEventBus());

        CONTENT.onTileRegister(BLOCK_ENTITY_TYPES);
        BLOCK_ENTITY_TYPES.register(context.getModEventBus());

        CONTENT.onContainerRegister(MENU_TYPES);
        MENU_TYPES.register(context.getModEventBus());

        FMLJavaModLoadingContext loadingContext = context;
        loadingContext.getModEventBus().addListener(this::onSetup);
        loadingContext.getModEventBus().register(CONTENT);
    }

    private void onSetup(FMLCommonSetupEvent e) {
        ModContainer container = CONTEXT.getContainer();
        BLOCK_CONFIG = CompoundConfig.of(DavincisVesselsBlockConfig.class, container, "davincis-blocks.toml");
        PROXY.onSetup(e);

        CompoundNetwork.createNetwork(container, "davincisvessels");
    }

}
