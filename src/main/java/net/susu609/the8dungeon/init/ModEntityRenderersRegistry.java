package net.susu609.the8dungeon.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.susu609.the8dungeon.client.model.GateModel;
import net.susu609.the8dungeon.client.model.PedestalModel;
import net.susu609.the8dungeon.client.renderer.entity.GateRenderer;
import net.susu609.the8dungeon.client.renderer.entity.*;

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

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PedestalModel.LAYER_LOCATION, PedestalModel::createBodyLayer);
        event.registerLayerDefinition(GateModel.LAYER_LOCATION, GateModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntitiesRegistry.PEDESTAL.get(), PedestalRenderer::new);
        event.registerEntityRenderer(ModEntitiesRegistry.GATE.get(), GateRenderer::new);
        event.registerEntityRenderer(ModEntitiesRegistry.GRAVE_KNIGHT.get(), GraveKnightRenderer::new);
    }

}