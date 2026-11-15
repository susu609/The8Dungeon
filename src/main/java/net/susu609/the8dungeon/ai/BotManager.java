package net.susu609.the8dungeon.ai;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.network.BotStatusPacket;
import net.susu609.the8dungeon.network.RulesSyncPacket;

/**
 * Quản lý các người chơi máy: lệnh /bot và hồi sinh khi bot chết.
 * Đăng ký: MinecraftForge.EVENT_BUS.register(BotManager.class);
 */
public final class BotManager {

    private static final Map<String, BotPlayer> BOTS = new LinkedHashMap<>();
    /** Số bot tối đa. Mỗi bot nên có khoảng 1 GB heap (chủ yếu là chunk quanh nó). */
    public static final int MAX_BOTS = 4;
    private static boolean reduced;
    private static boolean humans = true;

    /** true khi heap gần đầy: bot suy nghĩ thưa hơn, quét ít hơn, và không cho thêm bot. */
    public static boolean reduced() {
        return reduced;
    }

    /** true nếu có ít nhất một người chơi thật trên server. */
    public static boolean humansOnline() {
        return humans;
    }

    private static final Map<UUID, String> WATCH = new HashMap<>(); // spectator -> bot đã chọn bằng [ ]
    private static final Map<UUID, BotInfo> LAST_SENT = new HashMap<>();

