package net.susu609.the8dungeon.dungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Lưu seed của dungeon đã dựng vào world (file data/susu609_dungeon.dat).
 * Chỉ cần seed: cùng seed + cùng DungeonParams.DEFAULT luôn sinh lại đúng layout cũ.
 */
public final class DungeonSavedData extends SavedData {

    private static final String NAME = "susu609_dungeon";

    private boolean present;
    private long seed;

    public DungeonSavedData() {
    }

    // Dữ liệu dùng chung cho cả server, luôn đặt ở Overworld.
    public static DungeonSavedData get(ServerLevel anyLevel) {
        return anyLevel.getServer().overworld().getDataStorage()
                .computeIfAbsent(DungeonSavedData::load, DungeonSavedData::new, NAME);
    }

    public static DungeonSavedData load(CompoundTag tag) {
        DungeonSavedData data = new DungeonSavedData();
        data.present = tag.getBoolean("present");
        data.seed = tag.getLong("seed");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("present", present);
        tag.putLong("seed", seed);
        return tag;
    }

    public void set(long seed) {
        this.present = true;
        this.seed = seed;
        setDirty();   // báo Minecraft ghi xuống đĩa lần lưu tới
    }

    public boolean present() {
        return present;
    }

    public long seed() {
        return seed;
    }
}
