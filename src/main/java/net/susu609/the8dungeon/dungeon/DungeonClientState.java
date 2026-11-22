package net.susu609.the8dungeon.dungeon;

import net.susu609.the8dungeon.world.WorldRandom;

import java.util.HashSet;
import java.util.Set;

/**
 * Layout dungeon phía client, dựng lại từ seed server gửi xuống.
 * Không import gì của client nên an toàn khi server dedicated nạp lớp này.
 */
public final class DungeonClientState {

    private static final Set<Integer> visited = new HashSet<>();
    private static volatile DungeonLayout layout;

    private DungeonClientState() {
    }

    public static void apply(boolean present, long seed) {
        layout = present ? DungeonLayout.generate(new WorldRandom(seed), DungeonParams.DEFAULT) : null;
    }

    // null nếu chưa có dungeon.
    public static DungeonLayout layout() {
        return layout;
    }

    public static void setVisited(int[] ids) {
        visited.clear();
        for (int id : ids) visited.add(id);
    }

    public static Set<Integer> visited() {
        return visited;
    }
}
