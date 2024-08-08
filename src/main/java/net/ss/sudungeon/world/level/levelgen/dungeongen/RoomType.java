package net.ss.sudungeon.world.level.levelgen.dungeongen;

public enum RoomType {
    BOSS("boss", new RoomTypeConfig(0.1f, 1, 1.0f)),
    ELITE("elite", new RoomTypeConfig(0.1f, 2, 1.0f)),
    EVENT("event", new RoomTypeConfig(0.2f, 3, 2.0f)),
    NORMAL("normal", new RoomTypeConfig(0.6f, -1, 5.0f)),
    SHOP("shop", new RoomTypeConfig(0.05f, 1, 1.0f)),
    START("start", new RoomTypeConfig(1.0f, 1, 1.0f)),
    TREASURE("treasure", new RoomTypeConfig(0.15f, 1, 1.0f));

    private final String name;
    private final RoomTypeConfig config;

    RoomType(String name, RoomTypeConfig config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return this.name;
    }

    public RoomTypeConfig getConfig() {
        return config;
    }

    public static RoomType fromString(String name) {
        for (RoomType roomType : RoomType.values()) {
            if (roomType.name.equalsIgnoreCase(name)) {
                return roomType;
            }
        }
        throw new IllegalArgumentException("Invalid RoomType name: " + name);
    }
}