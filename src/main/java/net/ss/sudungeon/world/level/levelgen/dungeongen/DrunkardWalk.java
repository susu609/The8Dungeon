package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.overlay.BlackScreenOverlay;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.util.GetTextures;
import net.ss.sudungeon.util.Log;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DrunkardWalk implements DungeonGen {

    // Constants
    private static final ResourceLocation ROOM_STRUCTURE = new ResourceLocation(SsMod.MODID, "room1x1");
    private static final ResourceLocation PASSAGE_STRUCTURE = new ResourceLocation(SsMod.MODID, "passage");
    private static final int MAX_ROOMS = 32;
    private static final int MIN_ROOMS = 16;
    public static final int ROOM_SIZE = 16;
    private static final int MAX_STEPS_PER_CALL = 5;
    private int generationStep = 0;

    private final Set<BlockPos> roomPositions = new HashSet<>();
    private static final StructurePlaceSettings placementSettings = new StructurePlaceSettings().setIgnoreEntities(true);
    public static final Map<ResourceKey<Level>, Map<BlockPos, RoomData>> dungeonRoomsByDimension = new HashMap<>();

    // Variables
    private final Map<BlockPos, EnumSet<Direction>> roomConnections = new HashMap<>();
    public final List<RoomData> rooms = new ArrayList<>();
    private RoomType roomType;

    public boolean treasureRoomPlaced = false;
    public boolean bossRoomPlaced = false;
    private int progress = 0;

    // Constructor
    public DrunkardWalk (RoomType roomType) {
        this.roomType = roomType;
    }

    // Core generation method
    public void generate (@NotNull ServerLevel level, @NotNull BlockPos startPos, long seed) {
        RandomSource random = RandomSource.create(seed);
        int totalRooms = MIN_ROOMS + random.nextInt(MAX_ROOMS - MIN_ROOMS + 1);

        DungeonSavedData dungeonSavedData = DungeonSavedData.get(level);
        dungeonSavedData.setGenerating(true);

        BlockPos bossRoomPosition = null;
        int steps = 0;

        try {
            for (Entity player : level.players()) {
                player.setNoGravity(true);
            }

            // Place starting room
            placeRoom(level, startPos, roomType, random);
            rooms.add(new RoomData(startPos, roomType, EnumSet.noneOf(Direction.class), false));
            roomPositions.add(startPos);

            int currentRooms = 1;
            BlockPos currentPos = startPos;

            int delayTicks = 0; // tạo biến cooldown
            while (currentRooms < totalRooms && steps < MAX_STEPS_PER_CALL) {
                if (delayTicks > 0) {
                    delayTicks--;
                    continue;
                }

                boolean validPositionFound = false;

                for (int i = 0; i < 10; i++) {
                    Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    BlockPos newPos = currentPos.relative(direction, ROOM_SIZE);

                    if (roomPositions.contains(newPos)) {
                        createPassage(level, currentPos, direction, random);
                        updateRoomConnections(currentPos, newPos, direction);
                        currentPos = newPos;
                        validPositionFound = true;
                        break;
                    }

                    if (isValidRoomPosition(newPos, roomPositions)) {
                        validPositionFound = true;

                        if (currentRooms == totalRooms - 1) {
                            roomType = RoomType.BOSS;
                            bossRoomPosition = newPos;
                        } else {
                            roomType = getRandomRoomType(random, false);
                        }

                        placeRoom(level, newPos, roomType, random);
                        createPassage(level, currentPos, direction, random);
                        updateRoomConnections(currentPos, newPos, direction);

                        roomPositions.add(newPos);
                        currentPos = newPos;
                        currentRooms++;

                        progress = (int) (((float) currentRooms / totalRooms) * 100);
                        Log.i("Dungeon generation progress: {}%", String.valueOf(progress));

                        delayTicks = 2; // cooldown 2 tick giữa các bước
                        break;
                    }
                }

                if (!validPositionFound) {
                    Log.w("Could not find valid room position after 10 attempts, aborting room placement.");
                    break;
                }
                steps++;
            }


            // Nếu phòng cuối cùng không phải BOSS, chuyển đổi phòng cuối cùng thành BOSS
            if (bossRoomPosition == null && !rooms.isEmpty()) {
                RoomData lastRoom = rooms.remove(rooms.size() - 1);
                bossRoomPosition = lastRoom.getPosition();
                placeRoom(level, bossRoomPosition, RoomType.BOSS, random);
                rooms.add(new RoomData(bossRoomPosition, RoomType.BOSS, EnumSet.noneOf(Direction.class), false));
            }

            markDeadEnds();
            logDungeonSummary(); // 👈 Ghi log thống kê phòng
            // Lưu map phòng vào biến toàn cục
            Map<BlockPos, RoomData> dungeonMap = new HashMap<>();
            for (RoomData room : rooms) {
                dungeonMap.put(room.getPosition(), room);
            }
            dungeonRoomsByDimension.put(level.dimension(), dungeonMap);

            // Sau khi hoàn thành tất cả các phòng
            if (progress == 100) {
                onDungeonGenerationComplete(level, startPos);  // startPos là vị trí của phòng bắt đầu
            }

        } catch (Exception e) {
            Log.e("Error during dungeon generation: ", e);
        } finally {
            dungeonSavedData.setGenerating(false);
            dungeonSavedData.setDungeonSeed(seed);
            dungeonSavedData.setRooms(rooms);
            dungeonSavedData.setDirty();

        }
    }

    private void onDungeonGenerationComplete (ServerLevel level, BlockPos startRoomPos) {
        Log.i("Dungeon generation is complete!");

        for (Entity player : level.players()) {
            player.setNoGravity(false);
            player.teleportTo(startRoomPos.getX() + 8.5, startRoomPos.getY() + 5, startRoomPos.getZ() + 8.5);

        }
        BlackScreenOverlay.showOverlay(
                GetTextures.screen("overlay_black.png"), // Overlay texture
                40, // Fade-in duration (ticks)
                20, // Hold duration (ticks)
                40 // Fade-out duration (ticks)
        );
    }

    // Clear dungeon
    public void clearDungeon (ServerLevel level) {
        BlockPos startPos = new BlockPos(0, 0, 0);
        BlockPos endPos = new BlockPos(16, 8, 16);

        for (int x = startPos.getX(); x <= endPos.getX(); x++) {
            for (int y = startPos.getY(); y <= endPos.getY(); y++) {
                for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        Log.i("Cleared the area from (0, 0, 0) to (16, 8, 16).");
    }

    // Create passage and place room methods
    private void createPassage (ServerLevel world, BlockPos oldPos, Direction direction, RandomSource random) {
        BlockPos passagePos = calculatePassagePosition(oldPos, direction);
        placePassage(world, passagePos, direction, random);
    }

    private BlockPos calculatePassagePosition (BlockPos oldPos, Direction direction) {
        return switch (direction) {
            case NORTH -> new BlockPos(oldPos.getX() + 6, oldPos.getY(), oldPos.getZ() - 2);
            case SOUTH -> new BlockPos(oldPos.getX() + 6, oldPos.getY(), oldPos.getZ() + 14);
            case EAST -> new BlockPos(oldPos.getX() + 17, oldPos.getY(), oldPos.getZ() + 6);
            case WEST -> new BlockPos(oldPos.getX() + 1, oldPos.getY(), oldPos.getZ() + 6);
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    private void placePassage (ServerLevel world, BlockPos pos, Direction direction, RandomSource random) {
        Rotation rotation = (direction == Direction.EAST || direction == Direction.WEST) ? Rotation.CLOCKWISE_90 : Rotation.NONE;

        world.getStructureManager().get(PASSAGE_STRUCTURE).ifPresent(template ->
                template.placeInWorld(world, pos, pos, new StructurePlaceSettings().setRotation(rotation), random, 2)
        );
    }

    private void placeRoom (ServerLevel world, BlockPos pos, RoomType roomType, RandomSource random) {
        if (isValidRoomPosition(pos, roomPositions)) {
            world.getStructureManager().get(ROOM_STRUCTURE).ifPresent(template -> {
                template.placeInWorld(world, pos, pos, placementSettings, random, 2);
                placeDecorations(world, pos, roomType, random);
                DungeonSavedData dungeonSavedData = DungeonSavedData.get(world);
                dungeonSavedData.addRoomToMap(pos, roomType);
                dungeonSavedData.getRooms().add(new RoomData(pos, roomType, EnumSet.noneOf(Direction.class), false));
                SsMod.LOGGER.debug("Added room at position: " + pos + " with type: " + roomType);
            });
        } else {
            Log.w("Could not place room at position: " + pos + ". Invalid position.");
        }
    }

    private void placeDecorations (ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        DungeonFeatureSpawner featureSpawner = new DungeonFeatureSpawner();
        featureSpawner.spawnDecorations(world, roomPos, roomType, random);
    }

    // Room Connection and Type
    private boolean isValidRoomPosition (BlockPos pos, Set<BlockPos> roomPositions) {
        // Kiểm tra xem vị trí này đã được sử dụng bởi một phòng khác hay chưa
        for (BlockPos existingPos : roomPositions) {
            if (Math.abs(pos.getX() - existingPos.getX()) < ROOM_SIZE &&
                    Math.abs(pos.getZ() - existingPos.getZ()) < ROOM_SIZE) {
                Log.w("Invalid room position due to overlap: " + pos + " conflicting with " + existingPos);
                return false;
            }
        }

        // Thêm các kiểm tra khác nếu cần thiết, ví dụ: kiểm tra giới hạn của thế giới

        return true;
    }

    private RoomType getRandomRoomType (RandomSource random, boolean isLastRoom) {
        if (isLastRoom) {
            // Biến bossRoomPlaced có thể được sử dụng ở cấp lớp nếu cần kiểm tra
            bossRoomPlaced = true;
            return RoomType.BOSS;
        }

        if (!treasureRoomPlaced && random.nextInt(10) == 0) {
            treasureRoomPlaced = true;
            return RoomType.TREASURE;
        }

        return RoomType.NORMAL;
    }

    private void updateRoomConnections (BlockPos currentPos, BlockPos newPos, Direction direction) {
        EnumSet<Direction> currentConnections = roomConnections.computeIfAbsent(currentPos, k -> EnumSet.noneOf(Direction.class));
        EnumSet<Direction> newConnections = roomConnections.computeIfAbsent(newPos, k -> EnumSet.noneOf(Direction.class));

        currentConnections.add(direction);
        newConnections.add(direction.getOpposite());

        // Kiểm tra và ghi nhật ký lỗi nếu kết nối không đối xứng
        if (!currentConnections.contains(direction) || !newConnections.contains(direction.getOpposite())) {
            SsMod.LOGGER.error("Room connection error: {} to {} via {}", currentPos, newPos, direction);
        }
    }

    private void markDeadEnds () {
        for (RoomData room : rooms) {
            BlockPos roomPos = room.getPosition();

            if (isDeadEnd(roomPos)) {
                room.setDeadEnd(true);
                Log.i("Marked room at {} as a dead end.", String.valueOf(roomPos));

                // Tùy chỉnh logic cho các phòng đặc biệt:
                if (room.getType() == RoomType.BOSS || room.getType() == RoomType.TREASURE) {
                    Log.i("Special handling for dead-end room: {}", String.valueOf(room.getType()));
                    // Ví dụ: Thêm bẫy, kẻ địch, hoặc thay đổi cấu trúc phòng
                }
            } else {
                room.setDeadEnd(false);
            }
        }
    }

    private boolean isDeadEnd (BlockPos roomPos) {
        EnumSet<Direction> connections = roomConnections.get(roomPos);
        if (connections == null || connections.size() != 1) {
            return false; // Không phải đường cùng nếu có hơn 1 kết nối hoặc không có kết nối nào.
        }

        // Kiểm tra phòng liền kề
        Direction singleConnection = connections.iterator().next(); // Lấy hướng kết nối duy nhất
        BlockPos connectedRoomPos = roomPos.relative(singleConnection);

        // Nếu phòng kết nối không hợp lệ (không có trong roomPositions), thì đây là đường cùng.
        return !roomPositions.contains(connectedRoomPos);
    }


    public int getProgress () {
        return progress;
    }

    // Getters for rooms
    public List<RoomData> getRooms () {
        return rooms;
    }

    private void logDungeonSummary () {
        int totalRooms = rooms.size();
        long deadEnds = rooms.stream().filter(RoomData::isDeadEnd).count();
        long normalRooms = rooms.stream().filter(r -> r.getType() == RoomType.NORMAL).count();
        long specialRooms = rooms.stream().filter(r -> r.getType() != RoomType.NORMAL).count();
        int totalConnections = roomConnections.values().stream().mapToInt(EnumSet::size).sum() / 2;

        Log.i("=== TỔNG KẾT DUNGEON ===");
        Log.i("Tổng số phòng: {}", String.valueOf(totalRooms));
        Log.i("Phòng thường: {}", String.valueOf(normalRooms));
        Log.i("Phòng đặc biệt: {}", String.valueOf(specialRooms));
        Log.i("Phòng cụt (dead-end): {}", String.valueOf(deadEnds));
        Log.i("Tổng số kết nối giữa phòng: {}", String.valueOf(totalConnections));
        Log.i("==========================");
    }

}
