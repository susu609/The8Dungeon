package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.susu609.the8dungeon.dungeon.DungeonLayout.Door;
import net.susu609.the8dungeon.dungeon.DungeonLayout.Room;
import net.susu609.the8dungeon.init.ModBlocksRegistry;
import net.susu609.the8dungeon.init.ModEntitiesRegistry;
import net.susu609.the8dungeon.world.WorldRandom;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;
import net.susu609.the8dungeon.world.level.block.entity.PropEntity;
import net.susu609.the8dungeon.world.torch.TorchBurn;

/**
 * Biến DungeonLayout (dữ liệu) thành block thật trong world.
 * <p>
 * Cắt dọc một phòng (trục Y, tính từ origin.y):
 *   y = 0                  sàn  : STONE
 *   y = 1 .. 16            rỗng : AIR (tường ngoài rìa là STONE_BRICKS)
 *   y = 17                 trần : STONE_BRICKS
 * <p>
 * Mỗi phòng có tường riêng (tường dày 1). Hai phòng kề nhau có 2 lớp tường liền nhau,
 * cửa là lỗ khoét xuyên qua cả hai lớp: rộng 2 block (vị trí 7 và 8 của ô lưới), cao DOOR_HEIGHT.
 */
public final class DungeonBuilder {

    private static final int CELL = RoomPreset.CELL;
    private static final int HEIGHT = RoomPreset.INTERIOR_HEIGHT;
    private static final int DOOR_HEIGHT = 3;

    private static final int WALL_A = 15, WALL_B = 16;     // hai lớp tường của cửa
    private static final int DOOR_LO = 7, DOOR_HI = 8;     // hai block của lỗ cửa
    private static final int TORCH_SIDE_LO = 6, TORCH_SIDE_HI = 9;   // hai bên mép cửa
    private static final int TORCH_IN_A = 14, TORCH_IN_B = 17;       // sát mặt trong tường A và B

