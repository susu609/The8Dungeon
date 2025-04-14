package net.ss.sudungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonSavedData extends SavedData {

    private static final String TAG_ROOM_DATA_LIST = "RoomData";
    public static final String DATA_NAME = "sudungeon_data";

    private boolean isGenerating = false;
    private DrunkardWalk dungeonGenerator;
    private List<RoomData> roomDataList = new ArrayList<>();
    private long dungeonSeed;
    private final Map<BlockPos, RoomType> dungeonMap = new HashMap<>();
    private int mapOpenCount = 0;

    public static DungeonSavedData load (CompoundTag nbt) {
        DungeonSavedData data = new DungeonSavedData();
        data.read(nbt);
        return data;
    }

    public static DungeonSavedData get (ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                DungeonSavedData::load,
                DungeonSavedData::new,
                DATA_NAME
        );
    }

    @Override
    public @NotNull CompoundTag save (@NotNull CompoundTag compoundTag) {
        ListTag roomDataListTag = new ListTag();
        for (RoomData roomData : roomDataList) {
            CompoundTag roomDataTag = roomData.writeToNBT(new CompoundTag());
            roomDataListTag.add(roomDataTag);
        }
        compoundTag.put(TAG_ROOM_DATA_LIST, roomDataListTag);
        compoundTag.putLong("DungeonSeed", dungeonSeed);
        compoundTag.putBoolean("IsGenerating", isGenerating);
        compoundTag.putInt("MapOpenCount", mapOpenCount);
        return compoundTag;
    }

    public void read (CompoundTag nbt) {
        if (nbt == null) {
            return;
        }

        ListTag roomDataListTag = nbt.getList(TAG_ROOM_DATA_LIST, Tag.TAG_COMPOUND);
        roomDataList.clear();
        for (int i = 0; i < roomDataListTag.size(); i++) {
            CompoundTag roomDataTag = roomDataListTag.getCompound(i);
            RoomData roomData = RoomData.readFromNBT(roomDataTag);
            roomDataList.add(roomData);
        }
        isGenerating = nbt.getBoolean("IsGenerating");
        dungeonSeed = nbt.getLong("DungeonSeed");
        mapOpenCount = nbt.getInt("MapOpenCount");
    }

    public boolean isGenerating () {
        return isGenerating;
    }

    public void setGenerating (boolean generating) {
        isGenerating = generating;
        setDirty();
    }

    public long getDungeonSeed () {
        return dungeonSeed;
    }

    public void setDungeonSeed (long dungeonSeed) {
        this.dungeonSeed = dungeonSeed;
        setDirty();
    }

    public List<RoomData> getRooms () {
        return roomDataList;
    }

    public void setRooms (List<RoomData> rooms) {
        if (rooms != null) {
            this.roomDataList = rooms;
            setDirty();
        }
    }

    public void clearRooms () {
        roomDataList.clear();
        setDirty();
    }

    public void addRoomToMap (BlockPos pos, RoomType type) {
        if (pos != null && type != null) {
            if (!dungeonMap.containsKey(pos)) {
                dungeonMap.put(pos, type);
                System.out.println("Thêm phòng mới tại vị trí: " + pos + " với loại phòng: " + type);
                setDirty();
            } else {
                System.err.println("Lỗi: Phòng tại vị trí " + pos + " đã tồn tại!");
            }
        }
    }

    public void removeRoomFromMap (BlockPos pos) {
        if (pos != null) {
            dungeonMap.remove(pos);
            System.out.println("Đã xóa phòng tại vị trí: " + pos);
            setDirty();
        }
    }

    public RoomType getRoomTypeAt (BlockPos pos) {
        return dungeonMap.get(pos);
    }

    public boolean hasRoomAt (BlockPos pos) {
        return dungeonMap.containsKey(pos);
    }

    public DrunkardWalk getDungeonGenerator () {
        if (dungeonGenerator == null) {
            dungeonGenerator = new DrunkardWalk(RoomType.NORMAL);
        }
        return dungeonGenerator;
    }

    public void setDungeonGenerator (DrunkardWalk dungeonGenerator) {
        this.dungeonGenerator = dungeonGenerator;
        setDirty();
    }

    public boolean areAllRoomsDiscovered () {
        for (RoomData room : roomDataList) {
            if (!room.isDiscovered()) {
                System.out.println("Phòng tại vị trí " + room.getPosition() + " chưa được khám phá.");
                return false;
            }
        }
        return true;
    }

    public void incrementMapOpenCount () {
        mapOpenCount++;
        System.out.println("Số lần mở bản đồ đã tăng lên: " + mapOpenCount);
        setDirty();
    }

    public int getMapOpenCount () {
        return mapOpenCount;
    }

    public int getMinX () {
        return roomDataList.stream()
                .mapToInt(room -> room.getPosition().getX())
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    public int getMinZ () {
        return roomDataList.stream()
                .mapToInt(room -> room.getPosition().getZ())
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    public boolean isPlayerInRoom (BlockPos playerPos, RoomData room) {
        ChunkPos playerChunk = new ChunkPos(playerPos);
        ChunkPos roomChunk = room.getChunkPosition();
        return playerChunk.equals(roomChunk);
    }

    private static BlockPos lastPlayerPos = BlockPos.ZERO;

    @SubscribeEvent
    public static void onPlayerTick (TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide()) {
            return; // Chỉ chạy trên server
        }

        BlockPos currentPos = player.blockPosition();

        // Kiểm tra nếu người chơi đã di chuyển từ block này sang block khác
        if (!currentPos.equals(lastPlayerPos)) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            DungeonSavedData dungeonData = DungeonSavedData.get(serverLevel);
            dungeonData.updateRoomDiscoveryStatus(currentPos);
            lastPlayerPos = currentPos;
        }
    }

    public void updateRoomDiscoveryStatus (BlockPos playerPos) {
        for (RoomData room : this.getRooms()) {
            if (!room.isDiscovered() && isPlayerInRoom(playerPos, room)) {
                room.setDiscovered(true);
                this.setDirty(true);
            }
        }
    }
}
