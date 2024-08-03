package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
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
    public static final ResourceLocation PASSAGE_STRUCTURE = new ResourceLocation(SsMod.MODID, "passage");
    private static final int MAX_ROOMS = 32;
    private static final int MIN_ROOMS = 16;
    public static final int ROOM_SIZE = 16;
    private static final byte PASSAGE_WIDTH = 4;
    private final RoomType startRoomType;
    private RoomType roomType;
    private static final List<RoomType> SPECIAL_ROOM_TYPES = List.of(RoomType.BOSS, RoomType.SHOP, RoomType.TREASURE);
    public List<RoomData> rooms = new ArrayList<>();

    public DrunkardWalk (RoomType roomType) {
        this.startRoomType = roomType;
        this.roomType = roomType;
    }

    @Override
    public void generate (@NotNull ServerLevel world, @NotNull BlockPos startPos, long seed) {
        // 1. Khởi tạo
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(world);
        BlockPos currentPos = startPos;
        Set<BlockPos> roomPositions = new HashSet<>();
        roomPositions.add(currentPos);
        Map<BlockPos, Integer> roomConnections = new HashMap<>();
        RandomSource random = RandomSource.create(seed);
        roomType = RoomType.NORMAL;
        int currentRooms = 0;
        int attempts = 0;
        int maxAttempts = MAX_ROOMS + MIN_ROOMS;

        // 2. Drunkard Walk (Tạo phòng và kết nối)
        dungeonSavedData.setGenerating(true);
        try {
            BlockPos newPos;
            while (currentRooms < MAX_ROOMS && attempts < maxAttempts) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                newPos = currentPos.relative(direction, ROOM_SIZE);
                EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);

                // Kiểm tra nếu là phòng đầu tiên và đặt tại (0, 1, 0)
                if (currentRooms == 0) {
                    newPos = startPos.above();
                    placeRoom(world, newPos);
                    connections.add(direction);
                    rooms.add(new RoomData(newPos, startRoomType, connections, false));
                    roomPositions.add(newPos);
                    currentRooms++;
                } else if (isValidRoomPosition(newPos, roomPositions)) { // Nếu vị trí mới hợp lệ
                    roomPositions.add(newPos);
                    currentRooms++;
                    roomType = getRandomRoomType(random, currentRooms);
                    rooms.add(new RoomData(newPos, roomType, connections, false));
                    placeRoom(world, newPos); // Đặt phòng

                    // Tạo lối đi từ phòng thứ hai trở đi
                    if (currentRooms > 1) {
                        createPassage(world, currentPos, direction); // Đặt lối đi từ phòng CŨ
                    }

                    // Cập nhật connections
                    roomConnections.put(currentPos, roomConnections.getOrDefault(currentPos, 0) + 1);
                    roomConnections.put(newPos, roomConnections.getOrDefault(newPos, 0) + 1);
                    connections.add(direction.getOpposite());

                    // Đặt các khối và thực thể đặc biệt trong phòng (nếu có)
                    placeSpecialBlocksAndEntities(world, newPos, roomType, random);
                } else if (roomPositions.contains(newPos)) { // Nếu vị trí đã có phòng
                    // ... (Cập nhật lại connections khi không đặt phòng mới)
                    roomConnections.put(currentPos, roomConnections.getOrDefault(currentPos, 0) + 1);
                    roomConnections.put(newPos, roomConnections.getOrDefault(newPos, 0) + 1);
                    connections.add(direction.getOpposite());
                }

                currentPos = newPos;
                attempts++;
            }

            dungeonSavedData.setDungeonSeed(seed);
            dungeonSavedData.setRooms(rooms);
            dungeonSavedData.setDungeonGenerator(this);
            dungeonSavedData.setDirty();
            SsMod.LOGGER.info("Dungeon generation completed.\n" +
                    dungeonSavedData.getDungeonGenerator() + "\n" +
                    dungeonSavedData.getDungeonSeed() + "\n" +
                    dungeonSavedData.getRooms() + ".");
        } catch (Exception e) {
            SsMod.LOGGER.error("Lỗi khi tạo Dungeon: ", e);
        } finally {
            dungeonSavedData.setGenerating(false);
        }
    }

    private static boolean isValidRoomPosition (BlockPos pos, Set<BlockPos> roomPositions) { // Loại bỏ tham số world
        return !roomPositions.contains(pos);
    }

    private RoomType getRandomRoomType (RandomSource rand, int currentRooms) {
        if (currentRooms > 1) {
            float totalWeight = 0.0f;
            for (RoomType type : SPECIAL_ROOM_TYPES) {
                if (type != null && type.getConfig().getCurrentCount() < type.getConfig().getMaxCount()) { // Kiểm tra null cho type
                    totalWeight += type.getConfig().getSpawnProbability();
                }
            }

            if (totalWeight > 0) {
                float randomValue = rand.nextFloat() * totalWeight;
                for (RoomType type : SPECIAL_ROOM_TYPES) {
                    if (type.getConfig().getCurrentCount() < type.getConfig().getMaxCount()) {
                        if (randomValue < type.getConfig().getSpawnProbability()) {
                            type.getConfig().incrementCount();
                            return type;
                        }
                        randomValue -= type.getConfig().getSpawnProbability();
                    }
                }
            } else {
                // không còn phòng đặc biệt khả dụng, trả về phòng thường.
                return RoomType.NORMAL;
            }
        }
        return RoomType.NORMAL;
    }

    private void createPassage (ServerLevel world, BlockPos pos, Direction direction) {
        BlockPos passagePos;
        // Tính toán vị trí lối đi dựa trên hướng và vị trí phòng CŨ
        switch (direction) {
            case NORTH -> passagePos = new BlockPos(pos.getX() + 6, 2, pos.getZ() - 2); // Lối đi phía Bắc
            case SOUTH -> passagePos = new BlockPos(pos.getX() + 6, 2, pos.getZ() + 14); // Lối đi phía Nam
            case EAST -> passagePos = new BlockPos(pos.getX() + 17, 2, pos.getZ() + 6); // Lối đi phía Đông
            case WEST -> passagePos = new BlockPos(pos.getX() + 1, 2, pos.getZ() + 6); // Lối đi phía Tây
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        placePassage(world, passagePos, direction); // Đặt structure template của lối đi
    }

    private void placePassage (ServerLevel world, BlockPos pos, Direction direction) {
        world.getStructureManager().get(PASSAGE_STRUCTURE).ifPresent(template -> {
            Rotation rotation = direction.getAxis() == Direction.Axis.X ? Rotation.CLOCKWISE_90 : Rotation.NONE;
            template.placeInWorld(world, pos, pos, new StructurePlaceSettings().setRotation(rotation), world.getRandom(), 2);
        });
    }

    private void placeRoom (ServerLevel world, BlockPos pos) {
        world.getStructureManager().get(ROOM_STRUCTURE).ifPresentOrElse(
                template -> template.placeInWorld(world, pos, pos,
                        new StructurePlaceSettings().setIgnoreEntities(true), world.getRandom(), 2),
                () -> SsMod.LOGGER.warn("Không tìm thấy structure template cho phòng: {}", ROOM_STRUCTURE)
        );
    }

    private void placeDecorations (ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        int maxDecorations = 5; // Số lượng tối đa vật trang trí trong phòng
        int numDecorations = random.nextInt(maxDecorations + 1); // Số lượng vật trang trí thực tế

        for (int i = 0; i < numDecorations; i++) {
            BlockPos decorationPos = getRandomDecorationPos(world, roomPos, random);
            if (decorationPos != null) { // Kiểm tra xem có tìm thấy vị trí đặt hợp lệ không
                Block decorationBlock = switch (roomType) {
                    case NORMAL -> getRandomBlockFromList(random, Blocks.TORCH, Blocks.COBWEB, Blocks.DECORATED_POT);
                    case TREASURE -> getRandomBlockFromList(random, Blocks.CHEST, Blocks.GOLD_BLOCK);
                    // ... các loại phòng khác
                    default -> Blocks.TORCH; // Khối mặc định
                };

                world.setBlockAndUpdate(decorationPos, decorationBlock.defaultBlockState());
            }
        }
    }

    private Block getRandomBlockFromList (RandomSource random, Block... blocks) {
        return blocks[random.nextInt(blocks.length)];
    }

    private BlockPos getRandomDecorationPos (ServerLevel world, BlockPos roomPos, RandomSource random) {
        int x = roomPos.getX() + 1 + random.nextInt(ROOM_SIZE - 2); // Loại trừ viền ngoài của phòng
        int z = roomPos.getZ() + 1 + random.nextInt(ROOM_SIZE - 2);
        BlockPos pos = new BlockPos(x, roomPos.getY() + 1, z); // Giả sử đặt trên sàn (y + 1)

        // Kiểm tra xem vị trí có bị chặn bởi khối light hay không
        if (world.getBlockState(pos).is(Blocks.LIGHT)) {
            return null;
        }
        return pos;
    }

    private Block getRandomDecorationBlock (RandomSource random) {
        // Chọn ngẫu nhiên một khối trang trí (ví dụ: TORCH, FLOWER_POT, ...)
        List<Block> decorationBlocks = List.of(Blocks.TORCH, Blocks.REDSTONE_TORCH, Blocks.POTTED_RED_MUSHROOM);
        return decorationBlocks.get(random.nextInt(decorationBlocks.size()));
    }

    public void regenerateDungeon (ServerLevel world) {
        DungeonSavedData data = DungeonSavedData.get(world);
        long seed = data.getDungeonSeed(); // Lấy seed từ DungeonSavedData
        generate(world, new BlockPos(0, 1, 0), seed); // Tạo lại dungeon từ seed
    }

    public void clearDungeon (ServerLevel world) {
        DungeonSavedData data = DungeonSavedData.get(world);
        List<RoomData> roomDataList = data.getRooms();

        // Sử dụng roomDataList để lấy danh sách các chunk của dungeon

        List<ChunkPos> dungeonChunks = getDungeonChunks(roomDataList);
        // Xóa các entity trong dungeon
        for (ServerPlayer player : world.players()) {
            Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(player.createCommandSourceStack(), "despawn @e"); // Thực thi lệnh
        }
        // Xóa các khối trong dungeon
        for (RoomData roomData : roomDataList) {
            clearArea(world, roomData.pos);
        }
        data.setRoomDataList(new ArrayList<>()); // Xóa và cập nhật lại roomDataList
        data.setDungeonSeed(0L); // Reset seed
        data.setDirty(); // Đánh dấu để lưu thay đổi

    }

    private List<ChunkPos> getDungeonChunks (List<RoomData> roomDataList) {
        Set<ChunkPos> chunkPosSet = new HashSet<>();
        for (RoomData room : roomDataList) {
            ChunkPos chunkPos = new ChunkPos(room.pos);
            chunkPosSet.add(chunkPos);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (room.connections.contains(direction)) { // Kiểm tra xem có kết nối theo hướng này không
                    BlockPos neighborPos = room.pos.relative(direction, ROOM_SIZE);
                    chunkPosSet.add(new ChunkPos(neighborPos));
                }
            }
        }
        return new ArrayList<>(chunkPosSet);
    }

    private void clearArea (ServerLevel world, BlockPos startPos) {
        BlockPos endPos = startPos.offset(15, 7, 15);
        for (int x = startPos.getX(); x <= endPos.getX(); x++) {
            for (int y = startPos.getY(); y <= endPos.getY(); y++) {
                for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void placeSpecialBlocksAndEntities (ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        switch (roomType) {
            case TREASURE:
                // Đặt rương kho báu tại vị trí ngẫu nhiên trong phòng
                BlockPos chestPos = roomPos.offset(random.nextInt(16), 1, random.nextInt(16));
                world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
                // ... (tùy chỉnh nội dung rương)
                break;
            case SHOP:
                // Đặt quầy hàng tại vị trí ngẫu nhiên trong phòng
                BlockPos shopPos = roomPos.offset(random.nextInt(16), 1, random.nextInt(16));
                // ... (đặt các khối tạo nên quầy hàng)
                break;
            case BOSS:
                // Triệu hồi boss tại vị trí ngẫu nhiên trong phòng
                BlockPos bossPos = roomPos.offset(random.nextInt(16), 1, random.nextInt(16));
                // ... (triệu hồi entity boss)
                break;
            // ... (xử lý các loại phòng khác nếu cần)
        }
    }
}