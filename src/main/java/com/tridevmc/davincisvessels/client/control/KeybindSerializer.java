package com.tridevmc.davincisvessels.client.control;

import com.google.gson.Gson;
import com.tridevmc.compound.config.IConfigObjectSerializer;
import com.tridevmc.compound.config.RegisteredConfigObjectSerializer;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@RegisteredConfigObjectSerializer("davincisvessels")
@OnlyIn(Dist.CLIENT)
public class KeybindSerializer implements IConfigObjectSerializer<KeyMapping> {
    @Override
    public String toString(Class aClass, KeyMapping keyBinding) {
        return new Gson().toJson(keyBinding);
    }

    @Override
    public KeyMapping fromString(Class aClass, String s) {
        return new Gson().fromJson(s, KeyMapping.class);
    }

    @Override
    public boolean accepts(Class aClass) {
        return KeyMapping.class.isAssignableFrom(aClass);
    }
}
