package net.susu609.the8dungeon.dungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Lưu seed của dungeon đã dựng vào world (file data/susu609_dungeon.dat).
 * Chỉ cần seed: cùng seed + cùng DungeonParams.DEFAULT luôn sinh lại đúng layout cũ.
 */
public final class DungeonSavedData extends SavedData {

    private static final String NAME = "susu609_dungeon";
    private final Map<Integer, RoomState> states = new HashMap<>();
    private boolean present;
    private long seed;

    public DungeonSavedData() {
    }

    // Dữ liệu dùng chung cho cả server, luôn đặt ở Overworld.
    public static DungeonSavedData get(ServerLevel anyLevel) {
        return anyLevel.getServer().overworld().getDataStorage().computeIfAbsent(DungeonSavedData::load, DungeonSavedData::new, NAME);
    }

    public static DungeonSavedData load(CompoundTag tag) {
        DungeonSavedData data = new DungeonSavedData();
        data.present = tag.getBoolean("present");
        data.seed = tag.getLong("seed");
        int[] ids = tag.getIntArray("room_ids");
        int[] st = tag.getIntArray("room_states");
        for (int i = 0; i < ids.length; i++) data.states.put(ids[i], RoomState.values()[st[i]]);
        return data;
    }

    public RoomState state(int roomId) {
        return states.getOrDefault(roomId, RoomState.HIDDEN);
    }

    public void setState(int roomId, RoomState s) {
        if (states.put(roomId, s) != s) setDirty();   // chỉ ghi khi thật sự đổi
    }

    public void set(long seed) {
        this.present = true;
        this.seed = seed;
        states.clear();
        setDirty();   // báo Minecraft ghi xuống đĩa lần lưu tới
    }

    public boolean present() {
        return present;
    }

    public long seed() {
        return seed;
    }

    public int[] knownRooms() {
        return states.entrySet().stream().filter(e -> e.getValue() == RoomState.CLEARED || e.getValue() == RoomState.COMBAT).mapToInt(Map.Entry::getKey).toArray();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putBoolean("present", present);
        tag.putLong("seed", seed);
        int[] ids = states.keySet().stream().mapToInt(Integer::intValue).toArray();
        int[] st = Arrays.stream(ids).map(id -> states.get(id).ordinal()).toArray();
        tag.putIntArray("room_ids", ids);
        tag.putIntArray("room_states", st);
        return tag;
    }

}
