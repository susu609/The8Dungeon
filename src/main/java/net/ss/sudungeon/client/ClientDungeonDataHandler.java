package net.ss.sudungeon.client;

import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import java.util.List;

public class ClientDungeonDataHandler {
    private static DungeonSavedData clientDungeonData;

    public static void handleDungeonData(List<RoomData> rooms) {
        // Xử lý dữ liệu, ví dụ lưu vào biến tạm thời hoặc hiển thị trên GUI
        clientDungeonData = new DungeonSavedData();
        clientDungeonData.setRooms(rooms);
        SsMod.LOGGER.info("Dungeon data successfully received on client.");
    }

    public static DungeonSavedData getClientDungeonData() {
        return clientDungeonData;
    }
}
