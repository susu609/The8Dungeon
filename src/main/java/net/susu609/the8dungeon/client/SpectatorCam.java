package net.susu609.the8dungeon.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
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
 * Camera cho người ở chế độ spectator, 3 chế độ (phím V xoay vòng, hoặc /botcam free|top|orbit):
 * - Tự do: vanilla.
 * - Theo dõi trên xuống: camera cao 12 khối, nhìn thẳng xuống bot.
 * - Theo dõi xoay tự do: camera quay quanh bot theo chuột, cách 12 khối.
 * Khi giữa camera và bot có khối che (hang, mái nhà...), camera tiến dần lại còn 6 khối; tới 6 khối
 * mà vẫn bị che thì các khối chắn bị cắt khỏi hình và vẽ lại mờ. Bot đang theo dõi có viền sáng
 * (nhìn xuyên khối). Độ mờ và độ rộng vùng cắt chỉnh bằng /botcam alpha <0-255>, /botcam radius <1-4>.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpectatorCam {

    public enum Mode {
        FREE("tự do"), TOP("trên xuống"), ORBIT("xoay quanh bot");

        final String label;

        Mode(String label) {
            this.label = label;
        }
    }

    public static final KeyMapping KEY = new KeyMapping("key.susu609.camera", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, "key.categories.susu609");

    public static final KeyMapping PREV = new KeyMapping("key.susu609.bot_prev", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET, "key.categories.susu609");
    public static final KeyMapping NEXT = new KeyMapping("key.susu609.bot_next", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.susu609");

    private static final double FAR = 12.0D;   // khoảng cách bình thường
    private static final double NEAR = 6.0D;   // khoảng cách khi bị che; bắt đầu làm mờ ở mức này
    private static final int MAX_HIDDEN = 600;

    /** Chế độ lúc khởi động; -Dsusu609.cam=top|orbit để bật sẵn (tiện khi thử nghiệm). */
    private static Mode mode = switch (System.getProperty("susu609.cam", "")) {
        case "top" -> Mode.TOP;
        case "orbit" -> Mode.ORBIT;
        default -> Mode.FREE;
    };
    private static int ghostAlpha = 35;        // độ đậm khối bị cắt (0 = vô hình, 255 = đặc)
    private static int radius = 2;             // độ rộng vùng cắt quanh đường nhìn
    private static Player target;              // bot đang theo dõi (null nếu không hoạt động)
    private static double dist = FAR;
    private static double prevDist = FAR; // để nội suy khoảng cách theo khung hình
    // Bay mượt tới bot khác (bấm mặt bot trên overlay)
    private static final long FLY_MS = 1000L;
    private static String flyTarget;      // tên bot đang chờ bay tới
    private static Vec3 flyFrom;          // vị trí camera lúc bắt đầu bay
    private static long flyStart;
    private static Vec3 lastCamPos;
    private static int ticks;

    private SpectatorCam() {}

    public static boolean following() {
        return mode != Mode.FREE;
    }

    public static String modeLabel() {
        return mode.label;
    }

    /** Dùng bởi mixin viền sáng: bot đang được theo dõi thì luôn có viền. */
    public static boolean isFollowTarget(Entity e) {
        return target != null && e == target;
    }

    public static void setMode(Mode m) {
        mode = m;
        if (m == Mode.FREE) {
            target = null;
            CutawayState.clear();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("Camera: " + m.label), true);
    }

    @Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Keys {
        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(KEY);
            event.register(PREV);
            event.register(NEXT);
        }
    }

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("botcam")
                .then(Commands.literal("free").executes(c -> { setMode(Mode.FREE); return 1; }))
                .then(Commands.literal("top").executes(c -> { setMode(Mode.TOP); return 1; }))
                .then(Commands.literal("follow").executes(c -> { setMode(Mode.TOP); return 1; }))
                .then(Commands.literal("orbit").executes(c -> { setMode(Mode.ORBIT); return 1; }))
                .then(Commands.literal("alpha").then(Commands.argument("value", IntegerArgumentType.integer(0, 255))
                        .executes(c -> { ghostAlpha = IntegerArgumentType.getInteger(c, "value"); return 1; })))
                .then(Commands.literal("radius").then(Commands.argument("value", IntegerArgumentType.integer(1, 4))
                        .executes(c -> { radius = IntegerArgumentType.getInteger(c, "value"); return 1; }))));
    }

    // ===== Mỗi tick: chọn bot, tính khoảng cách, cập nhật khối bị cắt =====

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        while (KEY.consumeClick()) {
            if (p != null && p.isSpectator()) setMode(Mode.values()[(mode.ordinal() + 1) % Mode.values().length]);
        }
        while (PREV.consumeClick()) switchBot(p, -1);
        while (NEXT.consumeClick()) switchBot(p, 1);
        target = null;
        if (mode == Mode.FREE || p == null || mc.level == null || !p.isSpectator() || mc.getCameraEntity() != p) {
            CutawayState.clear(); // không ở spectator thì giữ lựa chọn nhưng tạm không áp dụng
            return;
        }
        Player bot = findBot(mc.level);
        if (bot == null) {
            CutawayState.clear();
            return;
        }
        target = bot;
        Vec3 eye = bot.getEyePosition();
        Vec3 dir = direction(p);
        boolean covered = mc.level.clip(new ClipContext(eye, eye.add(dir.scale(FAR)),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, bot)).getType() == HitResult.Type.BLOCK;
        prevDist = dist;
        dist += ((covered ? NEAR : FAR) - dist) * 0.25D; // tiến/lùi camera mượt
        place(p, eye.add(dir.scale(dist)));

        if (covered && dist <= NEAR + 0.5D) {
            if (++ticks % 4 == 0) CutawayState.update(occluders(mc.level, bot, eye, dir, dist));
        } else {
            CutawayState.clear();
        }
    }

    /**
     * Vị trí camera cho khung hình này (gọi từ CameraFollowMixin), hoặc null nếu không theo dõi.
     * Dùng vị trí nội suy của bot và hướng nhìn hiện tại của chuột nên không giật theo tick.
     */
    public static Vec3 cameraPosition(float partialTick) {
        Player bot = target;
        LocalPlayer p = Minecraft.getInstance().player;
        if (bot == null || p == null || mode == Mode.FREE) return null;
        double d = Mth.lerp(partialTick, prevDist, dist);
        Vec3 pos = bot.getEyePosition(partialTick).add(direction(p).scale(d));
        if (flyTarget != null && bot.getGameProfile().getName().equals(flyTarget)) {
            long now = net.minecraft.Util.getMillis();
            if (flyStart < 0L) {
                // quá xa (bot ngoài vùng đã tải, server dịch chuyển mình tới) thì khỏi bay
                if (flyFrom == null || flyFrom.distanceTo(pos) > 96.0D) flyTarget = null;
                else flyStart = now;
            }
            if (flyTarget != null) {
                double t = Math.min(1.0D, (now - flyStart) / (double) FLY_MS);
                double e = t * t * (3.0D - 2.0D * t); // smoothstep: chậm - nhanh - chậm
                pos = flyFrom.lerp(pos, e);
                if (t >= 1.0D) flyTarget = null;
            }
        }
        lastCamPos = pos;
        return pos;
    }

    /** Chế độ trên xuống: khóa nhìn thẳng xuống. */
    public static boolean lockPitchDown() {
        return target != null && mode == Mode.TOP;
    }

    /** Bấm mặt bot trên overlay: chọn bot đó và cho camera bay mượt tới (bot quá xa thì dịch chuyển thẳng). */
    public static void flyTo(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isSpectator()) return;
        if (mode == Mode.FREE) setMode(Mode.ORBIT);
        flyTarget = name;
        flyFrom = lastCamPos != null ? lastCamPos : mc.gameRenderer.getMainCamera().getPosition();
        flyStart = -1L; // bắt đầu tính khi bot mới đã thành mục tiêu
        Susu609Mod.PACKET_HANDLER.sendToServer(new net.susu609.the8dungeon.network.WatchBotPacket(name));
    }

    /** Chuyển sang bot trước/sau trong danh sách; server ghi nhớ lựa chọn và dịch chuyển tới nếu bot ở xa. */
    private static void switchBot(LocalPlayer p, int step) {
        if (p == null || !p.isSpectator()) return;
        java.util.List<String> names = BotHud.botNames();
        if (names.isEmpty()) return;
        int idx = names.indexOf(BotHud.botName());
        String next = names.get(Math.floorMod(idx + step, names.size()));
        Susu609Mod.PACKET_HANDLER.sendToServer(new net.susu609.the8dungeon.network.WatchBotPacket(next));
        p.displayClientMessage(Component.literal("Đang xem bot: " + next), true);
    }

    /** Hướng từ bot tới camera: thẳng lên (trên xuống) hoặc ngược hướng nhìn của chuột (xoay tự do). */
    private static Vec3 direction(LocalPlayer p) {
        if (mode == Mode.TOP) return new Vec3(0.0D, 1.0D, 0.0D);
        return Vec3.directionFromRotation(p.getXRot(), p.getYRot()).scale(-1.0D);
    }

    private static void place(LocalPlayer p, Vec3 cam) {
        p.setPos(cam.x, cam.y - p.getEyeHeight(), cam.z);
        p.setDeltaMovement(Vec3.ZERO);
        if (mode == Mode.TOP) p.setXRot(90.0F);
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

    /** Khối nằm quanh đường nhìn từ bot tới camera (ống rộng radius), không tính sàn dưới chân bot. */
    private static LongOpenHashSet occluders(ClientLevel level, Player bot, Vec3 eye, Vec3 dir, double len) {
        LongOpenHashSet set = new LongOpenHashSet();
        int floorY = bot.blockPosition().getY();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (double t = 1.0D; t <= len + 0.5D && set.size() < MAX_HIDDEN; t += 0.5D) {
            Vec3 pt = eye.add(dir.scale(t));
            int cx = Mth.floor(pt.x), cy = Mth.floor(pt.y), cz = Mth.floor(pt.z);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (cy + dy < floorY) continue; // giữ sàn để không thấy hố đen
                        m.set(cx + dx, cy + dy, cz + dz);
                        BlockState s = level.getBlockState(m);
                        if (!s.isAir() && s.getFluidState().isEmpty()) set.add(m.asLong());
                    }
                }
            }
        }
        return set;
    }

    // ===== Góc nhìn: khóa nhìn thẳng xuống ở chế độ trên xuống =====

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (target != null && mode == Mode.TOP) event.setPitch(90.0F);
    }

    // ===== Vẽ lại các khối bị cắt ở dạng mờ =====

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        long[] hidden = CutawayState.positions();
        Minecraft mc = Minecraft.getInstance();
        if (hidden.length == 0 || mc.level == null || ghostAlpha <= 0) return;

        PoseStack ps = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        AlphaVertexConsumer ghost = new AlphaVertexConsumer(buffers.getBuffer(RenderType.translucent()), ghostAlpha);
        MultiBufferSource source = type -> ghost;
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (long l : hidden) {
            m.set(l);
            BlockState s = mc.level.getBlockState(m);
            if (s.getRenderShape() != RenderShape.MODEL) continue;
            ps.pushPose();
            ps.translate(m.getX() - cam.x, m.getY() - cam.y, m.getZ() - cam.z);
            dispatcher.renderSingleBlock(s, ps, source, LevelRenderer.getLightColor(mc.level, m.above()),
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            ps.popPose();
        }
        buffers.endBatch(RenderType.translucent());
    }
}
