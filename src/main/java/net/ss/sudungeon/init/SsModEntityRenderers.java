
package net.ss.sudungeon.init;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.client.model.*;
import net.ss.sudungeon.client.renderer.entity.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SsModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers (EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SsModEntities.TARGET_DUMMY.get(), TargetDummyRenderer::new);
        event.registerEntityRenderer(SsModEntities.MOD_ZOMBIE.get(), ModZombieRenderer::new);
        event.registerEntityRenderer(SsModEntities.ZOMBIE_BOSS.get(), ZombieBossRenderer::new);
        event.registerEntityRenderer(SsModEntities.MOD_PLAYER.get(), ModPlayerRenderer::new);

    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TargetDummyModel.LAYER_LOCATION, TargetDummyModel::createBodyLayer);
        event.registerLayerDefinition(ModZombieModel.LAYER_LOCATION, ModZombieModel::createBodyLayer);
        event.registerLayerDefinition(ZombieBossModel.LAYER_LOCATION, ZombieBossModel::createBodyLayer);
        event.registerLayerDefinition(ModPlayerModel.LAYER_LOCATION, ModPlayerModel::createBodyLayer);

    }
}
