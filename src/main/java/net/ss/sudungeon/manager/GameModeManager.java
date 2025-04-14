package net.ss.sudungeon.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.ss.sudungeon.util.Log;

import static net.ss.sudungeon.world.dimension.DungeonDimension.DUNGEON_DIMENSION;

public class GameModeManager {
    private static GameModeType currentMode = GameModeType.NONE;
    private static ServerLevel level;

    public enum GameModeType {
        NONE,
        DUNGEON,
        WAVES
    }

    public static void startMode (MinecraftServer server, GameModeType mode) {
        level = server.getLevel(DUNGEON_DIMENSION); // hoặc theo mode
        currentMode = mode;
        switch (mode) {
            case DUNGEON -> {
                // logic khởi động dungeon
                Log.i("[GameModeManager] Bắt đầu chế độ Dungeon");
            }
            case WAVES -> {
                // logic khởi động waves
                Log.i("[GameModeManager] Bắt đầu chế độ Waves");
            }
        }
    }

    public static void stopMode () {
        Log.i("[GameModeManager] Kết thúc chế độ: " + currentMode);
        currentMode = GameModeType.NONE;
        level = null;
    }

    public static void restartMode (MinecraftServer server) {
        GameModeType mode = currentMode;
        stopMode();
        startMode(server, mode);
    }

    public static boolean isModeRunning () {
        return currentMode != GameModeType.NONE;
    }

    public static GameModeType getCurrentMode () {
        return currentMode;
    }

    public static ServerLevel getLevel () {
        return level;
    }
}
