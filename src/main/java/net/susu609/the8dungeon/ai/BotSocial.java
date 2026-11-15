package net.susu609.the8dungeon.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Quan hệ của một bot với từng người chơi / bot khác.
 * Điểm thân thiết -100..100: người lạ bắt đầu 0, bot khác bắt đầu +40.
 * Tăng khi được cho đồ hoặc ở gần lâu mà không đánh nhau; giảm khi bị đánh; điểm âm tự nguôi dần.
 * Bị đánh có bậc: trúng nhầm -> nhắc và lùi; cố ý -> cảnh báo và lùi; đánh tiếp / xuống mức thù -> đánh trả.
 */
final class BotSocial {

    static final float HOSTILE = -20.0F;
    static final float FRIEND = 30.0F;

    private static final class Rel {
        float trust;
        int hits;        // số đòn cố ý trong cửa sổ 5 giây gần nhất
        int windowStart;
        int lastHit = -100000;
    }

    private final BotPlayer bot;
    private final Map<UUID, Rel> rels = new HashMap<>();
    private UUID avoid;
    private int avoidUntil;
    private int lastSpeak = -100000;
    private int nextTick;

    BotSocial(BotPlayer bot) {
        this.bot = bot;
    }

    private Rel rel(Player p) {
        return rels.computeIfAbsent(p.getUUID(), k -> {
            Rel r = new Rel();
            r.trust = p instanceof BotPlayer ? 40.0F : 0.0F;
            return r;
        });
    }

    float trust(Player p) {
        return rel(p).trust;
    }

    /**
     * Bị người/bot đánh. accidental = đòn trúng nhầm (kẻ đánh đang đánh quái ngay cạnh bot).
     */
    void onHurtBy(Player attacker, boolean accidental, int now) {
        Rel r = rel(attacker);
        String name = attacker.getGameProfile().getName();
        r.lastHit = now;
        if (accidental) {
            r.trust = Math.max(-100.0F, r.trust - 2.0F);
            say("Ê " + name + ", đánh nhầm mình rồi kìa!", now);
            avoid(attacker, now + 30);
            log(attacker, "trúng nhầm", r);
            return;
        }
        if (now - r.windowStart > 100) {
            r.windowStart = now;
            r.hits = 0;
        }
        r.hits++;
        float drop = 8.0F * r.hits;
        if (r.trust >= FRIEND) drop *= 0.5F; // bạn thân thì dễ bỏ qua hơn
        r.trust = Math.max(-100.0F, r.trust - drop);
        if (r.trust > HOSTILE && r.hits < 3) {
            say(r.hits == 1 ? "Này " + name + ", đừng đánh mình!" : "Mình cảnh báo lần cuối đấy, " + name + "!", now);
            avoid(attacker, now + 60);
        } else {
            r.trust = Math.min(r.trust, HOSTILE - 1.0F);
            say("Được thôi " + name + ", mình không nhịn nữa!", now);
            avoid = null;
        }
        log(attacker, "cố ý (đòn thứ " + r.hits + ")", r);
    }

    /** Nhặt được đồ người khác ném cho. */
    void onGift(Player giver, int count, int now) {
        Rel r = rel(giver);
        r.trust = Math.min(100.0F, r.trust + Math.min(10.0F, 2.0F * count));
        say("Cảm ơn " + giver.getGameProfile().getName() + " nhé!", now);
        log(giver, "được cho " + count + " món", r);
    }

    /** Mỗi 10 giây: ở gần mà không đánh nhau thì thân hơn; điểm âm nguôi dần. */
    void tick(int now) {
        if (now < nextTick) return;
        nextTick = now + 200;
        ServerLevel level = bot.serverLevel();
        for (Player p : level.getEntitiesOfClass(Player.class, bot.getBoundingBox().inflate(8.0D),
                x -> x != bot && x.isAlive() && !x.isSpectator())) {
            Rel r = rel(p);
            if (now - r.lastHit > 1200 && r.trust < 50.0F) r.trust += 1.0F;
        }
        for (Rel r : rels.values()) {
            if (r.trust < 0.0F && now - r.lastHit > 2400) r.trust = Math.min(0.0F, r.trust + 0.5F);
        }
    }

    /** Người/bot đang bị coi là thù (gần nhất trong 16 khối), hoặc null. */
    Player grudge() {
        Player best = null;
        double bestD = 16.0D * 16.0D;
        for (Player p : bot.serverLevel().players()) {
            if (p == bot || !p.isAlive() || p.isSpectator()) continue;
            Rel r = rels.get(p.getUUID());
            if (r == null || r.trust > HOSTILE) continue;
            double d = p.distanceToSqr(bot);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    /** Người mà bot đang tạm lùi xa (sau khi bị đánh nhầm hoặc bị cảnh báo), hoặc null. */
    Player avoiding(int now) {
        if (avoid == null || now >= avoidUntil) return null;
        Player p = bot.serverLevel().getPlayerByUUID(avoid);
        return p != null && p.isAlive() && !p.isSpectator() && p.distanceToSqr(bot) < 10.0D * 10.0D ? p : null;
    }

    String describe() {
        if (rels.isEmpty()) return "chưa quen ai";
        StringBuilder sb = new StringBuilder();
        MinecraftServer server = bot.getServer();
        rels.forEach((id, r) -> {
            Player p = server != null ? server.getPlayerList().getPlayer(id) : null;
            String n = p != null ? p.getGameProfile().getName() : id.toString().substring(0, 8);
            sb.append(n).append('=').append((int) r.trust).append(' ');
        });
        return sb.toString().trim();
    }

    private void avoid(Player p, int until) {
        avoid = p.getUUID();
        avoidUntil = until;
    }

    private void say(String text, int now) {
        if (now - lastSpeak < 60) return; // không nói liên tục
        lastSpeak = now;
        MinecraftServer server = bot.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(
                    Component.literal("<" + bot.getGameProfile().getName() + "> " + text), false);
        }
    }

    private void log(Player who, String what, Rel r) {
        BotLog.log(bot, "SOCIAL", () -> who.getGameProfile().getName() + ": " + what + ", thân thiết=" + (int) r.trust);
    }
}
