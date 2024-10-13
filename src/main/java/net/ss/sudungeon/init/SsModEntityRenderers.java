package net.ss.sudungeon.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.client.model.ModelModZombie;
import net.ss.sudungeon.client.model.ModelTargetDummy;
import net.ss.sudungeon.client.renderer.entity.*;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SsModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SsModEntities.COLOSSAL_ZOMBIE.get(), ColossalZombieRenderer::new);
        event.registerEntityRenderer(SsModEntities.TARGET_DUMMY.get(), TargetDummyRenderer::new);
        event.registerEntityRenderer(SsModEntities.MOD_ZOMBIE.get(), ModZombieRenderer::new);
    }
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelTargetDummy.LAYER_LOCATION, ModelTargetDummy::createBodyLayer);
        event.registerLayerDefinition(ModelModZombie.LAYER_LOCATION, ModelModZombie::createBodyLayer);
    }

}