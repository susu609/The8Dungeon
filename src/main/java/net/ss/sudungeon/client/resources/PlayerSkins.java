package net.ss.sudungeon.client.resources;

import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;

public enum PlayerSkins {
    STEVE("textures/entity/player/steve.png"), // Sử dụng skin Steve từ vanilla
    ALEX("textures/entity/player/alex.png"),
    // Thêm các skin tùy chỉnh khác của bạn ở đây
    ;

    private final ResourceLocation skinLocation;

    PlayerSkins(String path) {
        this.skinLocation = new ResourceLocation(SsMod.MODID, path);
    }

    public ResourceLocation getSkinLocation() {
        return skinLocation;
    }
}