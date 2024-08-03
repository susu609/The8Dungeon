package net.ss.sudungeon.client.resources;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;


public class SkinSelectionHandler {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void onCharacterSelection(String character) {
        if (minecraft.player == null || minecraft.level == null) {
            LogUtils.getLogger().error("Player or level is null. Cannot apply skin.");
            return;
        }

        ResourceLocation skinLocation;
        if ("STEVE".equals(character)) {
            skinLocation = new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");
        } else if ("ALEX".equals(character)) {
            skinLocation = new ResourceLocation("minecraft", "textures/entity/player/wide/alex.png");
        } else {
            LogUtils.getLogger().error("Invalid character selection: " + character);
            return;
        }

        applySkin(skinLocation);
    }

    private static void applySkin(ResourceLocation skinLocation) {
        Player player = minecraft.player;
        if (player instanceof ServerPlayer serverPlayer) {
            // Tùy chỉnh theo cách serverPlayer xử lý skin
            // Ví dụ: cập nhật texture hoặc gọi API của Minecraft
        }
    }
}