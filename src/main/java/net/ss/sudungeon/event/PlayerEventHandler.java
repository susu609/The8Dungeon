package net.ss.sudungeon.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.model.ModPlayerModel;
import net.ss.sudungeon.network.packet.OpenModeSelectionPacket;
import net.ss.sudungeon.util.EventUtil;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.util.Vars;

import static net.ss.sudungeon.world.dimension.DungeonDimension.DUNGEON_DIMENSION;

@Mod.EventBusSubscriber
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin (PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Kiểm tra nếu người chơi không ở dimension đúng
            if (!player.level().dimension().equals(DUNGEON_DIMENSION)) {
                teleportPlayerToDimension(player);

                player.setRespawnPosition(
                        DUNGEON_DIMENSION,
                        new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ()),
                        player.getYRot(),
                        true,
                        false
                );

                // Hiển thị GUI chỉ nếu đây là lần chơi mới
                openModeSelectionScreen(player);
            }
        }
    }

    /**
     * Dịch chuyển người chơi đến dimension chỉ định.
     */
    private static void teleportPlayerToDimension (ServerPlayer player) {
        // Lấy Server của người chơi
        ServerLevel targetWorld = player.server.getLevel(DUNGEON_DIMENSION);

        if (targetWorld == null) {
            Log.e("Dimension " + DUNGEON_DIMENSION.location() + " không tồn tại!");
            return;
        }

        // Dịch chuyển người chơi sang dimension mục tiêu
        player.teleportTo(
                targetWorld, 0.5, 3, 0.5,
                player.getYRot(), // Giữ góc nhìn hiện tại
                player.getXRot()
        );
        player.setRespawnPosition(DUNGEON_DIMENSION, new BlockPos(0, 3, 0), 0F, true, false);

        Log.i("Dịch chuyển người chơi " + player.getName().getString() + " đến dimension " + DUNGEON_DIMENSION.location());
    }

    /**
     * Hiển thị GUI chọn chế độ chơi.
     */
    static void openModeSelectionScreen (ServerPlayer player) {
        SsMod.PACKET_HANDLER.sendTo(
                new OpenModeSelectionPacket(), // không cần buttonID, x, y, z
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
        Log.i("[PlayerEventHandler] Đã gửi packet mở màn hình ModeSelection tới " + player.getName().getString());

    }

    /*    @SubscribeEvent
        public static void onRenderPlayerPre1 (RenderPlayerEvent.Pre event) {
            Player player = event.getEntity();
            assert Minecraft.getInstance().player != null;
            if (player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
                EventUtil.cancelOrDeny(event);
            }

            if (!player.isLocalPlayer()) return;
            event.setCanceled(true);

            Minecraft mc = Minecraft.getInstance();
            Player realPlayer = mc.player;
            assert realPlayer != null;

            // Tạo model & texture
            ModPlayerModel<AbstractClientPlayer> customModel = new ModPlayerModel<>(
                    mc.getEntityModels().bakeLayer(ModPlayerModel.LAYER_LOCATION));
            ResourceLocation tex = ModPlayerModel.getSkin(realPlayer);
            VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(tex));

            // Setup anim cơ bản
            float partialTicks = event.getPartialTick();
            float limbSwing = player.walkAnimation.position(partialTicks);
            float limbSwingAmount = player.walkAnimation.speed();
            float ageInTicks = player.tickCount + partialTicks;
            float netHeadYaw = Mth.rotLerp(partialTicks, player.yRotO, player.getYRot()) - Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
            float headPitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());

            customModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            // Lấy animation state từ mixin
            PlayerAnimationAccessor accessor = (PlayerAnimationAccessor) player;

            if (accessor.getAttackAnimState().isStarted()) {
                customModel.animate(accessor.getAttackAnimState(), ModPlayerAnimation.PLAYER_SWORD_ATTACK1, ageInTicks);
            } else if (accessor.getHurtAnimState().isStarted()) {
                customModel.animate(accessor.getHurtAnimState(), ModPlayerAnimation.PLAYER_HURT, ageInTicks);
            } else if (accessor.getDrinkAnimState().isStarted()) {
                customModel.animate(accessor.getDrinkAnimState(), ModPlayerAnimation.PLAYER_DRINK, ageInTicks);
            } else if (accessor.getRunAnimState().isStarted()) {
                customModel.animate(accessor.getRunAnimState(), ModPlayerAnimation.PLAYER_RUN, ageInTicks);
            } else if (accessor.getWalkAnimState().isStarted()) {
                customModel.animate(accessor.getWalkAnimState(), ModPlayerAnimation.PLAYER_WALK, ageInTicks);
            } else {
                customModel.animate(accessor.getIdleAnimState(), ModPlayerAnimation.PLAYER_IDLE, ageInTicks);
            }

            // Render
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(0, -1.5, 0);
            customModel.renderToBuffer(poseStack, buffer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            poseStack.popPose();
        }*/
    @SubscribeEvent
    public static void onRenderHand (RenderHandEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            EventUtil.cancelOrDeny(event); // Ẩn player gốc
        }
    }


    /*    @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onRenderPlayerPre (RenderPlayerEvent event) {
            executeRenderPlayer(event, event.getEntity());
        }

        @OnlyIn(Dist.CLIENT)
        public static void executeRenderPlayer(@Nullable Event event, Entity entity) {
            if (!(entity instanceof LocalPlayer player)) return;
            if (!(event instanceof RenderPlayerEvent renderEvent)) return;

            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            Camera camera = mc.gameRenderer.getMainCamera();

            // Tìm hoặc tạo ghost entity đại diện cho player
            ModPlayerEntity ghost = level.getEntitiesOfClass(ModPlayerEntity.class, player.getBoundingBox().inflate(5))
                    .stream().filter(e -> {
                        UUID uuid = e.getRealPlayerUUID();
                        return uuid != null && uuid.equals(player.getUUID());
                    })
                    .findFirst()
                    .orElseGet(() -> {
                        ModPlayerEntity e = new ModPlayerEntity(SsModEntities.MOD_PLAYER.get(), level);
                        e.setRealPlayerUUID(player.getUUID());
                        e.setPos(player.getX(), player.getY(), player.getZ());
                        level.addFreshEntity(e);
                        return e;
                    });

            // Đồng bộ vị trí, hướng nhìn
            ghost.syncWithPlayer(player);
            ((EntityAccessor) ghost).invokeSetRot(camera.getYRot(), camera.getXRot());

            PlayerRenderer renderer = (PlayerRenderer) renderEvent.getRenderer();
            ModPlayerModel<ModPlayerEntity> model =
                    new ModPlayerModel<>(mc.getEntityModels().bakeLayer(ModPlayerModel.LAYER_LOCATION));
            ((LivingEntityRendererAccessor) renderer).setModel(model);
            model.setupAnim(ghost, 0, 0, renderEvent.getPartialTick(), ghost.getYRot(), ghost.getXRot());
            model.setAllVisible(true);

            if (!(renderer instanceof IgnoreCanceled)) {
                EventUtil.cancelOrDeny(renderEvent);
            }

        }*/

    @SubscribeEvent
    public static void onRenderPlayerPre (RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;

        // Huỷ render mặc định
        EventUtil.cancelOrDeny(event);

        // Lấy model và skin
        Minecraft mc = Minecraft.getInstance();
        var vars = Vars.PP(player);
        var skin = new ResourceLocation("ss", "textures/entity/player/" +
                (vars.isSlim ? "slim/" : "wide/") + vars.character + ".png");

        var model = new ModPlayerModel<>(mc.getEntityModels().bakeLayer(ModPlayerModel.LAYER_LOCATION));
        model.setSlim(vars.isSlim);
        model.setupAnimForRenderEvent(player, event.getPartialTick());

        assert mc.player != null;
        long ageInTicks = (long) (mc.player.tickCount + event.getPartialTick());

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(skin));
        poseStack.pushPose();

        // Xoay về hướng của player (sửa lỗi bị xoay ngược)
        float yaw = Mth.rotLerp(event.getPartialTick(), player.yRotO, player.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

        // Render model
        model.renderToBuffer(poseStack, consumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        poseStack.popPose();
    }


}