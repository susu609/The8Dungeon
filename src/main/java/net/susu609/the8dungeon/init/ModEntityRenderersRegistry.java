package net.susu609.the8dungeon.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Đăng ký renderer và layer model của entity. Chỉ chạy phía client.
 */
public final class ModEntityRenderersRegistry {

    private ModEntityRenderersRegistry() {
    }

    /**
     * Gọi một lần trong constructor của mod, với mod event bus.
     */
    public static void register(IEventBus bus) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        bus.addListener(ModEntityRenderersRegistry::onRegisterRenderers);
        bus.addListener(ModEntityRenderersRegistry::onRegisterLayers);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }
}