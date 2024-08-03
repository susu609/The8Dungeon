package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;

public class DefaultSkinProvider implements ISkinProvider {
    @Override
    public ResourceLocation getSkin(GameProfile profile, String character) {
        switch (character) {
            case "menu.character.steve":
                return PlayerSkins.STEVE.getSkinLocation();
            case "menu.character.alex":
                return PlayerSkins.ALEX.getSkinLocation();
            // Thêm các skin tùy chỉnh khác ở đây
            default:
                return PlayerSkins.STEVE.getSkinLocation(); // Skin mặc định
        }
    }
}