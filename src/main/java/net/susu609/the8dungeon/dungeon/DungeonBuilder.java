package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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
import net.susu609.the8dungeon.world.level.block.entity.PedestalEntity;
import net.susu609.the8dungeon.world.level.block.entity.PropEntity;
import net.susu609.the8dungeon.world.torch.TorchBurn;

import java.util.ArrayList;
import java.util.List;

/**
 * Biến DungeonLayout (dữ liệu) thành block thật trong world.
 * <p>
 * Cắt dọc một phòng (trục Y, tính từ origin.y):
 * y = 0                  sàn  : STONE
 * y = 1 .. 16            rỗng : AIR (tường ngoài rìa là STONE_BRICKS)
 * y = 17                 trần : STONE_BRICKS
 * <p>
 * Mỗi phòng có tường riêng (tường dày 1). Hai phòng kề nhau có 2 lớp tường liền nhau,
 * cửa là lỗ khoét xuyên qua cả hai lớp: rộng 2 block (vị trí 7 và 8 của ô lưới), cao DOOR_HEIGHT.
 */
public final class DungeonBuilder {

    // Cao độ mặt sàn cố định của mọi dungeon.
    public static final int FLOOR_Y = 0;
    // 2 = gửi thay đổi cho client, 16 = bỏ qua cập nhật hình dạng block lân cận (nhanh hơn khi đặt hàng loạt).
    public static final String ROOM_TAG = "room_";   // tag gắn lên quái: "room_<id>"
    private static final int CELL = RoomPreset.CELL;
    private static final int HEIGHT = RoomPreset.INTERIOR_HEIGHT;
    private static final int DOOR_HEIGHT = 3;
    private static final int WALL_A = 15, WALL_B = 16;     // hai lớp tường của cửa
    private static final int DOOR_LO = 7, DOOR_HI = 8;     // hai block của lỗ cửa
    private static final int TORCH_SIDE_LO = 6, TORCH_SIDE_HI = 9;   // hai bên mép cửa
    private static final int TORCH_IN_A = 14, TORCH_IN_B = 17;       // sát mặt trong tường A và B
    private static final double LIT_CHANCE = 0.75;
    private static final double WEAK_SHARE = 0.5;   // tạm: trong 25% còn lại, nửa yếu, nửa tắt; bạn chốt số thật
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private DungeonBuilder() {
    }

    static List<int[]> doorPoints(DungeonLayout layout, Room room) {
        List<int[]> out = new ArrayList<>();
        for (Door d : layout.doors()) {
            if (d.a() != room.id() && d.b() != room.id()) continue;     // cửa của phòng khác
            int dx = (d.cx() - room.cx()) * CELL + (d.east() ? WALL_B : DOOR_HI);
            int dz = (d.cz() - room.cz()) * CELL + (d.east() ? DOOR_HI : WALL_B);
            out.add(new int[]{dx, dz});
        }
        return out;
    }

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

    public static AABB roomBox(BlockPos origin, DungeonLayout.Room room) {
        int x0 = origin.getX() + room.cx() * CELL, z0 = origin.getZ() + room.cz() * CELL;
        return new AABB(x0, origin.getY(), z0, x0 + room.cellsX() * CELL, origin.getY() + totalHeight(), z0 + room.cellsZ() * CELL);
    }

    public static List<MobSpot> planMobs(WorldRandom world, DungeonLayout.Room room, int minBudget, int maxBudget, List<int[]> doors) {
        WorldRandom r = world.fork("mobs").at(room.cx(), room.cz());   // mỗi phòng 1 dãy riêng
        int budget = r.nextRange(minBudget, maxBudget);
        List<MobSpot> out = new ArrayList<>();
        RoomMob[] all = RoomMob.values();
        while (budget > 0) {
            RoomMob m = all[r.nextInt(all.length)];
            if (m.cost > budget) m = RoomMob.ZOMBIE;                   // hết tiền thì lấy loại rẻ nhất
            budget -= m.cost;
            int lx, lz, tries = 0;
            do {
                lx = r.nextRange(3, room.cellsX() * 16 - 4);
                lz = r.nextRange(3, room.cellsZ() * 16 - 4);
            } while (nearDoor(lx, lz, doors) && ++tries < 10);       // tối đa 10 lần, tránh vòng lặp vô tận
            out.add(new MobSpot(m, lx, lz));
        }
        return out;
    }

    private static void decorateRoom(ServerLevel level, DungeonLayout layout, Room room, BlockPos origin) {
        switch (layout.roleOf(room)) {
            case TREASURE -> placeTreasure(level, room, origin);
            default -> {
            }                                    // các loại khác: làm sau
        }
    }

    private static void placeTreasure(ServerLevel level, Room room, BlockPos origin) {
        PedestalEntity p = ModEntitiesRegistry.PEDESTAL.get().create(level);
        if (p == null) return;
        double cx = origin.getX() + (room.cx() + room.cellsX() / 2.0) * CELL;   // tâm phòng
        double cz = origin.getZ() + (room.cz() + room.cellsZ() / 2.0) * CELL;
        p.setPos(cx, origin.getY() + 1, cz);
        level.addFreshEntity(p);
    }

    private static boolean nearDoor(int lx, int lz, List<int[]> doors) {
        for (int[] d : doors) {
            int dx = lx - d[0], dz = lz - d[1];
            if (dx * dx + dz * dz < 6 * 6) return true;      // so bình phương, khỏi phải tính căn
        }
        return false;
    }

