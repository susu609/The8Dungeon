package net.ss.sudungeon.client.resources;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerSkinHandler {

    public static void register(IEventBus bus) {
        bus.addListener(EventPriority.NORMAL, PlayerSkinHandler::onPlayerJoin);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        if (!playerLoggedInEvent.getEntity().level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) playerLoggedInEvent.getEntity();
        }// Xử lý sự kiện login của player trên máy chủ
    }
}
