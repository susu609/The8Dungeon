package net.ss.sudungeon.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import static net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk.ROOM_SIZE;

public class DungeonUtils {

    private static void clearRoom (ServerLevel level, BlockPos roomPos) {
        for (int x = 0; x < ROOM_SIZE; x++) {
            for (int y = 0; y < ROOM_SIZE; y++) {
                for (int z = 0; z < ROOM_SIZE; z++) {
                    BlockPos pos = roomPos.offset(x, y, z);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // Xóa phòng khỏi bản đồ dungeon trong DungeonSavedData
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(level);
        dungeonSavedData.removeRoomFromMap(roomPos);
    }


    public static void clearDungeon(ServerLevel level, Iterable<RoomData> rooms) {
        for (RoomData room : rooms) {
            clearRoom(level, room.getPosition());
        }
    }
}
