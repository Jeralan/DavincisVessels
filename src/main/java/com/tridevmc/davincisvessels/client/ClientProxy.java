package com.tridevmc.davincisvessels.client;

import com.tridevmc.compound.config.CompoundConfig;
import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.control.DavincisKeybinds;
import com.tridevmc.davincisvessels.client.control.VesselKeyHandler;
import com.tridevmc.davincisvessels.client.handler.ClientHookContainer;
import com.tridevmc.davincisvessels.client.render.RenderParachute;
import com.tridevmc.davincisvessels.client.render.RenderSeat;
import com.tridevmc.davincisvessels.client.render.TileEntityGaugeRenderer;
import com.tridevmc.davincisvessels.client.render.TileEntityHelmRenderer;
import com.tridevmc.davincisvessels.common.CommonProxy;
import com.tridevmc.davincisvessels.common.entity.EntityParachute;
import com.tridevmc.davincisvessels.common.entity.EntitySeat;
import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.tileentity.TileGauge;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import com.tridevmc.movingworld.client.render.RenderMovingWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientProxy extends CommonProxy {

    public DavincisKeybinds keybinds;

    @Override
    public void onSetup(FMLCommonSetupEvent e) {
        super.onSetup(e);
        MinecraftForge.EVENT_BUS.register(new ClientHookContainer());
        registerKeyHandlers();
        registerRenderers();
    }

    private void registerKeyHandlers() {
        keybinds = CompoundConfig.of(DavincisKeybinds.class, DavincisVesselsMod.CONTEXT.getContainer());
        keybinds.addToControlsMenu();
        MinecraftForge.EVENT_BUS.register(new VesselKeyHandler(keybinds));
    }

    private void registerRenderers() {
        registerEntityRenderers();
        registerTileRenderers();
    }

    private void registerEntityRenderers() {
        EntityRenderers.register(((EntityType<EntityVessel>) DavincisVesselsMod.CONTENT.entityTypes.get(EntityVessel.class).get()), RenderMovingWorld::new);
        EntityRenderers.register(((EntityType<EntityParachute>) DavincisVesselsMod.CONTENT.entityTypes.get(EntityParachute.class).get()), RenderParachute::new);
        EntityRenderers.register(((EntityType<EntitySeat>) DavincisVesselsMod.CONTENT.entityTypes.get(EntitySeat.class).get()), RenderSeat::new);
    }

    private void registerTileRenderers() {
        BlockEntityRenderers.register(((BlockEntityType<TileGauge>) DavincisVesselsMod.CONTENT.tileTypes.get(TileGauge.class).get()), x -> new TileEntityGaugeRenderer());
        BlockEntityRenderers.register(((BlockEntityType<TileHelm>) DavincisVesselsMod.CONTENT.tileTypes.get(TileHelm.class).get()), x -> new TileEntityHelmRenderer());
    }

}
