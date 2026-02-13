package com.tridevmc.davincisvessels.client.handler;

import com.tridevmc.davincisvessels.common.entity.EntityVessel;
import com.tridevmc.davincisvessels.common.handler.CommonHookContainer;
import com.tridevmc.movingworld.api.MovingWorldNetworkHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientHookContainer extends CommonHookContainer {

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof EntityVessel) {
            EntityVessel vessel = (EntityVessel) event.getEntity();
            if (vessel.getMobileChunk().chunkTileEntityMap.isEmpty()) {
                return;
            }

            MovingWorldNetworkHelper.sendDataRequestMessage(vessel);
        }
    }
}
