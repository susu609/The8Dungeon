package net.ss.sudungeon.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.gamemodeselection.MainMenuScreen;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonEventHandler {
    @SubscribeEvent
    public static void onServerStarting (ServerStartingEvent event) {
        // Kiểm tra khi máy chủ bắt đầu
        ServerLevel serverLevel = event.getServer().getLevel(ServerLevel.OVERWORLD);
        assert serverLevel != null;
        DungeonSavedData dungeonData = DungeonSavedData.get(serverLevel);

        if (dungeonData.getRooms().isEmpty()) {
            // Không có dungeon, thực hiện hành động tùy chỉnh (ví dụ: hiện MainMenuScreen)
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().screen == null) {
                    Minecraft.getInstance().setScreen(new MainMenuScreen());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onClientSetup (FMLClientSetupEvent event) {
        // Đăng ký sự kiện máy khách
        // (Nơi bạn có thể đăng ký các màn hình của mình)
    }

    @SubscribeEvent
    public static void onCommonSetup (FMLCommonSetupEvent event) {
        // Đăng ký sự kiện chung
    }
}
