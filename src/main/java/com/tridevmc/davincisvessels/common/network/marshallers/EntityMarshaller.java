package com.tridevmc.davincisvessels.common.network.marshallers;

import java.nio.charset.Charset;

import com.tridevmc.compound.network.marshallers.Marshaller;
import com.tridevmc.compound.network.marshallers.RegisteredMarshaller;
import com.tridevmc.movingworld.MovingWorldMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@RegisteredMarshaller(channel = "davincisvessels", acceptedTypes = {Entity.class}, ids = {"entity"})
public class EntityMarshaller extends Marshaller<Entity> {

    @Override
    /*
     * dimId is a ResourceKey<Level> toString
     */
    public Entity readFrom(ByteBuf in) {
        if (in.readBoolean()) {
            int len = in.readInt();
            String dimID = in.readCharSequence(len, Charset.defaultCharset()).toString();
            int entityID = in.readInt();
            Level world = MovingWorldMod.PROXY.getWorld(dimID);
            return world.getEntity(entityID);
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(ByteBuf out, Entity entity) {
        if (entity != null) {
            out.writeBoolean(true);
            String dimID = entity.level().dimension().toString();
            out.writeInt(dimID.length());
            out.writeCharSequence(dimID, Charset.defaultCharset());
            out.writeInt(entity.getId());
        } else {
            out.writeBoolean(false);
        }
    }
}
