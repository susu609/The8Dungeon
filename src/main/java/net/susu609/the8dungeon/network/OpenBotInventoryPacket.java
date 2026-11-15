package net.susu609.the8dungeon.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotManager;

/** Client -> server: người đang spectator bấm E, xin xem kho đồ của bot đang theo dõi. */
public final class OpenBotInventoryPacket {

    public static void encode(OpenBotInventoryPacket p, FriendlyByteBuf buf) {
    }

    public static OpenBotInventoryPacket decode(FriendlyByteBuf buf) {
        return new OpenBotInventoryPacket();
    }

    public static void handle(OpenBotInventoryPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null && sender.isSpectator()) BotManager.openInventory(sender);
        });
        ctx.get().setPacketHandled(true);
    }
}
