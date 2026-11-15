package net.susu609.the8dungeon.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.susu609.the8dungeon.Susu609Mod;

/**
 * Nhật ký debug theo thời gian cho các hành động của bot.
 * Tắt mặc định (không tốn CPU: nội dung dòng log chỉ được dựng khi đang bật).
 * Bật bằng /bot debug on. Mỗi dòng có: tick server, giờ trong ngày, pha ngày/đêm, nhãn hành động.
 * Ngoài ra giữ 300 dòng gần nhất để xem bằng /bot log [n].
 */
final class BotLog {

    private static final int MAX_LINES = 300;
    private static final ArrayDeque<String> RING = new ArrayDeque<>();
    private static volatile boolean enabled;

    private BotLog() {}

    static boolean enabled() {
        return enabled;
    }

    static void setEnabled(boolean on) {
        enabled = on;
    }

    static void log(BotPlayer bot, String tag, Supplier<String> message) {
        if (!enabled) return;
        long day = bot.level().getDayTime() % 24000L;
        String phase = day >= 12700L && day < 23000L ? "đêm" : day >= 11000L && day < 12700L ? "chiều" : "ngày";
        int tick = bot.getServer() != null ? bot.getServer().getTickCount() : -1;
        String line = String.format("[%s] tick=%d giờ=%05d(%s) %-11s %s",
                bot.getGameProfile().getName(), tick, day, phase, tag, message.get());
        synchronized (RING) {
            if (RING.size() >= MAX_LINES) RING.removeFirst();
            RING.addLast(line);
        }
        Susu609Mod.LOGGER.info("[BotAI] {}", line);
    }

    static List<String> tail(int n) {
        synchronized (RING) {
            List<String> all = new ArrayList<>(RING);
            return all.subList(Math.max(0, all.size() - n), all.size());
        }
    }

    static void clear() {
        synchronized (RING) {
            RING.clear();
        }
    }
}
