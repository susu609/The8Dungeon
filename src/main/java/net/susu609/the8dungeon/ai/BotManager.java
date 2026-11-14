package net.susu609.the8dungeon.ai;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Quản lý các người chơi máy: lệnh /bot và hồi sinh khi bot chết.
 * Đăng ký: MinecraftForge.EVENT_BUS.register(BotManager.class);
 */
public final class BotManager {

    private static final Map<String, BotPlayer> BOTS = new LinkedHashMap<>();

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
                .then(Commands.literal("status").executes(BotManager::status)));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || BOTS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) return;
        for (Map.Entry<String, BotPlayer> e : new ArrayList<>(BOTS.entrySet())) {
            BotPlayer bot = e.getValue();
            if (bot.isRemoved() && bot.isAlive()) {
                BOTS.remove(e.getKey()); // bị gỡ từ bên ngoài
            } else if (!bot.isAlive() && ++bot.deadTicks >= 3) {
                server.getPlayerList().remove(bot);
                BOTS.put(e.getKey(), BotPlayer.spawn(server, e.getKey())); // hồi sinh sau ~3 giây
            }
        }
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        BOTS.clear();
    }

    private static int spawn(CommandContext<CommandSourceStack> c, String name) {
        MinecraftServer server = c.getSource().getServer();
        if (BOTS.containsKey(name) || server.getPlayerList().getPlayerByName(name) != null) {
            c.getSource().sendFailure(Component.literal("Đã có người chơi tên " + name));
            return 0;
        }
        BOTS.put(name, BotPlayer.spawn(server, name));
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
