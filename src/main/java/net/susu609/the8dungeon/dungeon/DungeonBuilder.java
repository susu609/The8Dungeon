package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.dungeon.DungeonLayout.Door;
import net.susu609.the8dungeon.dungeon.DungeonLayout.Room;

/**
 * Biến DungeonLayout (dữ liệu) thành block thật trong world.
 *
 * Cắt dọc một phòng (trục Y, tính từ origin.y):
 *   y = 0                  sàn  : STONE
 *   y = 1 .. 16            rỗng : AIR (tường ngoài rìa là STONE_BRICKS)
 *   y = 17                 trần : STONE_BRICKS
 *
 * Mỗi phòng có tường riêng (tường dày 1). Hai phòng kề nhau có 2 lớp tường liền nhau,
 * cửa là lỗ khoét xuyên qua cả hai lớp: rộng 2 block (vị trí 7 và 8 của ô lưới), cao DOOR_HEIGHT.
 */
public final class DungeonBuilder {

    private static final int CELL = RoomPreset.CELL;
    private static final int HEIGHT = RoomPreset.INTERIOR_HEIGHT;
    private static final int DOOR_HEIGHT = 4;

    // 2 = gửi thay đổi cho client, 16 = bỏ qua cập nhật hình dạng block lân cận (nhanh hơn khi đặt hàng loạt).
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private DungeonBuilder() {}

    // Cao độ mặt sàn cố định của mọi dungeon.
    public static final int FLOOR_Y = 0;

    /**
     * Góc cố định của lưới: ô giữa (nơi đặt phòng vào) trùng chunk (0, 0), sàn ở y = FLOOR_Y.
     * Lưới 13x13: ô giữa là 6, nên góc là (-96, 0, -96) và phòng vào phủ block x, z từ 0 đến 15.
     */
    public static BlockPos fixedOrigin(DungeonParams params) {
        return new BlockPos(-(params.gridW() / 2) * CELL, FLOOR_Y, -(params.gridD() / 2) * CELL);
    }

    // Chiều cao cần thiết tính từ origin.y (sàn + rỗng + trần).
    public static int totalHeight() {
        return HEIGHT + 2;
    }

    /**
     * @param origin góc (x, y, z) của ô lưới (0, 0); x và z nên chia hết cho 16 để mỗi phòng khớp đúng 1 chunk.
     *               y là cao độ của mặt sàn.
     */
    public static void build(ServerLevel level, DungeonLayout layout, BlockPos origin) {
        if (origin.getY() < level.getMinBuildHeight()
                || origin.getY() + totalHeight() > level.getMaxBuildHeight()) {
            throw new IllegalArgumentException("Dungeon vượt quá giới hạn chiều cao của world.");
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (Room room : layout.rooms()) {
            int x0 = origin.getX() + room.cx() * CELL;
            int z0 = origin.getZ() + room.cz() * CELL;
            int sizeX = room.cellsX() * CELL;
            int sizeZ = room.cellsZ() * CELL;

            for (int dx = 0; dx < sizeX; dx++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    boolean wall = dx == 0 || dz == 0 || dx == sizeX - 1 || dz == sizeZ - 1;

                    level.setBlock(pos.set(x0 + dx, origin.getY(), z0 + dz), stone, FLAGS);
                    for (int dy = 1; dy <= HEIGHT; dy++) {
                        level.setBlock(pos.set(x0 + dx, origin.getY() + dy, z0 + dz),
                                wall ? bricks : air, FLAGS);
                    }
                    level.setBlock(pos.set(x0 + dx, origin.getY() + HEIGHT + 1, z0 + dz), bricks, FLAGS);
                }
            }
        }

        // Khoét cửa sau khi mọi phòng đã dựng xong, nếu không tường của phòng dựng sau sẽ lấp lại lỗ.
        for (Door door : layout.doors()) {
            carveDoor(level, door, origin, air, pos);
        }
    }

    private static void carveDoor(ServerLevel level, Door door, BlockPos origin,
                                  BlockState air, BlockPos.MutableBlockPos pos) {
        int baseX = origin.getX() + door.cx() * CELL;
        int baseZ = origin.getZ() + door.cz() * CELL;

        // Hai lớp tường nằm ở local 15 của ô này và local 0 của ô bên cạnh (tức 15 và 16 tính từ góc ô này).
        // Hai block rộng của cửa nằm ở local 7 và 8.
        for (int wall = 15; wall <= 16; wall++) {
            for (int side = 7; side <= 8; side++) {
                for (int dy = 1; dy <= DOOR_HEIGHT; dy++) {
                    int x = door.east() ? baseX + wall : baseX + side;
                    int z = door.east() ? baseZ + side : baseZ + wall;
                    level.setBlock(pos.set(x, origin.getY() + dy, z), air, FLAGS);
                }
            }
        }
    }
}
