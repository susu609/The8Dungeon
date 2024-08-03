package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class DungeonManager {
    // Phương thức tính toán maxChallengePoints mới (ví dụ)
    public static int calculateNewMaxChallengePoints (int currentMaxChallengePoints) {
        return currentMaxChallengePoints + 5; // Tăng 5 điểm mỗi lần vào dungeon
    }

    // Phương thức spawn quái vật dựa trên maxChallengePoints (bạn cần tự triển khai)
    public static void spawnMobsInRoom (ServerLevel world, BlockPos roomPos, int maxChallengePoints) {
        int maxMobs = 8; // Giới hạn tối đa số quái vật trong phòng
        int numMobs = world.random.nextInt(maxMobs) + 1; // Sinh số lượng quái vật ngẫu nhiên từ 1 đến maxMobs
        // ... (phần còn lại của phương thức giữ nguyên)
    }
}