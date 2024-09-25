package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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
    private static final String TAG_DISCOVERED = "Discovered";
    private static final String TAG_TRIGGERED = "Triggered";
    private static final String TAG_DEAD_END = "DeadEnd";
    private static final String TAG_ENTITIES = "Entities";

    private final BlockPos pos;
    private final RoomType type;
    private final EnumSet<Direction> connections;
    private final List<Entity> entities = new ArrayList<>();
    private boolean spawned;
    private boolean discovered;
    private boolean triggered;
    private boolean isDeadEnd;

    // Constructor
    public RoomData(BlockPos pos, RoomType type, EnumSet<Direction> connections, boolean spawned) {
        this(pos, type, connections, spawned, false, false);
    }

    public RoomData (BlockPos pos, RoomType type, EnumSet<Direction> connections, boolean spawned, boolean discovered, boolean triggered) {
        this.pos = pos;
        this.type = type;
        this.connections = connections != null ? connections : EnumSet.noneOf(Direction.class);
        this.spawned = spawned;
        this.discovered = discovered;
        this.triggered = triggered;
    }

    // Getters and Setters
    public BlockPos getPosition () {
        return pos;
    }

    public RoomType getType () {
        return type;
    }

    public boolean isSpawned () {
        return spawned;
    }

    public void setSpawned (boolean spawned) {
        this.spawned = spawned;
    }

    public boolean isDiscovered () {
        return discovered;
    }

    public void setDiscovered (boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isTriggered () {
        return triggered;
    }

    public void setTriggered (boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isDeadEnd () {
        return isDeadEnd;
    }

    public void setDeadEnd (boolean isDeadEnd) {
        this.isDeadEnd = isDeadEnd;
    }

    public List<Entity> getEntities () {
        return entities;
    }

    public void addEntity (Entity entity) {
        this.entities.add(entity);
    }

    // NBT Handling
    public CompoundTag writeToNBT(@NotNull CompoundTag tag) {
        tag.put(TAG_POS, NbtUtils.writeBlockPos(pos));
        tag.putString(TAG_TYPE, type.name());
        int[] connectionsArray = connections.stream().mapToInt(Direction::get3DDataValue).toArray();
        tag.putIntArray(TAG_CONNECTIONS, connectionsArray);
        tag.putBoolean(TAG_SPAWNED, spawned);
        tag.putBoolean(TAG_DISCOVERED, discovered);
        tag.putBoolean(TAG_TRIGGERED, triggered);
        tag.putBoolean(TAG_DEAD_END, isDeadEnd);

        ListTag entityListTag = new ListTag();
        for (Entity entity : entities) {
            CompoundTag entityTag = new CompoundTag();
            entity.saveWithoutId(entityTag);
            entityListTag.add(entityTag);
        }
        tag.put(TAG_ENTITIES, entityListTag);

        return tag;
    }

    public static RoomData readFromNBT(@NotNull CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(TAG_POS));
        RoomType type = RoomType.valueOf(tag.getString(TAG_TYPE));

        int[] connectionsArray = tag.contains(TAG_CONNECTIONS) ? tag.getIntArray(TAG_CONNECTIONS) : new int[0];
        EnumSet<Direction> connections = connectionsArray.length > 0
                ? EnumSet.copyOf(Arrays.stream(connectionsArray).mapToObj(Direction::from3DDataValue).toList())
                : EnumSet.noneOf(Direction.class);

        boolean spawned = tag.getBoolean(TAG_SPAWNED);
        boolean discovered = tag.getBoolean(TAG_DISCOVERED);
        boolean triggered = tag.getBoolean(TAG_TRIGGERED);
        boolean isDeadEnd = tag.getBoolean(TAG_DEAD_END);

        RoomData roomData = new RoomData(pos, type, connections, spawned, discovered, triggered);

        // Khôi phục thực thể nếu cần
        ListTag entityListTag = tag.getList(TAG_ENTITIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < entityListTag.size(); i++) {
            CompoundTag entityTag = entityListTag.getCompound(i);
            // Khôi phục thực thể từ NBT
        }

        return roomData;
    }

    // Utility Methods
    public boolean isBlockInsideRoom (BlockPos blockPos) {
        int minX = pos.getX();
        int maxX = pos.getX() + DrunkardWalk.ROOM_SIZE - 1;
        int minZ = pos.getZ();
        int maxZ = pos.getZ() + DrunkardWalk.ROOM_SIZE - 1;
        return blockPos.getX() >= minX && blockPos.getX() <= maxX && blockPos.getZ() >= minZ && blockPos.getZ() <= maxZ;
    }

    public boolean isInsideRoom (BlockPos pos) {
        int roomStartX = this.pos.getX();
        int roomStartZ = this.pos.getZ();
        int roomEndX = roomStartX + DrunkardWalk.ROOM_SIZE - 1;
        int roomEndZ = roomStartZ + DrunkardWalk.ROOM_SIZE - 1;

        return pos.getX() >= roomStartX && pos.getX() <= roomEndX &&
                pos.getZ() >= roomStartZ && pos.getZ() <= roomEndZ;
    }

    public ChunkPos getChunkPosition () {
        int chunkX = this.pos.getX() >> 4;
        int chunkZ = this.pos.getZ() >> 4;
        return new ChunkPos(chunkX, chunkZ);
    }

    public BlockPos getRelativePositionInChunk () {
        int relativeX = this.pos.getX() % 16;
        int relativeZ = this.pos.getZ() % 16;

        return new BlockPos(relativeX, this.pos.getY(), relativeZ);
    }

    // Method to check if the room has monsters
    public boolean hasMonsters () {
        return !entities.isEmpty();
    }
}