    /**
     * @param origin góc (x, y, z) của ô lưới (0, 0); x và z nên chia hết cho 16 để mỗi phòng khớp đúng 1 chunk.
     *               y là cao độ của mặt sàn.
     * @param world  nguồn ngẫu nhiên theo seed (đuốc roll theo toạ độ).
     */
    public static void build(ServerLevel level, DungeonLayout layout, BlockPos origin, WorldRandom world) {
        if (origin.getY() < level.getMinBuildHeight() || origin.getY() + totalHeight() > level.getMaxBuildHeight()) {
            throw new IllegalArgumentException("Dungeon vượt quá giới hạn chiều cao của world.");
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Dựng lại thì xoá entity cũ (gate, bệ, quái của phòng); block thì bị ghi đè, entity thì không.
        AABB area = new AABB(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + layout.params().gridW() * CELL, origin.getY() + totalHeight(), origin.getZ() + layout.params().gridD() * CELL);
        level.getEntitiesOfClass(PropEntity.class, area).forEach(Entity::discard);
        level.getEntitiesOfClass(Mob.class, area, DungeonBuilder::isRoomMob).forEach(Entity::discard);
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
                        level.setBlock(pos.set(x0 + dx, origin.getY() + dy, z0 + dz), wall ? bricks : air, FLAGS);
                    }
                    level.setBlock(pos.set(x0 + dx, origin.getY() + HEIGHT + 1, z0 + dz), bricks, FLAGS);
                }
            }
        }

        // Khoét cửa sau khi mọi phòng đã dựng xong, nếu không tường của phòng dựng sau sẽ lấp lại lỗ.
        for (Door door : layout.doors()) {
            carveDoor(level, door, origin, air, pos);
        }
        // Đuốc đặt SAU khi khoét: block tường cạnh lỗ (local 6 và 9) vẫn là gạch nên đuốc có chỗ bám.
        for (Door door : layout.doors()) {
            placeDoorTorches(level, world, door, origin);
        }
        for (Door door : layout.doors()) {
            placeDoorGate(level, door, origin);
        }
        for (Room room : layout.rooms()) {
            spawnRoomMobs(level, layout, room, origin, world);
            decorateRoom(level, layout, room, origin);
        }
    }

    private static void spawnRoomMobs(ServerLevel level, DungeonLayout layout, Room room, BlockPos origin, WorldRandom world) {
        int count = switch (layout.roleOf(room)) {
            case NORMAL -> 3;
            case ELITE -> 4;
            case BOSS -> 1;
            default -> 0;                       // START, SHOP...: phòng an toàn
        };
        int[] budget = switch (layout.roleOf(room)) {
            case NORMAL -> new int[]{1, 3};
            case ELITE -> new int[]{4, 5};
            default -> null;                                      // phòng an toàn
        };
        if (budget == null) return;
        int x0 = origin.getX() + room.cx() * CELL, z0 = origin.getZ() + room.cz() * CELL;
        for (MobSpot s : planMobs(world, room, budget[0], budget[1], doorPoints(layout, room))) {
            Mob mob = s.mob().type.create(level);
            if (mob == null) continue;
            mob.moveTo(x0 + s.lx() + 0.5, origin.getY() + 1, z0 + s.lz() + 0.5, 0F, 0F);
            mob.setNoAi(true);                  // đứng im
            mob.setInvulnerable(true);          // đánh không chết
            mob.setPersistenceRequired();       // không tự biến mất khi người chơi đi xa
            mob.addTag(ROOM_TAG + room.id());
            level.addFreshEntity(mob);
        }
    }

    private static boolean isRoomMob(Mob mob) {
        return mob.getTags().stream().anyMatch(t -> t.startsWith(ROOM_TAG));
    }

    // Góc của ô lưới chứa cửa; mọi toạ độ của cửa (lỗ, đuốc, gate) đều tính từ đây.
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
            // Yếu: lên lịch WEAK_TICKS TRƯỚC rồi mới đặt đuốc. Cùng (pos, block) chỉ giữ lịch ĐẦU TIÊN,
            // nên LIT_TICKS mà mixin onPlace lên lịch sau đó bị bỏ qua. Đảo thứ tự hai dòng là hỏng.
            level.scheduleTick(pos, Blocks.WALL_TORCH, TorchBurn.WEAK_TICKS);
            level.setBlock(pos, lit(facing), FLAGS);
        } else {
            level.setBlock(pos, ModBlocksRegistry.WALL_TORCH_OFF.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing), FLAGS);      // tắt
        }
    }

    private static BlockState lit(Direction facing) {
        return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
    }

    private static void carveDoor(ServerLevel level, Door door, BlockPos origin, BlockState air, BlockPos.MutableBlockPos pos) {
        BlockPos base = doorBase(door, origin);

        // Hai lớp tường: local 15 của ô này và local 0 của ô bên cạnh (tức WALL_A, WALL_B tính từ góc ô này).
        // Lỗ rộng 2 block: DOOR_LO..DOOR_HI. Cửa đông: tường chạy theo x, lỗ theo z; cửa nam thì ngược lại.
        for (int wall = WALL_A; wall <= WALL_B; wall++) {
            for (int side = DOOR_LO; side <= DOOR_HI; side++) {
                for (int dy = 1; dy <= DOOR_HEIGHT; dy++) {
                    int x = base.getX() + (door.east() ? wall : side);
                    int z = base.getZ() + (door.east() ? side : wall);
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
            placeGate(level, base.getX() + WALL_B, y, base.getZ() + DOOR_HI, 90F);   // giữa lỗ: x = 16.0, z = 8.0
        } else {
            placeGate(level, base.getX() + DOOR_HI, y, base.getZ() + WALL_B, 0F);
        }
    }

    // Kết quả: loại quái + vị trí local trong phòng
    public record MobSpot(RoomMob mob, int lx, int lz) {
    }
}
