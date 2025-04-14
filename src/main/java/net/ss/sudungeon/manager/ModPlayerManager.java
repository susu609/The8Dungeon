package net.ss.sudungeon.manager;

import net.minecraft.world.entity.player.Player;
import net.ss.sudungeon.world.entity.ModPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModPlayerManager {
    private static final Map<UUID, ModPlayerEntity> map = new HashMap<>();

    public static void setRepEntity(Player player, ModPlayerEntity e) {
        map.put(player.getUUID(), e);
    }

    public static ModPlayerEntity getRepEntity(Player player) {
        return map.get(player.getUUID());
    }

    public static boolean hasRepEntity(Player player) {
        return map.containsKey(player.getUUID());
    }

    public static void clearRepEntity(Player player) {
        map.remove(player.getUUID());
    }

    public static boolean isMorphed(Player player) {
        return hasRepEntity(player);
    }
}
