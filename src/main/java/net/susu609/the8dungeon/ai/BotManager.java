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

/**
 * Quản lý các người chơi máy: lệnh /bot và hồi sinh khi bot chết.
 * Đăng ký: MinecraftForge.EVENT_BUS.register(BotManager.class);
 */
public final class BotManager {

    private static final Map<String, BotPlayer> BOTS = new LinkedHashMap<>();
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
        for (Map.Entry<String, BotPlayer> e : new ArrayList<>(BOTS.entrySet())) {
            BotPlayer bot = e.getValue();
            if (bot.isRemoved() && bot.isAlive()) {
                BOTS.remove(e.getKey()); // bị gỡ từ bên ngoài
            } else if (!bot.isAlive() && bot.deadTicks == 0) {
                bot.deadTicks = 1;
                BotLog.log(bot, "DEATH", () -> "bot chết tại " + bot.blockPosition().toShortString() + ", " + bot.status());
            } else if (!bot.isAlive() && ++bot.deadTicks >= 3) {
                server.getPlayerList().remove(bot);
                BOTS.put(e.getKey(), BotPlayer.spawn(server, e.getKey())); // hồi sinh sau ~3 giây
            }
        }
    }

    /** Chỉ gửi cho người đang spectator; ưu tiên bot mà họ đang nhìn qua camera, không thì bot gần nhất. */
    private static void sendToSpectators(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.isSpectator() || p instanceof BotPlayer) continue;
            BotPlayer target = p.getCamera() instanceof BotPlayer cam ? cam : nearest(p);
            if (target == null || !target.isAlive()) continue;
            BotInfo info = target.info();
            // Gửi khi có thay đổi, hoặc mỗi 2 giây để HUD không tự ẩn
            if (!info.equals(LAST_SENT.get(p.getUUID())) || server.getTickCount() % 40 == 0) {
                LAST_SENT.put(p.getUUID(), info);
                Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> p), new BotStatusPacket(info));
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
    }

    private static int spawn(CommandContext<CommandSourceStack> c, String name) {
        MinecraftServer server = c.getSource().getServer();
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
        BOTS.forEach((name, bot) -> c.getSource().sendSuccess(
                () -> Component.literal(name + ": " + bot.status()), false));
        return BOTS.size();
    }
}
