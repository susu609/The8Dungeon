package net.susu609.the8dungeon.dungeon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;


import java.util.ArrayList;
import java.util.List;

/**
 * Inspector phía client: khi người chơi đứng trong một phòng của dungeon thì
 * 1) vẽ viền vàng quanh khoảng rỗng của phòng đó (chỉ một phòng),
 * 2) hiện thông tin phòng ở góc màn hình, kiểu F3 (khi F3 mở thì chuyển xuống góc dưới trái).
 * Chữ dùng tiếng Anh/ASCII vì font mặc định của Minecraft không vẽ chắc chắn dấu tiếng Việt.
 */
public final class DungeonInspector {

    private static final int CELL = RoomPreset.CELL;

    private DungeonInspector() {
    }

    // ---------- Tìm phòng người chơi đang đứng ----------

    private static DungeonLayout.Room currentRoom(DungeonLayout layout, Player player) {
        BlockPos origin = DungeonBuilder.fixedOrigin(layout.params());
        double y = player.getY();
        if (y < origin.getY() || y >= origin.getY() + DungeonBuilder.totalHeight()) return null;
        int cx = Math.floorDiv(Mth.floor(player.getX()) - origin.getX(), CELL);
        int cz = Math.floorDiv(Mth.floor(player.getZ()) - origin.getZ(), CELL);
        return layout.roomAt(cx, cz);
    }

    // ---------- Thông tin ở góc màn hình ----------

    private static List<String> infoLines(DungeonLayout layout, DungeonLayout.Room room) {
        BlockPos o = DungeonBuilder.fixedOrigin(layout.params());
        int x0 = o.getX() + room.cx() * CELL;
        int z0 = o.getZ() + room.cz() * CELL;
        int x1 = x0 + room.cellsX() * CELL - 1;
        int z1 = z0 + room.cellsZ() * CELL - 1;

        List<String> lines = new ArrayList<>();
        lines.add("Room #" + room.id() + "  [" + layout.roleOf(room) + "]");
        lines.add("Preset: " + room.preset() + (room.rotated() ? " (rotated)" : "")
                + "  " + room.cellsX() * CELL + "x" + room.cellsZ() * CELL);
        lines.add("Cell: " + room.cx() + ", " + room.cz()
                + "  Chunk: " + Math.floorDiv(x0, CELL) + ", " + Math.floorDiv(z0, CELL));
        lines.add("Blocks: x " + x0 + ".." + x1 + "  z " + z0 + ".." + z1);

        StringBuilder exits = new StringBuilder();
        for (DungeonLayout.Exit e : layout.exitsOf(room)) {
            if (exits.length() > 0) exits.append(", ");
            exits.append(e.side()).append(">#").append(e.toRoom())
                    .append(' ').append(layout.roleOf(layout.rooms().get(e.toRoom())));
        }
        lines.add("Doors: " + layout.degree(room.id()) + "  " + exits);
        lines.add("From start: " + layout.distanceFromStart(room) + " doors"
                + (room.id() != 0 && layout.degree(room.id()) == 1 ? "  (dead end)" : ""));
        return lines;
    }

    private static void renderInfo(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (mc.level.dimension() != Level.OVERWORLD) return;
        DungeonLayout layout = DungeonClientState.layout();
        if (layout == null) return;
        DungeonLayout.Room room = currentRoom(layout, mc.player);
        if (room == null) return;

        List<String> lines = infoLines(layout, room);
        int lineHeight = 10;
        int y = mc.options.renderDebug
                ? mc.getWindow().getGuiScaledHeight() - lines.size() * lineHeight - 2
                : 2;
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            int width = mc.font.width(text);
            g.fill(1, y - 1, 2 + width + 1, y + 9, 0x90505050);          // nền xám trong suốt như F3
            g.drawString(mc.font, text, 2, y, i == 0 ? 0xFFD700 : 0xE0E0E0, false);   // dòng đầu màu vàng
            y += lineHeight;
        }
    }

    // ---------- Viền vàng quanh phòng ----------

    private static void renderOutline(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.level.dimension() != Level.OVERWORLD) return;
        DungeonLayout layout = DungeonClientState.layout();
        if (layout == null) return;
        DungeonLayout.Room room = currentRoom(layout, mc.player);
        if (room == null) return;

        // Khoảng rỗng bên trong phòng (bỏ lớp tường dày 1), co vào 0.02 để viền không bị z-fight với mặt tường.
        BlockPos o = DungeonBuilder.fixedOrigin(layout.params());
        double e = 0.02;
        AABB box = new AABB(
                o.getX() + room.cx() * CELL + 1 + e,
                o.getY() + 1 + e,
                o.getZ() + room.cz() * CELL + 1 + e,
                o.getX() + (room.cx() + room.cellsX()) * CELL - 1 - e,
                o.getY() + 1 + RoomPreset.INTERIOR_HEIGHT - e,
                o.getZ() + (room.cz() + room.cellsZ()) * CELL - 1 - e);

        // Tọa độ vẽ tính so với camera nên phải dịch ngược vị trí camera.
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        LevelRenderer.renderLineBox(pose, lines, box, 1.0f, 0.84f, 0.0f, 1.0f);   // vàng
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    // ---------- Đăng ký sự kiện (chỉ chạy ở client) ----------

    @Mod.EventBusSubscriber(modid = Susu609Mod.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("dungeon_inspector", (gui, g, partialTick, w, h) -> renderInfo(g));
        }
    }

    @Mod.EventBusSubscriber(modid = Susu609Mod.MODID, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                renderOutline(event);
            }
        }
    }
}
