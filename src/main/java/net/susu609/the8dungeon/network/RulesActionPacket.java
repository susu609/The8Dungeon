package net.susu609.the8dungeon.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.ai.BotManager;
import net.susu609.the8dungeon.ai.BotRules;

/** Client -> server: xin dữ liệu mới (action 0) hoặc áp dụng luật (action 1). target rỗng = luật chung. */
public final class RulesActionPacket {

    public static final int REQUEST = 0;
    public static final int APPLY = 1;

    private final int action;
    private final String target;
    private final boolean inherit;
    private final BotRules rules;

    public RulesActionPacket(int action, String target, boolean inherit, BotRules rules) {
        this.action = action;
        this.target = target;
        this.inherit = inherit;
        this.rules = rules;
    }

    public static void encode(RulesActionPacket p, FriendlyByteBuf b) {
        b.writeByte(p.action);
        b.writeUtf(p.target, 32);
        b.writeBoolean(p.inherit);
        p.rules.write(b);
    }

    public static RulesActionPacket decode(FriendlyByteBuf b) {
        return new RulesActionPacket(b.readByte(), b.readUtf(32), b.readBoolean(), BotRules.read(b));
    }

    public static void handle(RulesActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender == null || !sender.hasPermissions(2)) return; // chỉ OP mới sửa luật
            if (p.action == APPLY) BotManager.applyRules(p.target, p.inherit, p.rules);
            BotManager.sendRules(sender, false);
        });
        ctx.get().setPacketHandled(true);
    }
}
