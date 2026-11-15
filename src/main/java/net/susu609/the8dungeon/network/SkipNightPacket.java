package net.susu609.the8dungeon.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotManager;

/** Client -> server: người chơi ở Sáng tạo/Người xem bấm "Bỏ qua đêm" khi mọi bot đã ngủ. */
public final class SkipNightPacket {

    public static void encode(SkipNightPacket p, FriendlyByteBuf buf) {
    }

    public static SkipNightPacket decode(FriendlyByteBuf buf) {
        return new SkipNightPacket();
    }

    public static void handle(SkipNightPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) BotManager.skipNight(sender);
        });
        ctx.get().setPacketHandled(true);
    }
}
