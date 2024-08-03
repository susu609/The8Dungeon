/*
package net.ss.sudungeon.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.init.SsModBlocks;

import java.util.ArrayList;
import java.util.List;

public class DungeonGenerator {
    private static final int ROOM_SIZE = 16; // Kích thước mỗi phòng (16x16 blocks)
    private static final ResourceLocation ROOM_TEMPLATE = new ResourceLocation(SsMod.MODID, "room1x1"); // Đường dẫn đến structure template
    private static final int MIN_ROOMS = 16; // Số phòng tối thiểu
    private static final int MAX_ROOMS = 32; // Số phòng tối đa

    public static void generateDungeon (ServerLevel world, RandomSource random) {
        BlockPos startPos = new BlockPos(0, 64, 0); // Điểm bắt đầu cố định của hầm ngục
        List<BlockPos> roomPositions = new ArrayList<>();
        roomPositions.add(startPos); // Phòng bắt đầu
        int currentRooms = 1;

        // Tạo các phòng
        while (currentRooms < MAX_ROOMS) {
            BlockPos currentPos = roomPositions.get(random.nextInt(roomPositions.size()));
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos newPos = currentPos.relative(dir, ROOM_SIZE);

            if (isValidRoomPosition(world, newPos, roomPositions)) {
                roomPositions.add(newPos);
                currentRooms++;

                // Đặt khối đánh dấu phòng
                world.setBlockAndUpdate(newPos, SsModBlocks.ROOM_MARKER.get().defaultBlockState());
            }
        }

        // Đặt các phòng từ Structure Templates
        StructureTemplateManager templateManager = world.getStructureManager();
        for (BlockPos roomPos : roomPositions) {
            if (templateManager.get(ROOM_TEMPLATE).isPresent()) {
                StructureTemplate template = templateManager.getOrCreate(ROOM_TEMPLATE);
                template.placeInWorld(world, roomPos, roomPos,
                        new StructurePlaceSettings().setIgnoreEntities(true), random, 0);
                // Xóa khối đánh dấu sau khi đặt phòng
                world.setBlockAndUpdate(roomPos, Blocks.AIR.defaultBlockState());
            } else {
                SsMod.LOGGER.warn("Không tìm thấy structure template: {}", ROOM_TEMPLATE);
            }
        }

        // Kết nối các phòng
        connectRooms(world, roomPositions, random);
    }


    private static boolean isValidRoomPosition (ServerLevel world, BlockPos pos, List<BlockPos> roomPositions) {
        // Kiểm tra xem vị trí có hợp lệ để đặt phòng hay không
        // Ví dụ: không chồng lấn với các phòng khác, không ở dưới nước,...
        for (BlockPos existingPos : roomPositions) {
            if (pos.closerThan(existingPos, ROOM_SIZE)) { // Kiểm tra khoảng cách giữa các phòng
                return false;
            }
        }
        return true;
    }

    private static void connectRooms (ServerLevel world, List<BlockPos> roomPositions, RandomSource random) {
        for (int i = 0; i < roomPositions.size() - 1; i++) {
            BlockPos room1Pos = roomPositions.get(i);
            BlockPos room2Pos = roomPositions.get(i + 1);

            // Tính toán điểm giữa của hai phòng
            BlockPos midPoint = new BlockPos(
                    (room1Pos.getX() + room2Pos.getX()) / 2,
                    room1Pos.getY(), // Giữ nguyên chiều cao
                    (room1Pos.getZ() + room2Pos.getZ()) / 2
            );

            // Tạo hành lang theo trục X
            for (int x = room1Pos.getX() + ROOM_SIZE / 2; x < midPoint.getX(); x++) {
                world.setBlockAndUpdate(new BlockPos(x, midPoint.getY(), midPoint.getZ()), Blocks.AIR.defaultBlockState());
            }

            // Tạo hành lang theo trục Z
            for (int z = room1Pos.getZ() + ROOM_SIZE / 2; z < midPoint.getZ(); z++) {
                world.setBlockAndUpdate(new BlockPos(midPoint.getX(), midPoint.getY(), z), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static List<Direction> getRoomExits (ServerLevel world, BlockPos roomPos) { // Phương thức getRoomExits
        List<Direction> exits = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = roomPos.relative(dir, ROOM_SIZE / 2);
            BlockPos neighborPosUp = neighborPos.above();
            if (world.getBlockState(neighborPos).isAir() && world.getBlockState(neighborPosUp).isAir()) {
                exits.add(dir);
            }
        }
        return exits;
    }

    private static void fillDeadEnd (ServerLevel world, BlockPos roomPos, Direction direction) { // Phương thức fillDeadEnd
        BlockPos fillStart = roomPos.relative(direction, 2); // Bắt đầu lấp từ 2 khối sau cửa ra vào
        BlockState randomStoneBricksState = SsModBlocks.RANDOM_STONE_BRICKS.get().defaultBlockState();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos fillPos = fillStart.offset(dx, dy, dz);
                    world.setBlockAndUpdate(fillPos, randomStoneBricksState);
                }
            }
        }
    }
}
*/
