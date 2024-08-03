package net.ss.sudungeon.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ServerboundChangeSkinPacket implements Packet<ServerGamePacketListener> {
    private final ResourceLocation skinLocation;

    public ServerboundChangeSkinPacket (ResourceLocation skinLocation) {
        this.skinLocation = skinLocation;
    }

    public ServerboundChangeSkinPacket (FriendlyByteBuf buf) {
        this.skinLocation = buf.readResourceLocation();
    }

    @Override
    public void write (FriendlyByteBuf buf) {
        buf.writeResourceLocation(skinLocation);
    }

    @Override
    public void handle (@NotNull ServerGamePacketListener listener) {
        if (listener instanceof ServerPlayer player) {
            // Xử lý thay đổi skin cho người chơi
            handleChangeSkin(player);
        }
    }

    private void handleChangeSkin (ServerPlayer player) {
        // Thay đổi skin của người chơi trên server
        // Lưu skin mới vào cơ sở dữ liệu hoặc xử lý theo cách của bạn
        // Sau đó thông báo cho tất cả các client khác về thay đổi nếu cần
    }

    public ResourceLocation getSkinLocation () {
        return skinLocation;
    }
}
