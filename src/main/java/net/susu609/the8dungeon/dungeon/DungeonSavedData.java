package net.susu609.the8dungeon.dungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Lưu seed của dungeon đã dựng vào world (file data/susu609_dungeon.dat).
 * Chỉ cần seed: cùng seed + cùng DungeonParams.DEFAULT luôn sinh lại đúng layout cũ.
 */
public final class DungeonSavedData extends SavedData {

    private static final String NAME = "susu609_dungeon";
    private final Set<Integer> visited = new HashSet<>();
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
        for (int id : tag.getIntArray("visited")) data.visited.add(id);                       // load
        return data;
    }

    public Set<Integer> visited() {
        return visited;
    }

    public boolean markVisited(int roomId) {
        if (!visited.add(roomId)) return false;   // đã có rồi
        setDirty();
        return true;                              // phòng mới
    }

    public void set(long seed) {
        this.present = true;
        this.seed = seed;
        visited.clear();
        setDirty();   // báo Minecraft ghi xuống đĩa lần lưu tới
    }

    public boolean present() {
        return present;
    }

    public long seed() {
        return seed;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putBoolean("present", present);
        tag.putLong("seed", seed);
        tag.putIntArray("visited", visited.stream().mapToInt(Integer::intValue).toArray());   // save
        return tag;
    }

}
