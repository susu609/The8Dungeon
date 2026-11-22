package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.susu609.the8dungeon.world.WorldRandom;

public final class DungeonService {
    private DungeonService() {
    }

    /**
     * Sinh layout, dựng block, lưu seed. Trả về layout để nơi gọi dùng tiếp (dịch người chơi, gửi seed...).
     */
    public static DungeonLayout build(ServerLevel level, long seed) {
        DungeonParams params = DungeonParams.DEFAULT;
        WorldRandom world = new WorldRandom(seed);
        DungeonLayout layout = DungeonLayout.generate(world, params);
        DungeonBuilder.build(level, layout, DungeonBuilder.fixedOrigin(params), world);
        DungeonSavedData.get(level).set(seed);
        return layout;
    }

    /**
     * Toạ độ giữa phòng vào, đứng trên sàn.
     */
    public static Vec3 startPos(DungeonLayout layout) {
        // Vị trí cố định: phòng vào ở chunk (0, 0), sàn ở y = 0.
        DungeonParams params = DungeonParams.DEFAULT;
        BlockPos origin = DungeonBuilder.fixedOrigin(params);
        DungeonLayout.Room start = layout.start();
        double cx = origin.getX() + (start.cx() + start.cellsX() / 2.0) * RoomPreset.CELL;
        double cz = origin.getZ() + (start.cz() + start.cellsZ() / 2.0) * RoomPreset.CELL;
        return new Vec3(cx, DungeonBuilder.FLOOR_Y + 1, cz);
    }
}