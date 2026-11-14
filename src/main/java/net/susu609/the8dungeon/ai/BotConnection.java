package net.susu609.the8dungeon.ai;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

/**
 * Kết nối giả cho người chơi máy: không có socket thật, mọi gói tin gửi đi đều bị bỏ.
 * EmbeddedChannel chỉ để các hook của Forge đọc channel() không bị null.
 */
final class BotConnection extends Connection {

    BotConnection() {
        super(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(this);
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener listener) {
    }

    @Override
    public void disconnect(Component reason) {
    }

    @Override
    public void setReadOnly() {
    }

    @Override
    public void handleDisconnection() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
