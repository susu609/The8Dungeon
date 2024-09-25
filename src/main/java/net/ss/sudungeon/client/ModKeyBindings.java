package net.ss.sudungeon.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.PlayerStatsGui;
import net.ss.sudungeon.network.packet.RequestDungeonDataPacket;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "ss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    // Tạo KeyBinding cho thống kê người chơi và bản đồ dungeon
    private static KeyMapping openStatsKey;
    private static KeyMapping openMapKey;

    @SubscribeEvent
    public static void onRegisterKeyBindings (RegisterKeyMappingsEvent event) {
        // Đăng ký phím P để mở thống kê người chơi
        openStatsKey = new KeyMapping("key.ss.open_stats", GLFW.GLFW_KEY_P, "key.categories.ssmod");
        event.register(openStatsKey);

        // Đăng ký phím M để mở bản đồ dungeon
        openMapKey = new KeyMapping("key.ss.open_map", GLFW.GLFW_KEY_M, "key.categories.ssmod");
        event.register(openMapKey);
    }

    @Mod.EventBusSubscriber(modid = "ss", value = Dist.CLIENT)
    public static class KeyInputHandler {

        @SubscribeEvent
        public static void onKeyInputP (InputEvent.Key event) {
            // Kiểm tra nếu người chơi nhấn phím P và không có giao diện nào đang mở
            if (openStatsKey.isDown() && Minecraft.getInstance().screen == null) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    // Mở giao diện thống kê người chơi
                    Minecraft.getInstance().setScreen(new PlayerStatsGui(player));
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInputM (InputEvent.Key event) {
            if (openMapKey.isDown() && Minecraft.getInstance().screen == null) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    // Gửi yêu cầu dữ liệu dungeon từ server
                    SsMod.PACKET_HANDLER.sendToServer(new RequestDungeonDataPacket());
                }
            }
        }
    }
}