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
    private static final String TAG_POS = "Pos";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_CONNECTIONS = "Connections";
    private static final String TAG_SPAWNED = "Spawned";

    public BlockPos pos;
    public RoomType type;
    public List<Entity> entities = new ArrayList<>();
    public EnumSet<Direction> connections;
    public boolean spawned;

    // Constructor with all parameters
    public RoomData(BlockPos pos, RoomType type, EnumSet<Direction> connections, boolean spawned) {
        this.pos = pos;
        this.type = type;
        this.connections = connections != null ? connections : EnumSet.noneOf(Direction.class);
        this.spawned = spawned;
    }

    // Method to write data to NBT
    public CompoundTag writeToNBT(@NotNull CompoundTag tag) {
        tag.put(TAG_POS, NbtUtils.writeBlockPos(pos));
        tag.putString(TAG_TYPE, type.name());
        int[] connectionsArray = connections.stream().mapToInt(Direction::get3DDataValue).toArray();
        tag.putIntArray(TAG_CONNECTIONS, connectionsArray);
        tag.putBoolean(TAG_SPAWNED, spawned);
        return tag;
    }

    // Method to read data from NBT
    public static RoomData readFromNBT(@NotNull CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(TAG_POS));
        RoomType type = RoomType.valueOf(tag.getString(TAG_TYPE));

        int[] connectionsArray = tag.contains(TAG_CONNECTIONS) ? tag.getIntArray(TAG_CONNECTIONS) : new int[0];
        EnumSet<Direction> connections = connectionsArray.length > 0
                ? EnumSet.copyOf(Arrays.stream(connectionsArray).mapToObj(Direction::from3DDataValue).toList())
                : EnumSet.noneOf(Direction.class);

        boolean spawned = tag.getBoolean(TAG_SPAWNED);

        return new RoomData(pos, type, connections, spawned);
    }

    // Check if the room has been spawned
    public boolean isSpawned() {
        return spawned;
    }

    // Set the spawned state
    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }

    // Check if a position is inside the room
    public boolean isInside(BlockPos blockPos) {
        int minX = pos.getX();
        int maxX = pos.getX() + DrunkardWalk.ROOM_SIZE - 1;
        int minY = pos.getY();
        int maxY = pos.getY() + DrunkardWalk.ROOM_SIZE - 1;
        int minZ = pos.getZ();
        int maxZ = pos.getZ() + DrunkardWalk.ROOM_SIZE - 1;

        return blockPos.getX() >= minX && blockPos.getX() <= maxX &&
                blockPos.getY() >= minY && blockPos.getY() <= maxY &&
                blockPos.getZ() >= minZ && blockPos.getZ() <= maxZ;
    }

    // Get the position of the room
    public BlockPos getPosition() {
        return pos;
    }
}