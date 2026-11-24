package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Gói ba thứ mà mọi event dungeon đều cần: dữ liệu lưu, layout (dựng lại từ seed), góc dungeon.
 * Thay cho việc lặp lại 4 dòng get/present/layout/origin ở mỗi event.
 */
public record DungeonView(DungeonSavedData data, DungeonLayout layout, BlockPos origin) {

    /** null nếu không ở Overworld hoặc chưa có dungeon. */
    public static @Nullable DungeonView of(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) return null;
        DungeonSavedData data = DungeonSavedData.get(level);
        if (!data.present()) return null;
        DungeonLayout layout = DungeonService.layout(data.seed());
        return new DungeonView(data, layout, DungeonBuilder.fixedOrigin(layout.params()));
    }

    /** Phòng theo id; không giả định id trùng vị trí trong danh sách. */
    public @Nullable DungeonLayout.Room room(int id) {
        for (DungeonLayout.Room r : layout.rooms()) if (r.id() == id) return r;
        return null;
    }

    /** Phòng chứa chunk (cx, cz); mỗi ô dungeon trùng đúng 1 chunk. */
    public @Nullable DungeonLayout.Room roomAtChunk(int chunkX, int chunkZ) {
        return layout.roomAt(chunkX - (origin.getX() >> 4), chunkZ - (origin.getZ() >> 4));
    }
}
