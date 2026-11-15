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

/**
 * Server -> client (người ở Người xem / Sáng tạo): bot đang theo dõi (null với Sáng tạo), trạng thái
 * mọi bot (hàng mặt trên overlay), và có hiện nút "Bỏ qua đêm" không.
 */
public final class BotStatusPacket {

    private final BotInfo focus;
    private final List<BotInfo> all;
    private final boolean canSkipNight;

    public BotStatusPacket(BotInfo focus, List<BotInfo> all, boolean canSkipNight) {
        this.focus = focus;
        this.all = all;
        this.canSkipNight = canSkipNight;
    }

    private static void write(FriendlyByteBuf buf, BotInfo i) {
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

    private static BotInfo read(FriendlyByteBuf buf) {
        return new BotInfo(buf.readUtf(32), buf.readUtf(16), buf.readFloat(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean());
    }

    public static void encode(BotStatusPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.focus != null);
        if (p.focus != null) write(buf, p.focus);
        buf.writeVarInt(p.all.size());
        for (BotInfo i : p.all) write(buf, i);
        buf.writeBoolean(p.canSkipNight);
    }

    public static BotStatusPacket decode(FriendlyByteBuf buf) {
        BotInfo focus = buf.readBoolean() ? read(buf) : null;
        int n = Math.min(buf.readVarInt(), 16);
        List<BotInfo> all = new ArrayList<>(n);
        for (int i = 0; i < n; i++) all.add(read(buf));
        return new BotStatusPacket(focus, all, buf.readBoolean());
    }

    public static void handle(BotStatusPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BotHud.accept(p.focus, p.all, p.canSkipNight)));
        ctx.get().setPacketHandled(true);
    }
}
