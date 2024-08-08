package net.ss.sudungeon.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.client.gui.gamemodeselection.MainMenuScreen;

import java.util.function.Supplier;

public class ShowMainMenuScreenPacket {

    public ShowMainMenuScreenPacket() {
        // Constructor không chứa dữ liệu nào cần truyền
    }

    // Mã hóa gói tin (khi gửi đi)
    public static void encode(ShowMainMenuScreenPacket msg, FriendlyByteBuf buffer) {
        // Không cần mã hóa dữ liệu gì thêm
    }

    // Giải mã gói tin (khi nhận được)
    public static ShowMainMenuScreenPacket decode(FriendlyByteBuf buffer) {
        return new ShowMainMenuScreenPacket();
    }

    // Xử lý gói tin
    public static void handle(ShowMainMenuScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Chạy trên luồng chính của client để mở màn hình
            Minecraft.getInstance().setScreen(new MainMenuScreen());
        });
        ctx.get().setPacketHandled(true);
    }
}
