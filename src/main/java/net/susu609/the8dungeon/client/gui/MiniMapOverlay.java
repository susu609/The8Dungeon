package net.susu609.the8dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.dungeon.DungeonClientState;
import net.susu609.the8dungeon.dungeon.DungeonLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * Minimap dungeon vẽ trên HUD (chỉ chạy ở client).
 * <p>
 * Luồng mỗi khung hình:
 * 1. Tính vị trí + tỉ lệ (thu nhỏ ở góc, giữ Tab thì phóng to giữa màn hình).
 * 2. Dời + phóng "tờ giấy" (pose) một lần → mọi lệnh vẽ sau đó dùng toạ độ gốc (0,0).
 * 3. Vẽ nền → lưới → phòng (chế độ khám phá) → mũi tên người chơi.
 * 4. Trả tờ giấy và màu về như cũ, rồi vẽ chữ toạ độ (không bị phóng).
 * <p>
 * Dữ liệu: layout dựng lại từ seed, danh sách phòng đã khám phá do SERVER gửi xuống
 * (DungeonClientState.visited). Client không tự quyết phòng nào đã đi qua.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, value = Dist.CLIENT)
public final class MiniMapOverlay {

    // ===== Ảnh atlas 256x256 =====
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "textures/map/minimap_icons.png");
    private static final int TEX = 256;    // cỡ cả ảnh, blit cần để đổi pixel → UV 0..1

    // Bản đồ UV (pixel trong ảnh). Ảnh gốc mỗi ô 16 px.
    private static final int V_PLAYER = 0;           // hàng 0: mũi tên người chơi (u = 0)
    private static final int V_ICON = 16;            // hàng 1: icon vai trò phòng
    private static final int V_ROOM = 32;            // hàng 2-3: ô phòng
    private static final int U_ROOM1_LIT = 0, U_ROOM1_DARK = 16;   // 1x1   (16x16)
    private static final int U_ROOM2_LIT = 32, U_ROOM2_DARK = 48;   // 1x2 dọc (16x32)

    // ===== Kích thước trên màn hình =====
    private static final int GRID = 13;                   // dungeon 13x13 ô
    private static final int CELL_PX = 8;                 // 1 ô dungeon = 8 px (ảnh 16 px thu nhỏ 1/2)
    private static final int SIZE = GRID * CELL_PX;       // 104 px
    private static final int MARGIN = 8;
    private static final int ORIGIN_OFFSET = 96;          // fixedOrigin = -96 → cộng 96 để góc dungeon thành 0
    private static final int VIEW = 5;                     // 5x5 ô
    private static final int SMALL_SCALE = 2;              // phóng 2 lần cho dễ nhìn
    private static final int BOX = VIEW * CELL_PX * SMALL_SCALE;   // 80 px

    private MiniMapOverlay() {
    }

    private static float mapX(Player p) {
        return (float) ((p.getX() + ORIGIN_OFFSET) * CELL_PX / 16F);
    }

    private static float mapZ(Player p) {
        return (float) ((p.getZ() + ORIGIN_OFFSET) * CELL_PX / 16F);
    }

    // Forge gọi mỗi khung hình, sau khi HUD vanilla đã vẽ xong.
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;   // chưa vào world / F1
        GuiGraphics g = event.getGuiGraphics();

        // ---- 1. Vị trí + tỉ lệ ----
        boolean big = mc.options.keyPlayerList.isDown();
        int box = big ? SIZE : BOX;                       // cỡ khung trên màn hình
        int x = g.guiWidth() - box - MARGIN;              // luôn ở góc phải trên
        int y = MARGIN;

        g.fill(x - 2, y - 2, x + box + 2, y + box + 2, 0xDD000000);   // viền vẽ ngoài pose
        // ---- 2. Dời + phóng tờ giấy, đặt độ mờ ----
        g.pose().pushPose();
        if (big) {
            g.pose().translate(x, y, 0);                  // cả bản đồ, góc (0,0) ở góc khung
        } else {
            g.enableScissor(x, y, x + box, y + box);      // cắt mọi thứ ngoài khung
            g.pose().translate(x + box / 2F, y + box / 2F, 0);    // ① gốc ở GIỮA khung
            g.pose().scale(SMALL_SCALE, SMALL_SCALE, 1F);         // ② phóng ×2
            g.pose().translate(-mapX(mc.player), -mapZ(mc.player), 0);   // ③ kéo người chơi về gốc
        }
        RenderSystem.enableBlend();
        g.setColor(1F, 1F, 1F, big ? 1F : 0.5F);
        // ---- 3. Vẽ (toạ độ tính từ 0,0, không cộng x/y nữa) ----
        drawBackground(g);
        DungeonLayout layout = DungeonClientState.layout();
        if (layout != null) {
            drawRooms(g, layout);
            drawPlayer(g, mc.player);              // vẽ sau cùng → nằm trên phòng
        }

        // ---- 4. Trả lại như cũ ----
        g.setColor(1F, 1F, 1F, 1F);                // quên dòng này → hotbar cũng mờ
        g.pose().popPose();                        // bỏ translate/scale
        if (!big) g.disableScissor();                     // ◄ mới: phải tắt, nếu không cả HUD bị cắt
        // Chữ vẽ ngoài pose nên không bị phóng; đặt ngay dưới mép bản đồ thật (SIZE * scale).
        String pos = "X: " + mc.player.getBlockX() + " Z: " + mc.player.getBlockZ();
        g.drawString(mc.font, pos, x, y + box + 6, 0xFFFFFF, true);
    }

    // Viền đen, nền xám, lưới mỗi CELL_PX một vạch (khớp mép ô phòng).
    private static void drawBackground(GuiGraphics g) {
        g.fill(-2, -2, SIZE + 2, SIZE + 2, 0xDD000000);
        g.fill(0, 0, SIZE, SIZE, 0xFF454545);
        for (int i = 0; i < SIZE; i += CELL_PX) {
            g.fill(i, 0, i + 1, SIZE, 0xFF606060);    // vạch dọc
            g.fill(0, i, SIZE, i + 1, 0xFF606060);    // vạch ngang
        }
    }

    /**
     * Chế độ khám phá:
     * đã khám phá (server gửi)        → ô SÁNG
     * nối cửa với phòng đã khám phá   → ô TỐI + icon
     * còn lại                          → không vẽ
     */
    private static void drawRooms(GuiGraphics g, DungeonLayout layout) {
        Set<Integer> seen = DungeonClientState.visited();

        // Gom các phòng "kề": đi theo lối ra của mọi phòng đã khám phá.
        Set<Integer> near = new HashSet<>();
        for (DungeonLayout.Room r : layout.rooms()) {
            if (seen.contains(r.id())) {
                for (DungeonLayout.Exit e : layout.exitsOf(r)) near.add(e.toRoom());
            }
        }

        for (DungeonLayout.Room room : layout.rooms()) {
            boolean seenRoom = seen.contains(room.id());
            if (!seenRoom && !near.contains(room.id())) continue;      // vô hình

            int sx = room.cx() * CELL_PX;                               // ô lưới → pixel
            int sy = room.cz() * CELL_PX;
            int w = room.cellsX(), h = room.cellsZ();
            int u1 = seenRoom ? U_ROOM1_LIT : U_ROOM1_DARK;
            int u2 = seenRoom ? U_ROOM2_LIT : U_ROOM2_DARK;

            // blit 11 tham số: (ảnh, x, y, cỡ VẼ w, h, u, v, cỡ CẮT w, h, cỡ ảnh w, h)
            if (w == 1 && h == 1) {
                g.blit(ICONS, sx, sy, 8, 8, u1, V_ROOM, 16, 16, TEX, TEX);
            } else if (w == 1 && h == 2) {
                g.blit(ICONS, sx, sy, 8, 16, u2, V_ROOM, 16, 32, TEX, TEX);
            } else if (w == 2 && h == 1) {
                // Không có ảnh ngang → xoay ảnh dọc 90°.
                // Xoay quanh (0,0): (x, y) → (-y, x), ảnh lòi sang trái 16 px → dời gốc sang phải 16.
                g.pose().pushPose();
                g.pose().translate(sx + 16, sy, 0);
                g.pose().mulPose(Axis.ZP.rotationDegrees(90));
                g.blit(ICONS, 0, 0, 8, 16, u2, V_ROOM, 16, 32, TEX, TEX);
                g.pose().popPose();
            }

            int iconU = iconU(layout.roleOf(room));
            if (iconU >= 0) {
                g.blit(ICONS, sx, sy, 8, 8, iconU, V_ICON, 16, 16, TEX, TEX);
            }
        }
    }

    // Vai trò phòng → cột icon ở hàng V_ICON; -1 = không có icon.
    private static int iconU(DungeonLayout.Role role) {
        return switch (role) {
            case SHOP -> 0;
            case TREASURE -> 16;
            case BOSS -> 32;
            case LIBRARY -> 48;
            case MYSTERY -> 64;
            case ELITE -> 80;
            default -> -1;    // START, NORMAL
        };
    }

    /**
     * Mũi tên người chơi, xoay theo hướng nhìn.
     * yaw Minecraft: 0 = nam, 90 = tây, 180 = bắc, 270 = đông.
     * Ảnh mũi tên chỉ lên (bắc) → xoay (yaw + 180) độ.
     */
    private static void drawPlayer(GuiGraphics g, Player player) {
        // getX() số thực → mũi tên trượt mượt; block → ô (÷16) → pixel (×CELL_PX)
        float px = (float) ((player.getX() + ORIGIN_OFFSET) * CELL_PX / 16F);
        float pz = (float) ((player.getZ() + ORIGIN_OFFSET) * CELL_PX / 16F);
        g.pose().pushPose();
        g.pose().translate(px, pz, 0);                                         // tâm xoay = người chơi
        g.pose().mulPose(Axis.ZP.rotationDegrees(player.getYRot() + 180F));
        g.blit(ICONS, -4, -4, 8, 8, 0, V_PLAYER, 16, 16, TEX, TEX);            // lệch -4: tâm ảnh trùng tâm xoay
        g.pose().popPose();
    }
}
