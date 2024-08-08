package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DrunkardWalk implements DungeonGen {
    private static final ResourceLocation ROOM_STRUCTURE = new ResourceLocation(SsMod.MODID, "room1x1");
    private static final ResourceLocation PASSAGE_STRUCTURE = new ResourceLocation(SsMod.MODID, "passage");
    private static final int MAX_ROOMS = 32;
    private static final int MIN_ROOMS = 16;
    public static final int ROOM_SIZE = 16;
    private static final List<RoomType> SPECIAL_ROOM_TYPES = List.of(RoomType.BOSS, RoomType.SHOP, RoomType.TREASURE);
    private static final StructurePlaceSettings placementSettings = new StructurePlaceSettings().setIgnoreEntities(true);
    private RoomType roomType;
    private final Map<BlockPos, EnumSet<Direction>> roomConnections = new HashMap<>();
    public final List<RoomData> rooms = new ArrayList<>();

    private int progress = 0; // Biến tiến trình để theo dõi trạng thái

    public DrunkardWalk (RoomType roomType) {
        this.roomType = roomType;
    }

    public void generate (@NotNull ServerLevel world, @NotNull BlockPos startPos, long seed) {
        RandomSource random = RandomSource.create(seed);
        int totalRooms = MIN_ROOMS + random.nextInt(MAX_ROOMS - MIN_ROOMS + 1); // Chọn số lượng phòng từ MIN_ROOMS đến MAX_ROOMS
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(world);
        Set<BlockPos> roomPositions = new HashSet<>();
        dungeonSavedData.setGenerating(true);

        // Đặt phòng đầu tiên tại vị trí bắt đầu
        placeRoom(world, startPos, roomType, random);
        rooms.add(new RoomData(startPos, roomType, EnumSet.noneOf(Direction.class), false));
        roomPositions.add(startPos);

        int currentRooms = 1; // Bắt đầu từ 1 vì phòng đầu tiên đã được tạo
        BlockPos currentPos = startPos;

        try {
            while (currentRooms < totalRooms) {
                progress = (int) ((currentRooms / (float) totalRooms) * 100); // Cập nhật tiến trình
                System.out.println("Progress: " + progress + "%");

                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos newPos = currentPos.relative(direction, ROOM_SIZE);

                if (isValidRoomPosition(newPos, roomPositions)) {
                    roomType = getRandomRoomType(random);
                    placeRoom(world, newPos, roomType, random);
                    rooms.add(new RoomData(newPos, roomType, EnumSet.noneOf(Direction.class), false));

                    // Tạo lối đi sau khi phòng mới được đặt
                    createPassage(world, currentPos, direction, random);

                    updateRoomConnections(currentPos, newPos, direction);
                    roomPositions.add(newPos);
                    currentPos = newPos;
                    currentRooms++;
                }

                // Thêm độ trễ
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Khôi phục trạng thái ngắt quãng của thread
                    System.out.println("Thread was interrupted, failed to complete operation");
                }
            }
        } finally {
            dungeonSavedData.setGenerating(false);
            dungeonSavedData.setDungeonSeed(seed);
            dungeonSavedData.setRooms(rooms);
            dungeonSavedData.setDirty();
        }
    }


    public int getProgress () {
        return progress;
    }

    private void createPassage (ServerLevel world, BlockPos oldPos, Direction direction, RandomSource random) {
        // Tính toán vị trí lối đi dựa trên vị trí phòng cũ và mới
        BlockPos passagePos = calculatePassagePosition(oldPos, direction);
        placePassage(world, passagePos, direction, random);
    }

    private BlockPos calculatePassagePosition (BlockPos oldPos, Direction direction) {
        // Tính toán vị trí lối đi dựa trên hướng di chuyển và vị trí phòng
        return switch (direction) {
            case NORTH -> new BlockPos(oldPos.getX() + 6, oldPos.getY(), oldPos.getZ() - 2); // Không xoay
            case SOUTH -> new BlockPos(oldPos.getX() + 6, oldPos.getY(), oldPos.getZ() + 14); // Không xoay
            case EAST -> new BlockPos(oldPos.getX() + 17, oldPos.getY(), oldPos.getZ() + 6); // Xoay 90 độ
            case WEST -> new BlockPos(oldPos.getX() + 1, oldPos.getY(), oldPos.getZ() + 6); // Xoay 90 độ
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    private void placePassage (ServerLevel world, BlockPos pos, Direction direction, RandomSource random) {
        // Xác định hướng xoay dựa trên hướng di chuyển
        Rotation rotation = switch (direction) {
            case EAST, WEST -> Rotation.CLOCKWISE_90; // Xoay 90 độ cho hướng Đông và Tây
            default -> Rotation.NONE; // Không xoay cho hướng Bắc và Nam
        };

        // Đặt cấu trúc lối đi tại vị trí đã tính toán với hướng xoay
        world.getStructureManager().get(PASSAGE_STRUCTURE).ifPresent(template ->
                template.placeInWorld(world, pos, pos, new StructurePlaceSettings().setRotation(rotation), random, 2)
        );
    }


    private void placeRoom (ServerLevel world, BlockPos pos, RoomType roomType, RandomSource random) {
        world.getStructureManager().get(ROOM_STRUCTURE).ifPresentOrElse(template -> {
            template.placeInWorld(world, pos, pos, placementSettings, random, 2);
            placeDecorations(world, pos, roomType, random);
        }, () -> SsMod.LOGGER.warn("Không tìm thấy structure template cho phòng: {}", ROOM_STRUCTURE));
    }

    private void placeDecorations (ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        int numDecorations = random.nextInt(6); // 0 to 5
        for (int i = 0; i < numDecorations; i++) {
            BlockPos decorationPos = getRandomDecorationPos(world, roomPos, random);
            if (decorationPos != null) {
                Block decorationBlock = chooseDecorationBlock(random, roomType);
                world.setBlockAndUpdate(decorationPos, decorationBlock.defaultBlockState());
            }
        }
    }

    private Block chooseDecorationBlock (RandomSource random, RoomType roomType) {
        return switch (roomType) {
            case TREASURE -> random.nextInt(10) > 5 ? Blocks.CHEST : Blocks.GOLD_BLOCK;
            case BOSS -> Blocks.DRAGON_EGG;
            default -> getRandomDecorationBlock(random); // Fallback to a random decoration block
        };
    }

    private BlockPos getRandomDecorationPos (ServerLevel world, BlockPos roomPos, RandomSource random) {
        int x = roomPos.getX() + 1 + random.nextInt(ROOM_SIZE - 2); // Loại trừ viền ngoài của phòng
        int z = roomPos.getZ() + 1 + random.nextInt(ROOM_SIZE - 2);
        BlockPos pos = new BlockPos(x, roomPos.getY() + 1, z); // Giả sử đặt trên sàn
        return world.getBlockState(pos).isAir() ? pos : null;
    }

    private Block getRandomDecorationBlock (RandomSource random) {
        List<Block> decorationBlocks = List.of(Blocks.TORCH, Blocks.REDSTONE_TORCH, Blocks.POTTED_RED_MUSHROOM);
        return decorationBlocks.get(random.nextInt(decorationBlocks.size()));
    }

    private static boolean isValidRoomPosition (BlockPos pos, Set<BlockPos> roomPositions) {
        return !roomPositions.contains(pos);
    }

    private RoomType getRandomRoomType (RandomSource random) {
        // Calculate the total weight of spawn probabilities for eligible room types
        double totalWeight = SPECIAL_ROOM_TYPES.stream()
                .filter(type -> type.getConfig().getCurrentCount() < type.getConfig().getMaxCount())
                .mapToDouble(type -> type.getConfig().getSpawnProbability())
                .sum();

        // Generate a random value to select a room type based on their weights
        double randomValue = random.nextDouble() * totalWeight;

        // Iterate over the SPECIAL_ROOM_TYPES to find which type corresponds to the random value
        for (RoomType type : SPECIAL_ROOM_TYPES) {
            if (type.getConfig().getCurrentCount() < type.getConfig().getMaxCount()) {
                if (randomValue < type.getConfig().getSpawnProbability()) {
                    type.getConfig().incrementCount();
                    return type;
                }
                randomValue -= type.getConfig().getSpawnProbability();
            }
        }

        // Default to NORMAL room type if no special type was selected
        return RoomType.NORMAL;
    }

    private void updateRoomConnections (BlockPos currentPos, BlockPos newPos, Direction direction) {
        roomConnections.computeIfAbsent(currentPos, k -> EnumSet.noneOf(Direction.class)).add(direction);
        roomConnections.computeIfAbsent(newPos, k -> EnumSet.noneOf(Direction.class)).add(direction.getOpposite());
    }

    public void clearDungeon (ServerLevel level) {
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(level);

        // Lấy danh sách các phòng đã lưu
        List<RoomData> roomsToClear = dungeonSavedData.getRooms();

        // Duyệt qua từng phòng và xóa các block trong vùng 16x16
        for (RoomData room : roomsToClear) {
            BlockPos roomPos = room.getPosition();
            clearRoom(level, roomPos);
        }

        // Xóa dữ liệu phòng sau khi đã xóa toàn bộ dungeon
        dungeonSavedData.clearRooms();
        dungeonSavedData.setDirty();
    }

    // Phương thức trợ giúp để xóa các block trong một phòng
    private void clearRoom (ServerLevel level, BlockPos roomPos) {
        for (int x = 0; x < ROOM_SIZE; x++) {
            for (int y = 0; y < ROOM_SIZE; y++) {
                for (int z = 0; z < ROOM_SIZE; z++) {
                    BlockPos pos = roomPos.offset(x, y, z);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}