package com.tridevmc.davincisvessels.common.content;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.common.DavincisUIHooks;
import com.tridevmc.davincisvessels.common.content.block.*;
import com.tridevmc.davincisvessels.common.content.item.ItemBlockAnchorPoint;
import com.tridevmc.davincisvessels.common.content.item.ItemSecuredBed;
import com.tridevmc.davincisvessels.common.entity.EntityParachute;
import com.tridevmc.davincisvessels.common.entity.EntitySeat;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.tileentity.TileAnchorPoint;
import com.tridevmc.davincisvessels.common.tileentity.TileCrate;
import com.tridevmc.davincisvessels.common.tileentity.TileEngine;
import com.tridevmc.davincisvessels.common.tileentity.TileEntitySecuredBed;
import com.tridevmc.davincisvessels.common.tileentity.TileGauge;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DavincisVesselsContent {

    public RegistryObject<Block> blockHelm;
    public RegistryObject<Block> blockFloater;
    public RegistryObject<Block> blockGauge;
    public RegistryObject<Block> blockGaugeExtended;
    public RegistryObject<Block> blockSeat;
    public RegistryObject<Block> blockStickyBuffer;
    public RegistryObject<Block> blockBuffer;
    public RegistryObject<Block> blockEngine;
    public RegistryObject<Block> blockCrate;
    public RegistryObject<Block> blockAnchorPoint;
    public RegistryObject<Block> blockSecuredBed;
    public List<RegistryObject<Block>> balloonBlocks;

    public RegistryObject<Item> itemSecuredBed;

    public MenuType<? extends AbstractContainerMenu> universalContainerType;

    // public ItemGroup itemGroup = new ItemGroup("davincisTab") {
    //     @Override
    //     public ItemStack createIcon() {
    //         return new ItemStack(blockHelm);
    //     }
    // };

    private class ItemBlockToRegister {
        public String id;
        public Supplier<BlockItem> item;

        public ItemBlockToRegister(String id, Supplier<BlockItem> item) {
            this.id = id;
            this.item = item;
        }
    }

    public Map<Class<? extends BlockEntity>, RegistryObject<BlockEntityType<?>>> tileTypes = Maps.newHashMap();
    public Map<Class<? extends Entity>, RegistryObject<EntityType<?>>> entityTypes = Maps.newHashMap();

    public Function<Properties, Properties> materialFloater;
    public HashMap<String, RegistryObject<? extends Block>> registeredBlocks;
    public HashMap<String, RegistryObject<? extends Item>> registeredItems;
    private List<ItemBlockToRegister> itemBlocksToRegister;

    public void onTileRegister(DeferredRegister<BlockEntityType<?>> registry) {
        registerTileEntity(registry, "helm", TileHelm::new, TileHelm.class);
        registerTileEntity(registry, "gauge", TileGauge::new, TileGauge.class);
        registerTileEntity(registry, "crate", TileCrate::new, TileCrate.class);
        registerTileEntity(registry, "engine", TileEngine::new, TileEngine.class);
        registerTileEntity(registry, "anchor_point", TileAnchorPoint::new, TileAnchorPoint.class);
        registerTileEntity(registry, "secured_bed", TileEntitySecuredBed::new, TileEntitySecuredBed.class);
    }

    public void onEntityRegister(DeferredRegister<EntityType<?>> registry) {
        registerEntity(registry, "vesselmod", 64, DavincisVesselsMod.CONFIG.vesselEntitySyncRate, true, EntityVessel.class, EntityVessel::new);
        registerEntity(registry, "attachment_seat", 64, 20, false, EntitySeat.class, EntitySeat::new);
        registerEntity(registry, "parachute", 32, DavincisVesselsMod.CONFIG.vesselEntitySyncRate, true, EntityParachute.class, EntityParachute::new);
    }

    public void onItemRegister(DeferredRegister<Item> registry) {
        registeredItems = new HashMap<>();

        itemSecuredBed = registerItem(registry, "secured_bed", ItemSecuredBed::new);

        for (ItemBlockToRegister itemBlockToRegister : itemBlocksToRegister) {
            registry.register(itemBlockToRegister.id, itemBlockToRegister.item);
        }
    }

    public void onBlockRegister(DeferredRegister<Block> registry, DeferredRegister<CreativeModeTab> creativeRegistry) {
        registeredBlocks = Maps.newHashMap();
        itemBlocksToRegister = Lists.newArrayList();
        materialFloater = p -> p.mapColor(MapColor.WOOL).ignitedByLava();
        //new Material(MaterialColor.WOOL, false, true, true, true, true, true, false, PushReaction.NORMAL);

        this.balloonBlocks = new ArrayList<>();
        for (DyeColor colour : DyeColor.values()) {
            Supplier<Block> balloon = () -> new BlockBalloon(colour);
            RegistryObject<Block> balloonRO = registerBlock(registry, colour.getSerializedName() + "_balloon", balloon);
            balloonBlocks.add(balloonRO);
        }

        Supplier<Block> blockHelmSupplier = () -> new BlockHelm(Block.Properties.of().strength(1F).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).ignitedByLava());
        blockHelm = registerBlock(registry, "helm", blockHelmSupplier);

        Supplier<Block> blockFloaterSupplier = () -> new BlockAS(materialFloater, SoundType.WOOD);
        blockFloater = registerBlock(registry, "floater", blockFloaterSupplier);

        Supplier<Block> blockGaugeSupplier = BlockGauge::new;
        blockGauge = registerBlock(registry, "gauge", blockGaugeSupplier);

        Supplier<Block> blockGaugeExtendedSupplier = BlockGauge::new;
        blockGaugeExtended = registerBlock(registry, "gauge_ext", blockGaugeExtendedSupplier);

        Supplier<Block> blockSeatSupplier = BlockSeat::new;
        blockSeat = registerBlock(registry, "seat", blockSeatSupplier);

        Supplier<Block> blockBufferSupplier = () -> new BlockAS(p -> p.mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.GUITAR).ignitedByLava(), SoundType.WOOD, 0.8F, 0.8F);
        blockBuffer = registerBlock(registry, "buffer", blockBufferSupplier);

        Supplier<Block> blockStickyBufferSupplier = () -> new BlockAS(p -> p.mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.GUITAR).ignitedByLava(), SoundType.WOOD, 0.8F, 0.8F);
        blockStickyBuffer = registerBlock(registry, "sticky_buffer", blockStickyBufferSupplier);

        Supplier<Block> blockEngineSupplier = () -> new BlockEngine(1F, DavincisVesselsMod.CONFIG.engineConsumptionRate);
        blockEngine = registerBlock(registry, "engine", blockEngineSupplier);

        Supplier<Block> blockCrateSupplier = BlockCrate::new;
        blockCrate = registerBlock(registry, "crate_wood", blockCrateSupplier);

        Supplier<Block> blockAnchorPointSupplier = BlockAnchorPoint::new;
        blockAnchorPoint = registerBlock(registry, "anchor_point", blockAnchorPointSupplier, () -> new ItemBlockAnchorPoint(blockAnchorPoint.get()));

        Supplier<Block> blockSecuredBedSupplier = BlockSecuredBed::new;
        blockSecuredBed = registerBlock(registry, "secured_bed", blockSecuredBedSupplier, false);

        creativeRegistry.register("davincistab", () -> CreativeModeTab.builder().title(Component.literal("davincisTab")).icon(() -> {
            return new ItemStack(blockHelm.get());
        }).displayItems((params, out) -> {
            out.accept(blockHelm.get());
            out.accept(blockFloater.get());
            out.accept(blockGauge.get());
            out.accept(blockGaugeExtended.get());
            out.accept(blockSeat.get());
            out.accept(blockBuffer.get());
            out.accept(blockStickyBuffer.get());
            out.accept(blockEngine.get());
            out.accept(blockCrate.get());
            out.accept(blockAnchorPoint.get());
        }).build());
    }

    public void onContainerRegister(DeferredRegister<MenuType<?>> registry) {
        universalContainerType = DavincisUIHooks.register(registry);
        // DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DavincisUIHooks::registerScreens);
    }

    private <B extends Block> RegistryObject<Block> registerBlock(DeferredRegister<Block> registry, String id, Supplier<B> block) {
        return registerBlock(registry, id, block, true);
    }

    private <B extends Block> RegistryObject<Block> registerBlock(DeferredRegister<Block> registry, String id, Supplier<B> block, boolean withItemBlock) {
        // block.setRegistryName(REGISTRY_PREFIX, id);
        RegistryObject<Block> registryObject = registry.register(id, block);
        if (withItemBlock)
            itemBlocksToRegister.add(new ItemBlockToRegister(id, () -> {
                var out = new BlockItem(registryObject.get(), new Item.Properties());
                if (out.getBlock() == Blocks.AIR) {
                    new IllegalStateException("Block "+id);
                }
                return out;
            })); // .setRegistryName(block.getRegistryName()));
        registeredBlocks.put(id, registryObject);
        return registryObject;
    }

    private <B extends Block> RegistryObject<Block> registerBlock(DeferredRegister<Block> registry, String id, Supplier<B> block, Supplier<BlockItem> itemBlockClass) {
        // try {
        // block.setRegistryName(REGISTRY_PREFIX, id);
        RegistryObject<Block> registryObject = registry.register(id, block);
        // itemBlock.setRegistryName(REGISTRY_PREFIX, id);
        itemBlocksToRegister.add(new ItemBlockToRegister(id, itemBlockClass));
        registeredBlocks.put(id, registryObject);
        return registryObject;
        // } catch (Exception e) {
        //     DavincisVesselsMod.LOG.error("Caught exception while registering " + block, e);
        // }
    }

    private <I extends Item> RegistryObject<Item> registerItem(DeferredRegister<Item> registry, String id, Supplier<I> item) {
        // item.setRegistryName(REGISTRY_PREFIX, id);
        RegistryObject<Item> registryObject = registry.register(id, item);
        registeredItems.put(id, registryObject);
        return registryObject;
    }

    private void registerTileEntity(DeferredRegister<BlockEntityType<?>> registry, String id, BlockEntitySupplier tileSupplier, Class<? extends BlockEntity> c) {
        // Type<?> dataFixer = null;

        // try {
        // } catch (IllegalArgumentException e) {
        //     if (SharedConstants.IS_RUNNING_IN_IDE) {
        //         throw e;
        //     }
        // }

        // tileType.setRegistryName(id);
        RegistryObject<BlockEntityType<?>> registryObject = registry.register(id, () -> BlockEntityType.Builder.of(tileSupplier).build(null));
        this.tileTypes.put(c, registryObject);
        
    }

    private void registerEntity(DeferredRegister<EntityType<?>> registry, String id,
                                int range, int updateFrequency, boolean sendVelocityUpdates,
                                Class<? extends Entity> clazz, Function<? super Level, ? extends Entity> entityCreator) {
        EntityType.Builder entityType = EntityType.Builder.of((t, world) -> entityCreator.apply(world), MobCategory.MISC)
                .setTrackingRange(range)
                .setUpdateInterval(updateFrequency)
                .setShouldReceiveVelocityUpdates(sendVelocityUpdates)
                .noSummon()
                .setCustomClientFactory((spawnEntity, world) -> entityCreator.apply(world));

                // .build(id.toString());
        // entityType.setRegistryName(id);
        RegistryObject<EntityType<?>> registryObject = registry.register(id, () -> entityType.build(id.toString()));
        this.entityTypes.put(clazz, registryObject);
    }
}
