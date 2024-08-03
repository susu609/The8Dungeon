package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;

public interface ISkinProvider {
    ResourceLocation getSkin(GameProfile profile, String character);
}