package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;

public class PlayerSkinProvider {
    private static ISkinProvider instance;

    public static void register(ISkinProvider provider) {
        instance = provider;
    }

    public static ResourceLocation getSkin(GameProfile profile, String character) {
        return instance.getSkin(profile, character);
    }
}
