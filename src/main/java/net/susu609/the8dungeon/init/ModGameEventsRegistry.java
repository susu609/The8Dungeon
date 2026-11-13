package net.susu609.the8dungeon.init;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.susu609.the8dungeon.ai.ClawdChat;
import net.susu609.the8dungeon.ai.ClawdKeyStore;
import net.susu609.the8dungeon.ai.ClawdWorker;

/** Đăng ký lệnh và sự kiện trong game (bus game của Forge) liên quan tới Clawd. */
public final class ModGameEventsRegistry {

    private ModGameEventsRegistry() {}

    /** Gọi một lần trong constructor của mod. */
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ClawdChat::onChat);
        MinecraftForge.EVENT_BUS.addListener(ClawdWorker::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ModGameEventsRegistry::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("clawd")
                .then(Commands.literal("key")
                        .then(Commands.argument("key", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ClawdKeyStore.set(player, StringArgumentType.getString(ctx, "key").trim());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§6[Clawd]§r Đã lưu key."), false);
                                    return 1;
                                })))
                .then(Commands.literal("forget")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ClawdKeyStore.clear(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("§6[Clawd]§r Đã xóa key."), false);
                            return 1;
                        })));
    }
}
