package net.ss.sudungeon.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.IgnoreCanceled;
import net.ss.sudungeon.client.renderer.entity.player.PlayerSkinRenderer;
import net.ss.sudungeon.network.SsModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerRenderProcedure {

    // Render skin tùy chỉnh của người chơi khi không ở góc nhìn thứ nhất
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderPlayerPre (RenderPlayerEvent.Pre event) {
        executeRenderPlayer(event, event.getEntity());
    }

    public static void executeRenderPlayer (@Nullable Event event, Entity entity) {
        // Kiểm tra nếu entity là một Player hoặc ServerPlayer
        if (!(entity instanceof Player || entity instanceof ServerPlayer)) {
            return;
        }

        String skinUrl = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).skinUrl;
        boolean isSlim = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).isSlim;

            RenderPlayerEvent _evt = (RenderPlayerEvent) event;
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher dis = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(dis, mc.getItemRenderer(), mc.getBlockRenderer(), dis.getItemInHandRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
            assert _evt != null;
            Entity _evtEntity = _evt.getEntity();
            PlayerRenderer _pr = null;
            PoseStack poseStack = _evt.getPoseStack();


            if (_evt.getRenderer() instanceof PlayerRenderer && !(_evt.getRenderer() instanceof IgnoreCanceled)) {
                ResourceLocation _texture = new ResourceLocation(skinUrl);
                new PlayerSkinRenderer(context, isSlim, _texture).render((AbstractClientPlayer) _evt.getEntity(), _evt.getEntity().getYRot(), _evt.getPartialTick(), _evt.getPoseStack(), _evt.getMultiBufferSource(),
                        _evt.getPackedLight());
            }

            // Kiểm tra nếu renderer không phải là PlayerRenderer hoặc IgnoreCanceled
            if (_evt.getRenderer() instanceof PlayerRenderer
                    && !(_evt.getRenderer() instanceof IgnoreCanceled)) {

                // Nếu là Pre event thì hủy
                if (_evt instanceof RenderPlayerEvent.Pre _pre) {
                    _pre.setCanceled(true);
                }
            }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderArm (RenderArmEvent event) {
        executeRenderArm(event, event.getPlayer());
    }

    private static void executeRenderArm (@Nullable Event event, Entity entity) {
        // Kiểm tra nếu entity là một Player hoặc ServerPlayer
        if (!(entity instanceof Player || entity instanceof ServerPlayer)) {
            return;
        }

        String skinUrl = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).skinUrl;
        boolean isSlim = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).isSlim;

            RenderArmEvent _evt = (RenderArmEvent) event;
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher dis = mc.getEntityRenderDispatcher();
            assert _evt != null;

            // Setup PlayerRenderer and context
            Entity _evtEntity = _evt.getPlayer();
            PlayerRenderer playerRenderer = (PlayerRenderer) dis.getRenderer(_evt.getPlayer());
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(dis, mc.getItemRenderer(), mc.getBlockRenderer(), dis.getItemInHandRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);

            // Setup model and rendering variables
            MultiBufferSource bufferSource = _evt.getMultiBufferSource();
            int packedLight = _evt.getPackedLight();
            PoseStack poseStack = _evt.getPoseStack();

            // Setup player model
            PlayerModel<AbstractClientPlayer> playerModel = new PlayerModel<>(context.bakeLayer(isSlim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), false);
            playerModel.attackTime = 0.0F;
            playerModel.crouching = false;
            playerModel.swimAmount = 0.0F;
            playerModel.setupAnim(_evt.getPlayer(), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

            // Reset arm rotations to default
            playerModel.leftArm.xRot = 0.0F;
            playerModel.rightArm.xRot = 0.0F;

            // Determine which arm is being rendered
            HumanoidArm arm = _evt.getArm();

            // Render the correct arm with custom skin
            ResourceLocation _texture = new ResourceLocation(skinUrl);  // Use the player's custom skin
            if (arm == HumanoidArm.LEFT) {
                playerModel.leftArm.render(_evt.getPoseStack(), bufferSource.getBuffer(RenderType.entityTranslucentCull(_texture)), packedLight, OverlayTexture.NO_OVERLAY);
            } else {
                playerModel.rightArm.render(_evt.getPoseStack(), bufferSource.getBuffer(RenderType.entityTranslucentCull(_texture)), packedLight, OverlayTexture.NO_OVERLAY);
            }

            // Cancel the default rendering to apply the custom one
            _evt.setCanceled(true);

    }



/*    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderHand (RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson() && mc.player != null) {
            event.setCanceled(true);
        }
    }*/


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderPlayerPost (RenderPlayerEvent.Post event) {
    }
    private static Pair<String, Boolean> getSkinInfo(Entity entity) {
        SsModVariables.PlayerVariables vars = entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new SsModVariables.PlayerVariables());
        return Pair.of(vars.skinUrl, vars.isSlim);
    }

}
