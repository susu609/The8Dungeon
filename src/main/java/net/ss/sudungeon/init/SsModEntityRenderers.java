package net.ss.sudungeon.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SsModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers (EntityRenderersEvent.RegisterRenderers event) {

    }
}

