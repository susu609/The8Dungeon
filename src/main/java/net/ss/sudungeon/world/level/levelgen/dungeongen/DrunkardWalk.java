package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.BlackScreenOverlay;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DrunkardWalk implements DungeonGen {

    // Constants
    private static final ResourceLocation ROOM_STRUCTURE = new ResourceLocation(SsMod.MOD_ID, "room1x1");
    private static final ResourceLocation PASSAGE_STRUCTURE = new ResourceLocation(SsMod.MOD_ID, "passage");
    private static final int MAX_ROOMS = 32;
    private static final int MIN_ROOMS = 16;
    public static final int ROOM_SIZE = 16;

    private final Set<BlockPos> roomPositions = new HashSet<>();
    private static final StructurePlaceSettings placementSettings = new StructurePlaceSettings().setIgnoreEntities(true);

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

            while (currentRooms < totalRooms) {
                boolean validPositionFound = false;
                for (int i = 0; i < 10; i++) {  // Thử tối đa 10 lần để tìm vị trí hợp lệ
                    Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    BlockPos newPos = currentPos.relative(direction, ROOM_SIZE);

                    // Kiểm tra nếu vị trí đã có phòng
                    if (roomPositions.contains(newPos)) {
                        // Nếu đã có phòng ở vị trí này, chỉ tạo lối đi và không thêm phòng mới
                        SsMod.LOGGER.info("Room already exists at " + newPos + ". Creating passage only.");
                        createPassage(level, currentPos, direction, random);
                        updateRoomConnections(currentPos, newPos, direction);
                        currentPos = newPos;  // Di chuyển đến phòng hiện có
                        validPositionFound = true;
                        break;
                    }

                    // Nếu vị trí chưa có phòng, tạo phòng mới
                    if (isValidRoomPosition(newPos, roomPositions)) {
                        validPositionFound = true;

                        // Đảm bảo rằng phòng cuối cùng là phòng BOSS
                        if (currentRooms == totalRooms - 1) {
                            roomType = RoomType.BOSS;
                            bossRoomPosition = newPos;
                        } else {
                            roomType = getRandomRoomType(random, false);
                        }

                        placeRoom(level, newPos, roomType, random);
                        rooms.add(new RoomData(newPos, roomType, EnumSet.noneOf(Direction.class), false));
                        createPassage(level, currentPos, direction, random);
                        updateRoomConnections(currentPos, newPos, direction);

                        roomPositions.add(newPos);
                        currentPos = newPos;
                        currentRooms++;

                        progress = (int) (((float) currentRooms / totalRooms) * 100);
                        SsMod.LOGGER.info("Dungeon generation progress: {}%", progress);
                        break;
                    }
                }

                if (!validPositionFound) {
                    SsMod.LOGGER.warn("Could not find a valid room position after 10 attempts, aborting room placement.");
                    break;  // Nếu sau 10 lần thử không tìm được vị trí hợp lệ, thoát vòng lặp
                }
            }


            // Nếu phòng cuối cùng không phải BOSS, chuyển đổi phòng cuối cùng thành BOSS
            if (bossRoomPosition == null && !rooms.isEmpty()) {
                RoomData lastRoom = rooms.remove(rooms.size() - 1);
                bossRoomPosition = lastRoom.getPosition();
                placeRoom(level, bossRoomPosition, RoomType.BOSS, random);
                rooms.add(new RoomData(bossRoomPosition, RoomType.BOSS, EnumSet.noneOf(Direction.class), false));
            }

            markDeadEnds();

// Sau khi hoàn thành tất cả các phòng
            if (progress == 100) {
                onDungeonGenerationComplete(level, startPos);  // startPos là vị trí của phòng bắt đầu
            }

        } catch (Exception e) {
            SsMod.LOGGER.error("Error during dungeon generation: ", e);
        } finally {
            dungeonSavedData.setGenerating(false);
            dungeonSavedData.setDungeonSeed(seed);
            dungeonSavedData.setRooms(rooms);
            dungeonSavedData.setDirty();
        }
    }

    private void onDungeonGenerationComplete(ServerLevel level, BlockPos startRoomPos) {
        SsMod.LOGGER.info("Dungeon generation is complete!");

        for (Entity player : level.players()) {
            player.setNoGravity(false);
            player.teleportTo(startRoomPos.getX() + 8.5, startRoomPos.getY() + 5, startRoomPos.getZ() + 8.5);

        }

        BlackScreenOverlay.showOverlay();
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
        System.out.println("Cleared the area from (0, 0, 0) to (16, 8, 16).");
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
            SsMod.LOGGER.warn("Could not place room at position: " + pos + ". Invalid position.");
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
                SsMod.LOGGER.warn("Invalid room position due to overlap: " + pos + " conflicting with " + existingPos);
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

        if (!currentConnections.contains(direction) || !newConnections.contains(direction.getOpposite())) {
            System.err.println("Room connection error: " + currentPos + " to " + newPos);
        }
    }

    private void markDeadEnds () {
        for (RoomData room : rooms) {
            if (isDeadEnd(room.getPosition())) {
                room.setDeadEnd(true);
                if (room.getType() == RoomType.BOSS || room.getType() == RoomType.TREASURE || room.getType() == RoomType.SHOP) {
                    // Logic đặc biệt cho phòng đặc biệt:
                    // Ví dụ: thay đổi cấu trúc hoặc thêm đặc tính gì đó
                }
            }
        }
    }

    private boolean isDeadEnd (BlockPos roomPos) {
        EnumSet<Direction> connections = roomConnections.get(roomPos);
        return connections != null && connections.size() == 1;
    }

    public int getProgress () {
        return progress;
    }

    // Getters for rooms
    public List<RoomData> getRooms () {
        return rooms;
    }
}
