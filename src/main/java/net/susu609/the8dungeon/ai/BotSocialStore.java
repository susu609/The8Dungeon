package net.susu609.the8dungeon.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Điểm thân thiết của từng bot với từng người/bot khác, lưu theo thế giới (qua cả khởi động lại và hồi sinh). */
public final class BotSocialStore extends SavedData {

    private static final String NAME = "susu609_bot_social";

    private final Map<String, Map<UUID, Float>> data = new HashMap<>();

    public static BotSocialStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BotSocialStore::load, BotSocialStore::new, NAME);
    }

    Map<UUID, Float> all(String bot) {
        return data.getOrDefault(bot, Map.of());
    }

    void put(String bot, UUID other, float trust) {
        Float old = data.computeIfAbsent(bot, k -> new HashMap<>()).put(other, trust);
        if (old == null || old != trust) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        data.forEach((bot, map) -> {
            CompoundTag t = new CompoundTag();
            map.forEach((id, v) -> t.putFloat(id.toString(), v));
            tag.put(bot, t);
        });
        return tag;
    }

    private static BotSocialStore load(CompoundTag tag) {
        BotSocialStore s = new BotSocialStore();
        for (String bot : tag.getAllKeys()) {
            CompoundTag t = tag.getCompound(bot);
            Map<UUID, Float> map = new HashMap<>();
            for (String id : t.getAllKeys()) {
                try {
                    map.put(UUID.fromString(id), t.getFloat(id));
                } catch (IllegalArgumentException ignored) {
                }
            }
            s.data.put(bot, map);
        }
        return s;
    }
}
