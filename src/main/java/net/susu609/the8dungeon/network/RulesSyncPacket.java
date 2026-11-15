package net.susu609.the8dungeon.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotRules;
import net.susu609.the8dungeon.client.gui.BotRulesScreen;

/** Server -> client: luật hiện tại, thống kê hiệu năng; open=true thì mở màn hình luật. */
public final class RulesSyncPacket {

    /** Một bot trong danh sách. micros = thời gian tick trung bình của bộ não (µs). */
    public record BotRow(String name, boolean inherit, BotRules rules, int micros, String goal) {}

    public final boolean open;
    public final BotRules global;
    public final List<BotRow> bots;
    public final int heapUsedMb;
    public final int heapMaxMb;
    public final int loadedChunks;
    public final int maxBots;
    public final boolean reduced;

    public RulesSyncPacket(boolean open, BotRules global, List<BotRow> bots, int heapUsedMb, int heapMaxMb,
                           int loadedChunks, int maxBots, boolean reduced) {
        this.open = open;
        this.global = global;
        this.bots = bots;
        this.heapUsedMb = heapUsedMb;
        this.heapMaxMb = heapMaxMb;
        this.loadedChunks = loadedChunks;
        this.maxBots = maxBots;
        this.reduced = reduced;
    }

    public static void encode(RulesSyncPacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.open);
        p.global.write(b);
        b.writeVarInt(p.bots.size());
        for (BotRow r : p.bots) {
            b.writeUtf(r.name(), 32);
            b.writeBoolean(r.inherit());
            r.rules().write(b);
            b.writeVarInt(r.micros());
            b.writeUtf(r.goal(), 16);
        }
        b.writeVarInt(p.heapUsedMb);
        b.writeVarInt(p.heapMaxMb);
        b.writeVarInt(p.loadedChunks);
        b.writeVarInt(p.maxBots);
        b.writeBoolean(p.reduced);
    }

    public static RulesSyncPacket decode(FriendlyByteBuf b) {
        boolean open = b.readBoolean();
        BotRules global = BotRules.read(b);
        int n = b.readVarInt();
        List<BotRow> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(new BotRow(b.readUtf(32), b.readBoolean(), BotRules.read(b), b.readVarInt(), b.readUtf(16)));
        }
        return new RulesSyncPacket(open, global, rows, b.readVarInt(), b.readVarInt(), b.readVarInt(),
                b.readVarInt(), b.readBoolean());
    }

    public static void handle(RulesSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BotRulesScreen.accept(p)));
        ctx.get().setPacketHandled(true);
    }
}
