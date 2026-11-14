package net.susu609.the8dungeon.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.susu609.the8dungeon.client.model.ClawdModel;
import net.susu609.the8dungeon.client.renderer.entity.ClawdRenderer;

/**
 * Đăng ký renderer và layer model của entity. Chỉ chạy phía client.
 */
public final class ModEntityRenderersRegistry {

    private ModEntityRenderersRegistry() {
    }

    /**
     * Gọi một lần trong constructor của mod, với mod event bus.
     */
    public static void register(IEventBus modBus) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        modBus.addListener(ModEntityRenderersRegistry::onRegisterRenderers);
        modBus.addListener(ModEntityRenderersRegistry::onRegisterLayers);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntitiesRegistry.CLAWD_AGENT.get(), ClawdRenderer::new);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ClawdModel.LAYER_LOCATION, ClawdModel::createBodyLayer);
    }
}