package net.susu609.the8dungeon.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Quan hệ của một bot với từng người chơi / bot khác. Điểm thân thiết -100..100, lưu trong BotSocialStore.
 * Người lạ bắt đầu 0, bot khác bắt đầu +40. Tăng khi được cho đồ hoặc ở gần lâu mà không đánh nhau;
 * giảm khi bị đánh; điểm âm tự nguôi dần.
 * Bị đánh có bậc: trúng nhầm -> nhắc và lùi; cố ý -> cảnh báo và lùi; đánh tiếp / xuống mức thù -> đánh trả.
 * Làm hòa: cho đồ (khi đang giận được cộng nhiều hơn), ngồi xuống (Shift) gần bot mà không đánh,
 * bot tự đề nghị làm hòa với bot khác sau 30 giây không đánh nhau, hoặc admin dùng /bot peace.
 */
final class BotSocial {

    static final float HOSTILE = -20.0F;
    static final float FRIEND = 30.0F;
    /** Đánh nhau chỉ đẩy xuống tới mức này; còn đường làm hòa khi một bên bị thương. */
    private static final float FIGHT_FLOOR = -70.0F;
    private static final float TRUCE = HOSTILE + 5.0F; // điểm sau khi làm hòa: hết thù nhưng chưa thân

    private static final class Rel {
        float trust;
        int hits;              // số đòn cố ý trong cửa sổ 5 giây gần nhất
        int windowStart;
        int lastHit = -100000;
        int nextOffer;         // lần sớm nhất được đề nghị làm hòa tiếp (bot với bot)
        int truceUntil;        // ngừng bắn sau khi làm hòa giữa trận: đòn lỡ tay không tính
    }

    private final BotPlayer bot;
    private final Map<UUID, Rel> rels = new HashMap<>();
    private boolean loaded;
    private UUID avoid;
    private int avoidUntil;
    private int lastSpeak = -100000;
    private int nextSlowTick;
    private int nextFastTick;

    BotSocial(BotPlayer bot) {
        this.bot = bot;
    }

    private String botName() {
        return bot.getGameProfile().getName();
    }

    private BotSocialStore store() {
        MinecraftServer server = bot.getServer();
        return server != null ? BotSocialStore.get(server) : null;
    }

