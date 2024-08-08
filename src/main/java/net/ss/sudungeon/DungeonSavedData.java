package net.ss.sudungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DungeonSavedData extends SavedData {

    private boolean isGenerating = false;
    private DrunkardWalk dungeonGenerator;
    private List<RoomData> roomDataList = new ArrayList<>();
    private long dungeonSeed; // Seed của dungeon
    private static final String TAG_ROOM_DATA_LIST = "RoomData";
    public static final String DATA_NAME = "sudungeon_data";


    public void setRooms (List<RoomData> rooms) {
        this.roomDataList = rooms;
        this.setDirty(); // Đánh dấu dữ liệu đã thay đổi
    }

    public static DungeonSavedData load (CompoundTag nbt) {
        DungeonSavedData data = new DungeonSavedData();
        data.read(nbt);
        return data;
    }

    public static DungeonSavedData get (ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                DungeonSavedData::load, // Function to load the data if it doesn't exist
                () -> {
                    DungeonSavedData data = new DungeonSavedData();
                    level.getDataStorage().set(DATA_NAME, data); // Lưu trữ instance mới vào DataStorage
                    return data;
                },
                DATA_NAME
        );
    }

    @Override
    public @NotNull CompoundTag save (@NotNull CompoundTag compoundTag) {
        try {
            ListTag roomDataListTag = new ListTag();
            for (RoomData roomData : roomDataList) {
                CompoundTag roomDataTag = new CompoundTag();
                roomDataTag = roomData.writeToNBT(roomDataTag);
                roomDataListTag.add(roomDataTag);
            }
            compoundTag.put(TAG_ROOM_DATA_LIST, roomDataListTag); // Chỉ sử dụng TAG_ROOM_DATA_LIST
            compoundTag.putLong("DungeonSeed", dungeonSeed);
            compoundTag.putBoolean("IsGenerating", isGenerating);
        } catch (Exception e) {
            SsMod.LOGGER.error("Lỗi khi lưu DungeonSavedData: {}", e.getMessage());
        }
        return compoundTag;
    }

    public void read (CompoundTag nbt) {
        try {
            ListTag roomDataListTag = nbt.getList(TAG_ROOM_DATA_LIST, Tag.TAG_COMPOUND); // Sử dụng TAG_ROOM_DATA_LIST
            for (int i = 0; i < roomDataListTag.size(); i++) {
                CompoundTag roomDataTag = roomDataListTag.getCompound(i);
                RoomData roomData = RoomData.readFromNBT(roomDataTag);
                roomDataList.add(roomData);
            }
            isGenerating = nbt.getBoolean("IsGenerating");
            dungeonSeed = nbt.getLong("DungeonSeed");
        } catch (Exception e) {
            SsMod.LOGGER.error("Lỗi khi đọc DungeonSavedData: {}", e.getMessage());
        }
    }

    public boolean isGenerating () {
        return isGenerating;
    }

    public void setGenerating (boolean generating) {
        isGenerating = generating;
        setDirty(); // Đánh dấu dữ liệu đã thay đổi
    }

    // Getter và Setter cho dungeonSeed
    public long getDungeonSeed () {
        return dungeonSeed;
    }

    public void setDungeonSeed (long dungeonSeed) {
        this.dungeonSeed = dungeonSeed;
    }

    // Getter cho roomDataList
    public List<RoomData> getRooms () {
        return roomDataList;
    }

    public void setRoomDataList (List<RoomData> roomDataList) {
        this.roomDataList = roomDataList;
        this.setDirty();
    }

    public DrunkardWalk getDungeonGenerator () {
        return dungeonGenerator;
    }

    public void setDungeonGenerator (DrunkardWalk dungeonGenerator) {
        this.dungeonGenerator = dungeonGenerator;
        this.setDirty();
    }

    public void clearRooms () {
        // Clear the list of rooms
        roomDataList.clear();

        // Mark the data as dirty to ensure it gets saved
        setDirty();
    }
}