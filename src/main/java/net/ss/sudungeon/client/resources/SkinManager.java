package net.ss.sudungeon.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;


public class SkinManager {
    private static final ResourceLocation STEVE_SKIN = new ResourceLocation("ss", "textures/entity/player/wide/steve.png");
    private static final ResourceLocation DEFAULT_SKIN = STEVE_SKIN;

    private final TextureManager textureManager;
    private static final HashMap<String, ResourceLocation> customSkins = new HashMap<>();

    public SkinManager () {
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }

    public static void addCustomSkin (String username, ResourceLocation skinLocation) {
        customSkins.put(username, skinLocation);
    }

    public static ResourceLocation getCustomSkin (AbstractClientPlayer player) {
        String playerName = player.getName().getString();
        return customSkins.getOrDefault(playerName, DEFAULT_SKIN);
    }
}