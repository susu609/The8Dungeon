package net.susu609.the8dungeon.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotInfo;
import net.susu609.the8dungeon.client.gui.BotHud;

/** Server -> client: trạng thái bot đang được theo dõi + danh sách tên mọi bot (để chuyển bằng [ ]). */
public final class BotStatusPacket {

    private final BotInfo info;
    private final List<String> bots;

    public BotStatusPacket(BotInfo info, List<String> bots) {
        this.info = info;
        this.bots = bots;
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
        buf.writeVarInt(p.bots.size());
        for (String b : p.bots) buf.writeUtf(b, 32);
    }

    public static BotStatusPacket decode(FriendlyByteBuf buf) {
        BotInfo info = new BotInfo(buf.readUtf(32), buf.readUtf(16), buf.readFloat(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean());
        int n = Math.min(buf.readVarInt(), 16);
        List<String> bots = new ArrayList<>(n);
        for (int i = 0; i < n; i++) bots.add(buf.readUtf(32));
        return new BotStatusPacket(info, bots);
    }

    public static void handle(BotStatusPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BotHud.accept(p.info, p.bots)));
        ctx.get().setPacketHandled(true);
    }
}
