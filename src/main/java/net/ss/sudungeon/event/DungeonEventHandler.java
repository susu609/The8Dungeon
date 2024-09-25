package net.ss.sudungeon.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonEventHandler {
    @SubscribeEvent
    public static void onPlayerLogin (PlayerEvent.PlayerLoggedInEvent event) {
        // Get the Overworld ServerLevel from the server
        if (event.getEntity().getServer() == null) {
            SsMod.LOGGER.error("Server is not initialized.");
            return;
        }
        ServerLevel serverLevel = event.getEntity().getServer().getLevel(ServerLevel.OVERWORLD);


        // Retrieve dungeon data associated with the Overworld
        assert serverLevel != null;
        DungeonSavedData dungeonData = DungeonSavedData.get(serverLevel);

        // Check if there are any rooms in the dungeon
        if (dungeonData.getRooms().isEmpty()) {
            if (event.getEntity() instanceof ServerPlayer) {
                handleNoRoomsCondition((ServerPlayer) event.getEntity());
            } else {
                SsMod.LOGGER.warn("Entity is not a ServerPlayer.");
            }

        }

    }

    private static void handleNoRoomsCondition (ServerPlayer player) {
        // Placeholder for any specific logic to handle the no rooms condition
        // For instance, you could send a packet to the player to open a GUI, display a message, etc.
//        SsMod.PACKET_HANDLER.sendTo(new ShowMainMenuScreenPacket(), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
//        SsMod.LOGGER.info("Sent MainMenuScreen packet to " + player.getName().getString());
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