    private static final double LIT_CHANCE = 0.75;
    private static final double WEAK_SHARE = 0.5;   // tạm: trong 25% còn lại, nửa yếu, nửa tắt; bạn chốt số thật

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
     * @param world
     */
    public static void build(ServerLevel level, DungeonLayout layout, BlockPos origin, WorldRandom world) {
        if (origin.getY() < level.getMinBuildHeight()
                || origin.getY() + totalHeight() > level.getMaxBuildHeight()) {
            throw new IllegalArgumentException("Dungeon vượt quá giới hạn chiều cao của world.");
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int span = 13 * CELL;
        AABB area = new AABB(
                origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + span, origin.getY() + totalHeight(), origin.getZ() + span);
        level.getEntitiesOfClass(PropEntity.class, area).forEach(Entity::discard);
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
        for (Door door : layout.doors()) {
            placeDoorTorches(level, world, door, origin);
        }
        for (Door door : layout.doors()) {
            placeDoorGate(level, door, origin);
        }
        // TODO: đuốc TƯỜNG (Blocks.WALL_TORCH) đặt bên TRONG mỗi phòng, ở hai bên cạnh cửa, cho MỌI cửa.
        //   Cửa hướng đông (door.east() == true), baseX/baseZ như trong carveDoor, y = origin.getY() + 2:
        //     phòng phía tây: x = baseX + 14, z = baseZ + 6 và baseZ + 9, FACING = WEST (quay ra xa tường x = 15)
        //     phòng phía đông: x = baseX + 17, cùng hai z, FACING = EAST (tường ở x = 16)
        //   Cửa hướng nam (east == false): đổi vai x và z; phía bắc z = baseZ + 14 FACING = NORTH, phía nam z = baseZ + 17 FACING = SOUTH.
        //   Đặt sau vòng khoét cửa; block tường tại (15|16, 6|9) vẫn còn là STONE_BRICKS nên đuốc có chỗ bám.
        // TODO: mỗi đuốc roll RIÊNG: world.fork("torch").at(x, z).chance(0.75) (x, z là toạ độ block đuốc, nên kết quả không phụ thuộc thứ tự lặp).
        // TODO: 25% còn lại là tắt (wall_torch_off) hoặc YẾU. Đuốc yếu = Blocks.WALL_TORCH nhưng thời gian cháy chỉ còn 25% (TorchBurn.WEAK_TICKS).
        //       Không cần block mới. Cần chốt tỉ lệ chia tắt / yếu trong 25%; nếu chia thì roll thứ hai cho cùng đuốc.
        // TODO: đuốc sáng và yếu đều do mixin ModBlockBehaviour lên lịch tắt khi onPlace; đặt đuốc yếu phải ghi đè thời gian (xem TODO trong TorchBurn).
        // TODO: build() chưa nhận seed/WorldRandom; thêm tham số thì phải sửa cả DungeonCommand (gọi build).
    }

    private static BlockPos doorBase(Door door, BlockPos origin) {
        return new BlockPos(origin.getX() + door.cx() * CELL, origin.getY(), origin.getZ() + door.cz() * CELL);
    }

    private static void placeDoorTorches(ServerLevel level, WorldRandom world, Door door, BlockPos origin) {
        BlockPos base = doorBase(door, origin);
        int y = origin.getY() + 2;
        for (int side : new int[]{TORCH_SIDE_LO, TORCH_SIDE_HI}) {
            if (door.east()) {
                placeTorch(level, world, new BlockPos(base.getX() + TORCH_IN_A, y, base.getZ() + side), Direction.WEST);
                placeTorch(level, world, new BlockPos(base.getX() + TORCH_IN_B, y, base.getZ() + side), Direction.EAST);
            } else {
                placeTorch(level, world, new BlockPos(base.getX() + side, y, base.getZ() + TORCH_IN_A), Direction.NORTH);
                placeTorch(level, world, new BlockPos(base.getX() + side, y, base.getZ() + TORCH_IN_B), Direction.SOUTH);
            }
        }
    }

    private static void placeTorch(ServerLevel level, WorldRandom world, BlockPos pos, Direction facing) {
        // Khóa theo (x, z) của đuốc: cùng seed ra cùng kết quả, không phụ thuộc thứ tự lặp.
        WorldRandom r = world.fork("torch").at(pos.getX(), pos.getZ());

        if (r.chance(LIT_CHANCE)) {
            level.setBlock(pos, lit(facing), FLAGS);                       // sáng, cháy LIT_TICKS
        } else if (r.chance(WEAK_SHARE)) {
            level.scheduleTick(pos, Blocks.WALL_TORCH, TorchBurn.WEAK_TICKS);   // xem TODO 5
            level.setBlock(pos, lit(facing), FLAGS);                       // yếu
        } else {
            level.setBlock(pos, ModBlocksRegistry.WALL_TORCH_OFF.get().defaultBlockState()
                    .setValue(WallTorchBlock.FACING, facing), FLAGS);      // tắt
        }
    }

    private static BlockState lit(Direction facing) {
        return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
    }
    // TODO: toạ độ đuốc dùng chung baseX/baseZ/door.east() với hàm này; cân nhắc tách hàm tính toạ độ chung để hai nơi không lệch nhau.
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

    private static void placeGate(ServerLevel level, double x, double y, double z, float yaw) {
        GateEntity gate = ModEntitiesRegistry.GATE.get().create(level);
        if (gate == null) return;
        gate.setPos(x, y, z);
        gate.setYRot(yaw);
        level.addFreshEntity(gate);
    }

    private static void placeDoorGate(ServerLevel level, Door door, BlockPos origin) {
        BlockPos base = doorBase(door, origin);
        double y = origin.getY() + 1;      // đứng trên sàn
        if (door.east()) {
            placeGate(level, base.getX() + 16.0, y, base.getZ() + 8.0, 90F);
        } else {
            placeGate(level, base.getX() + 8.0, y, base.getZ() + 16.0, 0F);
        }
    }
}
