package net.susu609.the8dungeon.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotInfo;
import net.susu609.the8dungeon.client.gui.BotHud;

/** Server -> client: trạng thái bot đang được theo dõi (chỉ gửi cho người ở chế độ spectator). */
public final class BotStatusPacket {

    private final BotInfo info;

    public BotStatusPacket(BotInfo info) {
        this.info = info;
    }

    public static void encode(BotStatusPacket p, FriendlyByteBuf buf) {
        BotInfo i = p.info;
        buf.writeUtf(i.name(), 32);
        buf.writeUtf(i.goal(), 16);
        buf.writeFloat(i.hp());
        buf.writeVarInt(i.food());
        buf.writeVarInt(i.wood());
        buf.writeVarInt(i.stone());
        buf.writeVarInt(i.pick());
        buf.writeBoolean(i.sheltered());
        buf.writeBoolean(i.danger());
    }

    public static BotStatusPacket decode(FriendlyByteBuf buf) {
        return new BotStatusPacket(new BotInfo(buf.readUtf(32), buf.readUtf(16), buf.readFloat(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean()));
    }

    public static void handle(BotStatusPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BotHud.accept(p.info)));
        ctx.get().setPacketHandled(true);
    }
}
