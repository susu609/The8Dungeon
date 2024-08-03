package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;

public enum RoomType {
    BOSS("dungeon_boss", 0.1f, 1, 1.0f, new RoomTypeConfig(0.1f, 1, 1.0f)),
    ELITE("dungeon_elite", 0.1f, 2, 1.0f, new RoomTypeConfig(0.1f, 2, 1.0f)),
    EVENT("dungeon_event", 0.2f, 3, 2.0f, new RoomTypeConfig(0.2f, 3, 2.0f)),
    NORMAL("dungeon_normal", 0.6f, -1, 5.0f, new RoomTypeConfig(0.6f, -1, 5.0f)),
    SHOP("dungeon_shop", 0.05f, 1, 1.0f, new RoomTypeConfig(0.05f, 1, 1.0f)),
    START("dungeon_start", 1.0f, 1, 1.0f, new RoomTypeConfig(1.0f, 1, 1.0f)),
    TREASURE("dungeon_treasure", 0.15f, 1, 1.0f, new RoomTypeConfig(0.15f, 1, 1.0f));
    private final ResourceLocation templateLocation;
    private final RoomTypeConfig defaultConfig; // Khai báo defaultConfig
    private RoomTypeConfig config; // Giữ lại config

    RoomType(String templateName, float spawnProbability, int maxCount, float weight, RoomTypeConfig defaultConfig) {
        this.templateLocation = new ResourceLocation(SsMod.MODID, templateName);
        this.defaultConfig = defaultConfig;
        this.config = defaultConfig; // Khởi tạo config bằng defaultConfig
    }

    // Getter cho templateLocation (tên file cấu trúc)
    public ResourceLocation getTemplateLocation () {
        return templateLocation;
    }

    // Getter và Setter cho RoomTypeConfig
    public RoomTypeConfig getConfig() {
        return config; // Trả về config hiện tại
    }

    public void setDefaultConfig() {
        this.config = defaultConfig; // Đặt lại config về giá trị mặc định
    }

    public void setConfig (RoomTypeConfig config) {
        this.config = config;
    }

    public static RoomType fromString (String string) {
        for (RoomType roomType : RoomType.values()) {
            if (roomType.getName().equals(string)) {
                return roomType;
            }
        }
        throw new IllegalArgumentException("Invalid RoomType name: " + string);
    }

    public String getName () {
        return this.templateLocation.getPath().replace("dungeon_", "");
    }

    public RoomTypeConfig getDefaultConfig () {
        return defaultConfig; // Trả về cấu hình mặc định của loại phòng
    }
}