package net.ss.sudungeon.event;

import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.util.EventUtil;

@Mod.EventBusSubscriber
public class CriticalEventHandler {

    @SubscribeEvent
    public static void onCriticalHit (CriticalHitEvent event) {
        EventUtil.cancelOrDeny(event);
    }

}
