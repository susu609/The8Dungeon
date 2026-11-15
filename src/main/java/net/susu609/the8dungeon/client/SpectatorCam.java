package net.susu609.the8dungeon.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.client.gui.BotHud;
import org.lwjgl.glfw.GLFW;

/**
 * Camera cho người ở chế độ spectator: Tự do (vanilla) hoặc Theo dõi bot nhìn từ trên xuống.
 * Khi theo dõi mà bot đi vào hang/nhà (có khối che phía trên), camera hạ dần từ 12 xuống 6 khối;
 * tới 6 khối mà vẫn bị che thì các khối chắn giữa camera và bot bị cắt khỏi hình và vẽ lại mờ.
 * Đổi chế độ: phím V (đổi được trong Controls) hoặc lệnh /botcam follow|free.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpectatorCam {

    public static final KeyMapping KEY = new KeyMapping("key.susu609.camera", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, "key.categories.susu609");

    private static final double FAR = 12.0D;   // độ cao camera bình thường
    private static final double NEAR = 6.0D;   // độ cao khi bị che, bắt đầu làm mờ ở mức này
    private static final int RADIUS = 2;       // cắt khối trong cột 5x5 quanh bot
    private static final int GHOST_ALPHA = 70; // độ mờ của khối bị cắt (0..255)

    private static boolean follow = Boolean.getBoolean("susu609.followCam");
    private static boolean active;             // tick này có đang đặt camera theo bot không
    private static double height = FAR;
    private static int ticks;

    private SpectatorCam() {}

    public static boolean following() {
        return follow;
    }

    public static void setFollow(boolean on) {
        follow = on;
        if (!on) {
            active = false;
            CutawayState.clear();
        }
    }

    @Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Keys {
        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(KEY);
        }
    }

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("botcam")
                .then(Commands.literal("follow").executes(c -> {
                    setFollow(true);
                    return 1;
                }))
                .then(Commands.literal("free").executes(c -> {
                    setFollow(false);
                    return 1;
                })));
    }

    // ===== Mỗi tick: đặt camera và cập nhật khối bị cắt =====

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        while (KEY.consumeClick()) {
            if (p != null && p.isSpectator()) {
                setFollow(!follow);
                p.displayClientMessage(Component.literal(follow
                        ? "Camera: theo dõi bot (nhìn từ trên xuống)" : "Camera: tự do"), true);
            }
        }
        active = false;
        if (!follow || p == null || mc.level == null) {
            CutawayState.clear();
            return;
        }
        if (!p.isSpectator()) { // giữ lựa chọn, chỉ tạm không áp dụng khi không ở spectator
            CutawayState.clear();
            return;
        }
        Player bot = findBot(mc.level);
        if (mc.getCameraEntity() != p || bot == null) { // đang nhập vào góc nhìn bot của vanilla, hoặc chưa thấy bot
            CutawayState.clear();
            return;
        }
        active = true;
        Vec3 eye = bot.getEyePosition();
        boolean covered = covered(mc.level, bot);
        double target = covered ? NEAR : FAR;
        height += (target - height) * 0.25D; // hạ/nâng camera mượt
        p.setPos(bot.getX(), eye.y + height, bot.getZ());
        p.setDeltaMovement(Vec3.ZERO);
        p.setXRot(90.0F);

        if (covered && height <= NEAR + 0.5D) {
            if (++ticks % 4 == 0) CutawayState.update(occluders(mc.level, bot, eye.y + height));
        } else {
            CutawayState.clear();
        }
    }

    /** Bot đang xem trên HUD (bot mình đang nhìn qua camera hoặc bot gần nhất). */
    private static Player findBot(ClientLevel level) {
        String name = BotHud.botName();
        if (name == null) return null;
        for (AbstractClientPlayer pl : level.players()) {
            if (pl.getGameProfile().getName().equals(name)) return pl;
        }
        return null;
    }

    /** Có khối che ngay trên đầu bot trong khoảng FAR không (hang, mái nhà, tán cây...). */
    private static boolean covered(ClientLevel level, Player bot) {
        BlockPos base = bot.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy <= (int) FAR; dy++) {
            m.set(base.getX(), base.getY() + dy, base.getZ());
            BlockState s = level.getBlockState(m);
            if (!s.isAir() && s.getFluidState().isEmpty()) return true;
        }
        return false;
    }

    /** Các khối nằm giữa camera và bot trong cột (2*RADIUS+1)^2, từ trên đầu bot tới camera. */
    private static LongOpenHashSet occluders(ClientLevel level, Player bot, double camY) {
        LongOpenHashSet set = new LongOpenHashSet();
        BlockPos base = bot.blockPosition();
        int top = Mth.floor(camY);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int y = base.getY() + 2; y <= top; y++) {
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    m.set(base.getX() + dx, y, base.getZ() + dz);
                    BlockState s = level.getBlockState(m);
                    if (!s.isAir() && s.getFluidState().isEmpty()) set.add(m.asLong());
                }
            }
        }
        return set;
    }

    // ===== Góc nhìn: khóa nhìn thẳng xuống khi đang theo dõi =====

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (active) event.setPitch(90.0F);
    }

    // ===== Vẽ lại các khối bị cắt ở dạng mờ =====

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        long[] hidden = CutawayState.positions();
        Minecraft mc = Minecraft.getInstance();
        if (hidden.length == 0 || mc.level == null) return;

        PoseStack ps = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        AlphaVertexConsumer ghost = new AlphaVertexConsumer(buffers.getBuffer(RenderType.translucent()), GHOST_ALPHA);
        MultiBufferSource source = type -> ghost;
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (long l : hidden) {
            m.set(l);
            BlockState s = mc.level.getBlockState(m);
            if (s.getRenderShape() != RenderShape.MODEL) continue;
            ps.pushPose();
            ps.translate(m.getX() - cam.x, m.getY() - cam.y, m.getZ() - cam.z);
            // ánh sáng lấy từ ô phía trên (ô của khối đặc luôn tối)
            dispatcher.renderSingleBlock(s, ps, source, LevelRenderer.getLightColor(mc.level, m.above()),
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            ps.popPose();
        }
        buffers.endBatch(RenderType.translucent());
    }
}