    private BotManager() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bot")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(c -> spawn(c, "Bot"))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> spawn(c, StringArgumentType.getString(c, "name")))))
                .then(Commands.literal("kill")
                        .executes(c -> kill(c, null))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> kill(c, StringArgumentType.getString(c, "name")))))
                .then(Commands.literal("status").executes(BotManager::status))
                .then(Commands.literal("rule")
                        .then(Commands.argument("target", StringArgumentType.word())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes(BotManager::setRule)))))
                .then(Commands.literal("peace")
                        .then(Commands.argument("bot", StringArgumentType.word())
                                .executes(c -> peace(c, null))
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(c -> peace(c, StringArgumentType.getString(c, "player"))))))
                .then(Commands.literal("social").executes(c -> {
                    BOTS.forEach((n, b) -> c.getSource().sendSuccess(() -> Component.literal(n + ": " + b.socialStatus()), false));
                    return BOTS.size();
                }))
                .then(Commands.literal("gui").executes(c -> {
                    sendRules(c.getSource().getPlayerOrException(), true);
                    return 1;
                }))
                .then(Commands.literal("debug")
                        .then(Commands.literal("on").executes(c -> debug(c, true)))
                        .then(Commands.literal("off").executes(c -> debug(c, false))))
                .then(Commands.literal("log")
                        .executes(c -> showLog(c, 20))
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, 100))
                                .executes(c -> showLog(c, IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("logclear").executes(c -> {
                    BotLog.clear();
                    return 1;
                })));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || BOTS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 10 == 0) sendToSpectators(server);
        if (server.getTickCount() % 20 != 0) return;
        humans = server.getPlayerList().getPlayers().stream().anyMatch(p -> !(p instanceof BotPlayer));
        Runtime rt = Runtime.getRuntime();
        double ratio = (double) (rt.totalMemory() - rt.freeMemory()) / rt.maxMemory();
        if (!reduced && ratio > 0.90D) reduced = true;       // có độ trễ: vào ở 90%, ra ở 75%
        else if (reduced && ratio < 0.75D) reduced = false;
        for (Map.Entry<String, BotPlayer> e : new ArrayList<>(BOTS.entrySet())) {
            BotPlayer bot = e.getValue();
            if (bot.isRemoved() && bot.isAlive()) {
                BOTS.remove(e.getKey()); // bị gỡ từ bên ngoài
            } else if (!bot.isAlive() && bot.deadTicks == 0) {
                bot.deadTicks = 1;
                BotLog.log(bot, "DEATH", () -> "bot chết tại " + bot.blockPosition().toShortString() + ", " + bot.status());
            } else if (!bot.isAlive() && ++bot.deadTicks >= 3) {
                bot.prepareRespawn(); // lưu ở trạng thái sống để bot mới không bị vẽ như xác
                server.getPlayerList().remove(bot);
                BOTS.put(e.getKey(), BotPlayer.spawn(server, e.getKey(), true)); // hồi sinh sau ~3 giây, không báo "joined"
            }
        }
    }

    // ===== Quan hệ: bị đánh, được cho đồ =====

    @SubscribeEvent
    public static void onHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (!(event.getEntity() instanceof BotPlayer bot)) return;
        if (!(event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player attacker) || attacker == bot) return;
        bot.onHurtBy(attacker, isAccidental(attacker, bot));
    }

    /** Trúng nhầm: kẻ đánh vừa đánh một con khác (trong 2 giây) đang ở sát bot, ví dụ đòn quét hay tên lạc. */
    private static boolean isAccidental(net.minecraft.world.entity.player.Player attacker, BotPlayer bot) {
        net.minecraft.world.entity.LivingEntity last = attacker.getLastHurtMob();
        return last != null && last != bot && last.isAlive()
                && attacker.tickCount - attacker.getLastHurtMobTimestamp() < 40
                && last.distanceToSqr(bot) < 4.0D * 4.0D;
    }

    @SubscribeEvent
    public static void onPickup(net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof BotPlayer bot
                && event.getOriginalEntity().getOwner() instanceof net.minecraft.world.entity.player.Player giver
                && giver != bot) {
            bot.onGift(giver, event.getStack().getCount());
        }
    }

    /** Chỉ gửi cho người đang spectator; ưu tiên bot mà họ đang nhìn qua camera, không thì bot gần nhất. */
    private static void sendToSpectators(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.isSpectator() || p instanceof BotPlayer) continue;
            BotPlayer watched = BOTS.get(WATCH.get(p.getUUID()));
            BotPlayer target = p.getCamera() instanceof BotPlayer cam ? cam
                    : watched != null && watched.isAlive() && watched.level() == p.level() ? watched : nearest(p);
            if (target == null || !target.isAlive()) continue;
            BotInfo info = target.info();
            // Gửi khi có thay đổi, hoặc mỗi 2 giây để HUD không tự ẩn
            if (!info.equals(LAST_SENT.get(p.getUUID())) || server.getTickCount() % 40 == 0) {
                LAST_SENT.put(p.getUUID(), info);
                Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> p), new BotStatusPacket(info, new ArrayList<>(BOTS.keySet())));
            }
        }
    }

    /** Mở kho đồ của bot đang xem cho spectator, ở dạng chỉ đọc. */
    public static void openInventory(ServerPlayer viewer) {
        BotPlayer bot = viewer.getCamera() instanceof BotPlayer cam ? cam : nearest(viewer);
        if (bot == null || !bot.isAlive()) return;
        viewer.openMenu(new SimpleMenuProvider(
                (id, inv, player) -> new ChestMenu(MenuType.GENERIC_9x4, id, inv, bot.getInventory(), 4) {
                    @Override
                    public void clicked(int slot, int button, ClickType type, Player p) {
                        // chỉ xem: bỏ qua mọi thao tác chuột
                    }

                    @Override
                    public ItemStack quickMoveStack(Player p, int index) {
                        return ItemStack.EMPTY;
                    }

                    @Override
                    public boolean stillValid(Player p) {
                        return bot.isAlive();
                    }
                },
                Component.literal("Kho đồ của " + bot.getGameProfile().getName())));
    }

    /** Spectator chọn bot để xem: ghi nhớ, đổi camera nếu đang nhập vào bot khác, dịch chuyển tới nếu bot ở xa. */
    public static void watch(ServerPlayer viewer, String name) {
        BotPlayer bot = BOTS.get(name);
        if (bot == null || !bot.isAlive()) return;
        WATCH.put(viewer.getUUID(), name);
        LAST_SENT.remove(viewer.getUUID()); // gửi HUD mới ngay
        if (viewer.getCamera() instanceof BotPlayer && viewer.getCamera() != bot) viewer.setCamera(bot);
        if (bot.level() != viewer.level() || bot.distanceToSqr(viewer) > 32.0D * 32.0D) {
            viewer.teleportTo(bot.serverLevel(), bot.getX(), bot.getY() + 10.0D, bot.getZ(), viewer.getYRot(), viewer.getXRot());
        }
    }

    private static BotPlayer nearest(ServerPlayer p) {
        BotPlayer best = null;
        double bestD = Double.MAX_VALUE;
        for (BotPlayer b : BOTS.values()) {
            if (b.level() != p.level()) continue;
            double d = b.distanceToSqr(p);
            if (d < bestD) {
                bestD = d;
                best = b;
            }
        }
        return best;
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        BOTS.clear();
        LAST_SENT.clear();
        WATCH.clear();
    }

    private static int spawn(CommandContext<CommandSourceStack> c, String name) {
        MinecraftServer server = c.getSource().getServer();
        if (BOTS.size() >= MAX_BOTS) {
            c.getSource().sendFailure(Component.literal("Tối đa " + MAX_BOTS + " bot (mỗi bot ~1 GB RAM)"));
            return 0;
        }
        if (reduced) {
            c.getSource().sendFailure(Component.literal("Heap server gần đầy, chưa thêm bot được"));
            return 0;
        }
        if (BOTS.containsKey(name) || server.getPlayerList().getPlayerByName(name) != null) {
            c.getSource().sendFailure(Component.literal("Đã có người chơi tên " + name));
            return 0;
        }
        BotPlayer created = BotPlayer.spawn(server, name);
        BOTS.put(name, created);
        BotLog.log(created, "SPAWN", () -> "tại " + created.blockPosition().toShortString());
        c.getSource().sendSuccess(() -> Component.literal("Đã tạo bot " + name), true);
        return 1;
    }

    private static int kill(CommandContext<CommandSourceStack> c, String name) {
        MinecraftServer server = c.getSource().getServer();
        int n = 0;
        for (Map.Entry<String, BotPlayer> e : new ArrayList<>(BOTS.entrySet())) {
            if (name == null || name.equals(e.getKey())) {
                server.getPlayerList().remove(e.getValue());
                BOTS.remove(e.getKey());
                n++;
            }
        }
        int removed = n;
        c.getSource().sendSuccess(() -> Component.literal("Đã gỡ " + removed + " bot"), true);
        return n;
    }

    // ===== Luật =====

    /** Áp dụng luật từ client (đã kiểm quyền). target rỗng = luật chung; bot không tồn tại thì bỏ qua. */
    public static void applyRules(String target, boolean inherit, BotRules rules) {
        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        BotRules r = rules.copy();
        r.clamp();
        BotRulesStore store = BotRulesStore.get(server);
        if (target.isEmpty()) store.setGlobal(r);
        else if (BOTS.containsKey(target)) store.setBot(target, inherit, r);
    }

    /** Gửi luật hiện tại và thống kê hiệu năng cho một người chơi; open=true thì mở màn hình. */
    public static void sendRules(ServerPlayer to, boolean open) {
        MinecraftServer server = to.getServer();
        if (server == null) return;
        BotRulesStore store = BotRulesStore.get(server);
        java.util.List<RulesSyncPacket.BotRow> rows = new ArrayList<>();
        for (Map.Entry<String, BotPlayer> e : BOTS.entrySet()) {
            BotRulesStore.Entry en = store.entry(e.getKey());
            boolean inherit = en == null || en.inherit;
            BotRules shown = en != null ? en.rules : store.global();
            rows.add(new RulesSyncPacket.BotRow(e.getKey(), inherit, shown,
                    (int) (e.getValue().avgNanos / 1000L), e.getValue().info().goal()));
        }
        Runtime rt = Runtime.getRuntime();
        int used = (int) ((rt.totalMemory() - rt.freeMemory()) >> 20);
        int max = (int) (rt.maxMemory() >> 20);
        int chunks = to.serverLevel().getChunkSource().getLoadedChunksCount();
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> to),
                new RulesSyncPacket(open, store.global(), rows, used, max, chunks, MAX_BOTS, reduced));
    }

    /** /bot rule <all|tên bot> <khóa> <giá trị>, ví dụ: /bot rule all fight false, /bot rule Bot roam 96 */
    private static int setRule(CommandContext<CommandSourceStack> c) {
        String target = StringArgumentType.getString(c, "target");
        String key = StringArgumentType.getString(c, "key");
        String value = StringArgumentType.getString(c, "value");
        boolean global = target.equals("all");
        if (!global && !BOTS.containsKey(target)) {
            c.getSource().sendFailure(Component.literal("Không có bot tên " + target));
            return 0;
        }
        BotRulesStore store = BotRulesStore.get(c.getSource().getServer());
        BotRules r = (global ? store.global() : store.effective(target)).copy();
        try {
            boolean b = Boolean.parseBoolean(value);
            switch (key) {
                case "fight" -> r.fight = b;
                case "hunt" -> r.hunt = b;
                case "mine" -> r.mine = b;
                case "shelter" -> r.shelter = b;
                case "explore" -> r.explore = b;
                case "collect" -> r.collect = b;
                case "cook" -> r.cook = b;
                case "pause" -> r.pauseWhenAlone = b;
                case "flee" -> r.fleeHpPct = Integer.parseInt(value);
                case "eat" -> r.eatBelow = Integer.parseInt(value);
                case "roam" -> r.roam = Integer.parseInt(value);
                case "think" -> r.thinkEvery = Integer.parseInt(value);
                case "budget" -> r.scanBudget = Integer.parseInt(value);
                default -> {
                    c.getSource().sendFailure(Component.literal("Khóa hợp lệ: fight hunt mine shelter explore collect cook pause flee eat roam think budget"));
                    return 0;
                }
            }
        } catch (NumberFormatException e) {
            c.getSource().sendFailure(Component.literal("Giá trị không phải số: " + value));
            return 0;
        }
        applyRules(global ? "" : target, false, r);
        c.getSource().sendSuccess(() -> Component.literal("Đã đặt " + key + "=" + value + " cho " + target), true);
        return 1;
    }

    /** /bot peace <bot> [người]: xóa thù của bot (với một người hoặc tất cả). */
    private static int peace(CommandContext<CommandSourceStack> c, String player) {
        String name = StringArgumentType.getString(c, "bot");
        BotPlayer bot = BOTS.get(name);
        if (bot == null) {
            c.getSource().sendFailure(Component.literal("Không có bot tên " + name));
            return 0;
        }
        UUID target = null;
        if (player != null) {
            ServerPlayer p = c.getSource().getServer().getPlayerList().getPlayerByName(player);
            target = p != null ? p.getUUID() : net.minecraft.core.UUIDUtil.createOfflinePlayerUUID(player);
        }
        int n = bot.forgive(target);
        c.getSource().sendSuccess(() -> Component.literal(name + " đã xóa " + n + " mối thù"), true);
        return n;
    }

    private static int debug(CommandContext<CommandSourceStack> c, boolean on) {
        BotLog.setEnabled(on);
        c.getSource().sendSuccess(() -> Component.literal("Debug log bot: " + (on ? "BẬT" : "TẮT")), true);
        return 1;
    }

    private static int showLog(CommandContext<CommandSourceStack> c, int n) {
        java.util.List<String> lines = BotLog.tail(n);
        if (lines.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal(
                    BotLog.enabled() ? "Chưa có dòng log nào" : "Debug đang tắt. Dùng /bot debug on"), false);
            return 0;
        }
        for (String line : lines) c.getSource().sendSuccess(() -> Component.literal(line), false);
        return lines.size();
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        if (BOTS.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("Chưa có bot. Dùng /bot spawn"), false);
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Build: " + net.susu609.the8dungeon.BuildInfo.line()), false);
        BOTS.forEach((name, bot) -> c.getSource().sendSuccess(
                () -> Component.literal(name + ": " + bot.status()), false));
        return BOTS.size();
    }
}
