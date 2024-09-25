/*
package net.mcreator.sfsdfsddsfsdfsdfsdfsdfsd.procedures;

*/
/* imports omitted *//*


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerrenderProcedure {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderArm(RenderLivingEvent event) {
        execute(event, event.getEntity());
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity == null)
            return;
        String skin = "";
        boolean boolean1 = false;
        if (entity instanceof Player || entity instanceof ServerPlayer) {
            RenderLivingEvent _evt = (RenderLivingEvent) event;
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher dis = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(dis, mc.getItemRenderer(), mc.getBlockRenderer(), dis.getItemInHandRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
            Entity _evtEntity = _evt.getEntity();
            PlayerRenderer _pr = null;
            PoseStack poseStack = _evt.getPoseStack();
            if (_evt.getRenderer() instanceof PlayerRenderer && !(_evt.getRenderer() instanceof com.kleiders.kleidersplayerrenderer.KleidersIgnoreCancel)) {
                ResourceLocation _texture = new ResourceLocation("kleiders_custom_renderer:textures/entities/empty.png");
                com.kleiders.kleidersplayerrenderer.KleidersSkinRenderer emptyRenderer = new com.kleiders.kleidersplayerrenderer.KleidersSkinRenderer(context,
                        (_evtEntity instanceof AbstractClientPlayer ? ((AbstractClientPlayer) _evtEntity).getModelName().equals("slim") : false), _texture);
                _pr = emptyRenderer;
                emptyRenderer.clearLayers();
                emptyRenderer.render((AbstractClientPlayer) _evt.getEntity(), _evt.getEntity().getYRot(), _evt.getPartialTick(), _evt.getPoseStack(), _evt.getMultiBufferSource(), _evt.getPackedLight());
            }
            if (_evt.getRenderer() instanceof PlayerRenderer && !(_evt.getRenderer() instanceof com.kleiders.kleidersplayerrenderer.KleidersIgnoreCancel)) {
                ResourceLocation _texture = new ResourceLocation("kleiders_custom_renderer:textures/entities/default.png");
                if (ResourceLocation.tryParse("sfsdfsddsfsdfsdfsdfsdfsd:textures/entities/duong404.png") != null) {
                    _texture = new ResourceLocation("sfsdfsddsfsdfsdfsdfsdfsd:textures/entities/duong404.png");
                }
                new com.kleiders.kleidersplayerrenderer.KleidersSkinRenderer(context, false, _texture).render((AbstractClientPlayer) _evt.getEntity(), _evt.getEntity().getYRot(), _evt.getPartialTick(), _evt.getPoseStack(),
                        _evt.getMultiBufferSource(), _evt.getPackedLight());
            }
            if (_evt.getRenderer() instanceof LivingEntityRenderer && !(_evt.getRenderer() instanceof com.kleiders.kleidersplayerrenderer.KleidersIgnoreCancel)) {
                if (_evt instanceof RenderLivingEvent.Pre _pre) {
                    _pre.setCanceled(true);
                }
            }
        }
    }
}*/
