package net.susu609.the8dungeon.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotManager;

/** Client -> server: spectator chọn bot muốn xem (phím [ ]). */
public final class WatchBotPacket {

    private final String name;

    public WatchBotPacket(String name) {
        this.name = name;
    }

    public static void encode(WatchBotPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.name, 32);
    }

    public static WatchBotPacket decode(FriendlyByteBuf buf) {
        return new WatchBotPacket(buf.readUtf(32));
    }

    public static void handle(WatchBotPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null && sender.isSpectator()) BotManager.watch(sender, p.name);
        });
        ctx.get().setPacketHandled(true);
    }
}
