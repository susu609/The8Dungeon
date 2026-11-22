package net.susu609.the8dungeon.dungeon;

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
        // TODO: lưu seed vào DungeonSavedData (chép dòng đang có trong DungeonCommand.run)
        return layout;
    }

    /**
     * Toạ độ giữa phòng vào, đứng trên sàn.
     */
    public static Vec3 startPos(DungeonLayout layout) {
        // TODO: chép phép tính cx, cz từ DungeonCommand.run, trả về new Vec3(cx, FLOOR_Y + 1, cz)
    }
}