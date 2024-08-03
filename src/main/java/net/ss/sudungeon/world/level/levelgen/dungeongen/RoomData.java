package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class RoomData {
    private static final String TAG_ROOM_DATA_LIST = "RoomData";
    private static final String TAG_ROOM_TYPE = "RoomType"; // Khởi tạo giá trị

    public BlockPos pos;

    public RoomType type;
    public List<Entity> entities = new ArrayList<>();
    public EnumSet<Direction> connections; // Lưu trữ các hướng có lối đi
    public boolean spawned; // Biến để đánh dấu đã spawn quái vật hay chưa

    public RoomData (BlockPos pos, RoomType type, EnumSet<Direction> connections) {
        this(pos, type, connections, false); // Gọi constructor với 4 tham số
    }

    public RoomData (BlockPos pos, RoomType type, EnumSet<Direction> connections, boolean spawned) {
        this.pos = pos;
        this.type = type;
        this.connections = connections;
        this.spawned = spawned;
    }

    public CompoundTag writeToNBT (@NotNull CompoundTag tag) {
        tag.put("Pos", NbtUtils.writeBlockPos(pos));
        tag.putString("Type", type.name());
        int[] connectionsArray = connections.stream().mapToInt(Direction::get3DDataValue).toArray();
        tag.putIntArray("Connections", connectionsArray);
        tag.putBoolean("Spawned", spawned); // Lưu trạng thái spawned vào NBT tag
        tag.putString(TAG_ROOM_TYPE, type.name()); // Lưu loại phòng
        return tag;
    }

    public static RoomData readFromNBT (@NotNull CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
        RoomType type = RoomType.valueOf(tag.getString(TAG_ROOM_TYPE)); // Đọc loại phòng

        int[] connectionsArray = tag.contains("Connections") ? tag.getIntArray("Connections") : new int[0];
        EnumSet<Direction> connections = connectionsArray.length > 0 ?
                EnumSet.copyOf(Arrays.stream(connectionsArray).mapToObj(Direction::from3DDataValue).toList()) :
                EnumSet.noneOf(Direction.class);

        boolean spawned = tag.getBoolean("Spawned"); // Đọc trạng thái spawned từ NBT tag

        
        return new RoomData(pos, type, connections, spawned);
    }

    public boolean isSpawned () {
        return spawned;
    }

    public void setSpawned (boolean spawned) {
        this.spawned = spawned;
    }

    public boolean isInside(BlockPos blockPos) {
        int minX = pos.getX();
        int maxX = pos.getX() + DrunkardWalk.ROOM_SIZE - 1; // Kích thước phòng trừ đi 1 vì tọa độ bắt đầu từ 0
        int minY = pos.getY();
        int maxY = pos.getY() + DrunkardWalk.ROOM_SIZE - 1;
        int minZ = pos.getZ();
        int maxZ = pos.getZ() + DrunkardWalk.ROOM_SIZE - 1;

        return blockPos.getX() >= minX && blockPos.getX() <= maxX &&
                blockPos.getY() >= minY && blockPos.getY() <= maxY &&
                blockPos.getZ() >= minZ && blockPos.getZ() <= maxZ;
    }
}
