package net.ss.sudungeon.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class onCriticalHitEventHandler {
    @SubscribeEvent
    public static void onCriticalHit (CriticalHitEvent event) {
        if (event != null && event.isCancelable()) {
            event.setCanceled(true);
        } else if (event != null && event.hasResult()) {
            event.setResult(Event.Result.DENY);
        }
    }
}