    /** Nạp điểm đã lưu lần đầu cần dùng (bot mới hồi sinh hoặc server vừa khởi động). */
    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        BotSocialStore s = store();
        if (s == null) return;
        s.all(botName()).forEach((id, v) -> {
            Rel r = new Rel();
            r.trust = v;
            rels.put(id, r);
        });
    }

    private Rel rel(Player p) {
        ensureLoaded();
        return rels.computeIfAbsent(p.getUUID(), k -> {
            Rel r = new Rel();
            r.trust = p instanceof BotPlayer ? 40.0F : 0.0F;
            return r;
        });
    }

    /** Đổi điểm (kẹp -100..100) và lưu. Trả về true nếu vừa thoát khỏi mức thù. */
    private boolean setTrust(UUID id, Rel r, float value) {
        boolean wasHostile = r.trust <= HOSTILE;
        r.trust = Math.max(-100.0F, Math.min(100.0F, value));
        BotSocialStore s = store();
        if (s != null) s.put(botName(), id, r.trust);
        return wasHostile && r.trust > HOSTILE;
    }

    /** Đồng đội: mọi người chơi/bot không thù (điểm thân thiết từ 0 trở lên). */
    boolean isAlly(Player p) {
        return rel(p).trust >= 0.0F;
    }

    float trust(Player p) {
        return rel(p).trust;
    }

    // ===== Bị đánh =====

    /** accidental = đòn trúng nhầm (kẻ đánh đang đánh quái ngay cạnh bot). */
    void onHurtBy(Player attacker, boolean accidental, int now) {
        Rel r = rel(attacker);
        String name = attacker.getGameProfile().getName();
        if (now < r.truceUntil) { // đòn còn đang vung dở lúc vừa làm hòa: bỏ qua
            log(attacker, "đòn trong lúc ngừng bắn, bỏ qua", r);
            return;
        }
        r.lastHit = now;
        if (accidental) {
            setTrust(attacker.getUUID(), r, r.trust - 2.0F);
            say("Ê " + name + ", đánh nhầm mình rồi kìa!", now);
            avoid(attacker, now + 30);
            log(attacker, "trúng nhầm", r);
            return;
        }
        if (r.trust <= HOSTILE) { // đã là thù: mỗi đòn chỉ trừ chút ít, không xuống dưới FIGHT_FLOOR
            if (r.trust > FIGHT_FLOOR) setTrust(attacker.getUUID(), r, Math.max(FIGHT_FLOOR, r.trust - 2.0F));
            avoid = null;
            log(attacker, "đánh tiếp khi đang thù", r);
            return;
        }
        if (now - r.windowStart > 100) {
            r.windowStart = now;
            r.hits = 0;
        }
        r.hits++;
        float drop = 8.0F * r.hits;
        if (r.trust >= FRIEND) drop *= 0.5F; // bạn thân thì dễ bỏ qua hơn
        setTrust(attacker.getUUID(), r, Math.max(FIGHT_FLOOR, r.trust - drop));
        if (r.trust > HOSTILE && r.hits < 3) {
            say(r.hits == 1 ? "Này " + name + ", đừng đánh mình!" : "Mình cảnh báo lần cuối đấy, " + name + "!", now);
            avoid(attacker, now + 60);
        } else {
            setTrust(attacker.getUUID(), r, Math.min(r.trust, HOSTILE - 1.0F));
            say("Được thôi " + name + ", mình không nhịn nữa!", now);
            avoid = null;
        }
        log(attacker, "cố ý (đòn thứ " + r.hits + ")", r);
    }

    // ===== Làm hòa / thân hơn =====

    /** Nhặt được đồ người khác ném cho. Đang giận thì quà có tác dụng mạnh hơn (xin lỗi). */
    void onGift(Player giver, int count, int now) {
        Rel r = rel(giver);
        String name = giver.getGameProfile().getName();
        float gain = r.trust < 0.0F ? Math.min(15.0F, 5.0F * count) : Math.min(10.0F, 2.0F * count);
        if (setTrust(giver.getUUID(), r, r.trust + gain)) {
            say("Thôi được " + name + ", nhận quà rồi, mình bỏ qua.", now);
        } else {
            say(r.trust <= HOSTILE ? "Chưa đủ đâu " + name + "..." : "Cảm ơn " + name + " nhé!", now);
        }
        log(giver, "được cho " + count + " món", r);
    }

    /** Bot khác đề nghị làm hòa. Đồng ý nếu không vừa bị nó đánh và chưa quá căm ghét. */
    boolean acceptPeace(BotPlayer from, int now) {
        Rel r = rel(from);
        String name = from.getGameProfile().getName();
        if (r.trust > HOSTILE) return true; // vốn không thù
        if (now - r.lastHit < 200 || r.trust < -80.0F) {
            say("Chưa đâu " + name + "!", now);
            log(from, "từ chối làm hòa", r);
            return false;
        }
        setTrust(from.getUUID(), r, TRUCE);
        say("Ừ, làm hòa nhé " + name + ".", now);
        log(from, "đồng ý làm hòa", r);
        return true;
    }

    /** Đang đánh nhau với bot khác: đề nghị dừng tay. Trả true nếu bên kia đồng ý. */
    boolean offerPeaceInFight(BotPlayer foe, int now) {
        Rel r = rel(foe);
        String name = foe.getGameProfile().getName();
        say(name + ", dừng tay đi, làm hòa nhé!", now, true);
        if (!foe.acceptPeaceInFight(bot)) {
            log(foe, "đề nghị làm hòa giữa trận bị từ chối", r);
            return false;
        }
        setTrust(foe.getUUID(), r, Math.max(r.trust, TRUCE));
        r.truceUntil = now + 100;
        r.hits = 0;
        avoid = null;
        log(foe, "làm hòa giữa trận", r);
        return true;
    }

    /** Bot khác đề nghị dừng tay giữa trận. Đồng ý khi chính mình mất nhiều máu hoặc chưa quá căm ghét. */
    boolean acceptPeaceInFight(BotPlayer from, float myHp, int now) {
        Rel r = rel(from);
        String name = from.getGameProfile().getName();
        if (r.trust < -80.0F || (myHp >= 0.6F && r.trust < -60.0F)) {
            say("Không đời nào, " + name + "!", now, true);
            return false;
        }
        setTrust(from.getUUID(), r, Math.max(r.trust, TRUCE));
        r.truceUntil = now + 100;
        r.hits = 0;
        avoid = null;
        say("Được, dừng tay. Làm hòa, " + name + ".", now, true);
        log(from, "đồng ý làm hòa giữa trận", r);
        return true;
    }

    /** Admin xóa thù (/bot peace). target null = với tất cả. */
    int forgive(UUID target, int now) {
        ensureLoaded();
        int n = 0;
        for (Map.Entry<UUID, Rel> e : rels.entrySet()) {
            if (target != null && !target.equals(e.getKey())) continue;
            Rel r = e.getValue();
            if (r.trust < 0.0F) {
                setTrust(e.getKey(), r, 0.0F);
                n++;
            }
            r.hits = 0;
        }
        if (target == null || target.equals(avoid)) avoid = null;
        return n;
    }

    // ===== Theo thời gian =====

    void tick(int now) {
        ensureLoaded();
        ServerLevel level = bot.serverLevel();
        if (now >= nextFastTick) { // mỗi giây: người đang thù mà ngồi xuống gần bot thì nguôi dần
            nextFastTick = now + 20;
            for (Player p : level.getEntitiesOfClass(Player.class, bot.getBoundingBox().inflate(6.0D),
                    x -> x != bot && x.isAlive() && !x.isSpectator() && x.isShiftKeyDown())) {
                Rel r = rels.get(p.getUUID());
                if (r == null || r.trust > HOSTILE || now - r.lastHit < 100) continue;
                if (setTrust(p.getUUID(), r, r.trust + 3.0F)) {
                    say("Được rồi " + p.getGameProfile().getName() + ", mình bỏ qua lần này.", now);
                    log(p, "làm hòa (ngồi xuống xin lỗi)", r);
                }
            }
        }
        if (now < nextSlowTick) return;
        nextSlowTick = now + 200; // mỗi 10 giây
        for (Player p : level.getEntitiesOfClass(Player.class, bot.getBoundingBox().inflate(8.0D),
                x -> x != bot && x.isAlive() && !x.isSpectator())) {
            Rel r = rel(p);
            if (now - r.lastHit > 1200 && r.trust < 50.0F && r.trust > HOSTILE) setTrust(p.getUUID(), r, r.trust + 1.0F);
        }
        MinecraftServer server = bot.getServer();
        for (Map.Entry<UUID, Rel> e : rels.entrySet()) {
            Rel r = e.getValue();
            if (r.trust < 0.0F && now - r.lastHit > 2400) setTrust(e.getKey(), r, Math.min(0.0F, r.trust + 0.5F));
            // Bot với bot: 30 giây không đánh nhau thì đề nghị làm hòa
            if (r.trust <= HOSTILE && now - r.lastHit > 600 && now >= r.nextOffer && server != null
                    && server.getPlayerList().getPlayer(e.getKey()) instanceof BotPlayer other && other.isAlive()) {
                String name = other.getGameProfile().getName();
                say(name + ", mình làm hòa nhé?", now);
                if (other.acceptPeace(bot)) {
                    setTrust(e.getKey(), r, TRUCE);
                    log(other, "làm hòa thành công", r);
                } else {
                    r.nextOffer = now + 1200; // một phút sau thử lại
                }
            }
        }
    }

    // ===== Truy vấn cho bộ não =====

    /** Người/bot đang bị coi là thù (gần nhất trong 16 khối), hoặc null. */
    Player grudge() {
        ensureLoaded();
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
        ensureLoaded();
        if (rels.isEmpty()) return "chưa quen ai";
        StringBuilder sb = new StringBuilder();
        MinecraftServer server = bot.getServer();
        rels.forEach((id, r) -> {
            Player p = server != null ? server.getPlayerList().getPlayer(id) : null;
            String n = p != null ? p.getGameProfile().getName() : id.toString().substring(0, 8);
            sb.append(n).append('=').append((int) r.trust).append(r.trust <= HOSTILE ? "(thù) " : " ");
        });
        return sb.toString().trim();
    }

    private void avoid(Player p, int until) {
        avoid = p.getUUID();
        avoidUntil = until;
    }

    private void say(String text, int now) {
        say(text, now, false);
    }

    /** force=true: câu quan trọng (làm hòa) luôn được nói, không bị giới hạn tần suất. */
    private void say(String text, int now, boolean force) {
        if (!force && now - lastSpeak < 60) return; // không nói liên tục
        lastSpeak = now;
        MinecraftServer server = bot.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("<" + botName() + "> " + text), false);
        }
    }

    private void log(Player who, String what, Rel r) {
        BotLog.log(bot, "SOCIAL", () -> who.getGameProfile().getName() + ": " + what + ", thân thiết=" + (int) r.trust);
    }
}
