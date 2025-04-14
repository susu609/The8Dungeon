package net.ss.sudungeon.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ServerEventHandler {

/*    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // Xử lý logic từng thế giới
            event.getServer().getAllLevels().forEach(ServerEventHandler::processWorld);
        }
    }

    private static void processWorld(ServerLevel serverLevel) {
        String currentMode = SsModVariables.WorldVariables.get(serverLevel)
                .getVariable("currentGameMode", String.class);

        if (currentMode == null || currentMode.isEmpty()) {
            for (ServerPlayer player : serverLevel.players()) {
                PlayerEventHandler.openModeSelectionScreen(player);
            }
        }

    }*/

}