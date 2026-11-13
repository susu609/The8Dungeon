package net.susu609.the8dungeon.ai;

import java.util.Locale;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ServerChatEvent;
import net.susu609.the8dungeon.world.entity.ClawdEntity;
import net.susu609.the8dungeon.world.item.ClawdSpawnEggItem;

/** Đọc chat bắt đầu bằng "clawd": lệnh cố định chạy ngay, câu khác gửi cho ClawdBrain. */
public final class ClawdChat {

    private static final double HEAR_RANGE = 32.0D;

    private ClawdChat() {}

    public static void onChat(ServerChatEvent event) {
        String raw = event.getRawText().trim();
        if (!raw.toLowerCase(Locale.ROOT).startsWith("clawd")) return;

        ServerPlayer player = event.getPlayer();
        // Xử lý trên luồng server cho chắc chắn an toàn khi đọc thế giới.
        player.server.execute(() -> handle(player, raw));
    }

    private static void handle(ServerPlayer player, String raw) {
        ClawdEntity clawd = findClawd(player);
        if (clawd == null || clawd.level() != player.level() || clawd.distanceTo(player) > HEAR_RANGE) return;

        String text = raw.substring(5).replaceFirst("^[\\s,:]+", "");
        String cmd = text.toLowerCase(Locale.ROOT);

        // Lệnh cố định: 0 token.
        if (cmd.equals("theo") || cmd.equals("đi thôi")) {
            clawd.setOrderedToSit(false);
            clawd.playHappySound();
            return;
        }
        if (cmd.equals("đứng") || cmd.equals("chờ")) {
            clawd.setOrderedToSit(true);
            clawd.playHappySound();
            return;
        }
        if (cmd.equals("dừng")) {
            ClawdWorker.stop(clawd);
            return;
        }
        if (text.isBlank()) return;

        ClawdBrain.ask(player, clawd, text);
    }

    static ClawdEntity findClawd(ServerPlayer player) {
        UUID id = ClawdSpawnEggItem.activeClawdOf(player);
        if (id == null) return null;
        for (ServerLevel level : player.server.getAllLevels()) {
            Entity e = level.getEntity(id);
            if (e instanceof ClawdEntity clawd) return clawd;
        }
        return null;
    }
}
