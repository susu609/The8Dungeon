package net.ss.sudungeon.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.ss.sudungeon.network.packet.ServerboundChangeSkinPacket;

public class PacketHandler {
    public static void handleChangeSkin (ServerPlayer player, ServerboundChangeSkinPacket packet) {
        // Thay đổi skin của người chơi trên server
        ResourceLocation newSkin = packet.getSkinLocation();
        // Lưu skin mới vào cơ sở dữ liệu hoặc xử lý theo cách của bạn
        // Sau đó thông báo cho tất cả các client khác về thay đổi
    }
}