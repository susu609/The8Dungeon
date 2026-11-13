package net.susu609.the8dungeon.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Lưu API key riêng của từng người chơi (dữ liệu PlayerPersisted, giữ lại khi chết).
 * Lưu ý: chủ server đọc được file lưu của người chơi.
 */
public final class ClawdKeyStore {

    private static final String KEY = "susu609_clawd_api_key";

    private ClawdKeyStore() {}

    public static String get(Player player) {
        CompoundTag data = data(player);
        return data.contains(KEY) ? data.getString(KEY) : null;
    }

    public static void set(Player player, String apiKey) {
        data(player).putString(KEY, apiKey);
        ClawdBrain.forgetClient(player.getUUID());
    }

    public static void clear(Player player) {
        data(player).remove(KEY);
        ClawdBrain.forgetClient(player.getUUID());
    }

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
