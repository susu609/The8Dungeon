package net.susu609.the8dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.dungeon.DungeonClientState;
import net.susu609.the8dungeon.dungeon.DungeonLayout;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, value = Dist.CLIENT)
public final class MiniMapOverlay {

    // Đường dẫn đến PNG đang có trong repo
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "textures/map/minimap_icons.png");

    // Kích thước và khoảng cách của minimap
    private static final int GRID = 13;
    private static final int CELL_PX = 8;                 // 1 ô dungeon = 8 px trên màn hình
    private static final int SIZE = GRID * CELL_PX;       // 104, thay cho 100
    private static final int MARGIN = 8;

    private MiniMapOverlay() {
    }

    // Forge gọi sau khi HUD được render
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Chỉ vẽ khi người chơi đã vào thế giới
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();

        // Góc trên bên phải
        boolean big = mc.options.keyPlayerList.isDown();      // phím Tab
        float scale = big ? (g.guiHeight() * 0.8F) / SIZE : 1F;
        int x = big ? (int) ((g.guiWidth() - SIZE * scale) / 2) : g.guiWidth() - SIZE - MARGIN;
        int y = big ? (int) ((g.guiHeight() - SIZE * scale) / 2) : MARGIN;

        g.pose().pushPose();
        RenderSystem.enableBlend();
        g.setColor(1F, 1F, 1F, big ? 1F : 0.5F);

        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1F);    // ◄ thêm ngay sau translate ở bước ①
        // Viền đen
        g.fill(x - 2, y - 2, x + SIZE + 2, y + SIZE + 2, 0xDD000000);

        // Nền màu xám
        g.fill(x, y, x + SIZE, y + SIZE, 0xFF454545);

        // Lưới thử nghiệm, mỗi ô 10 pixel
        for (int i = 0; i < SIZE; i += CELL_PX) {
            g.fill(i, 0, i + 1, SIZE, 0xFF606060);
            g.fill(0, i, SIZE, i + 1, 0xFF606060);
        }
        DungeonLayout layout = DungeonClientState.layout();
        if (layout != null) {
            Set<Integer> seen = DungeonClientState.visited();
            Set<Integer> near = new HashSet<>();
            for (DungeonLayout.Room r : layout.rooms()) {
                if (seen.contains(r.id())) {
                    for (DungeonLayout.Exit e : layout.exitsOf(r)) near.add(e.toRoom());
                }
            }

            g.fill(-2, -2, SIZE + 2, SIZE + 2, 0xDD000000);
            g.fill(0, 0, SIZE, SIZE, 0xFF454545);

            for (DungeonLayout.Room room : layout.rooms()) {
                boolean seenRoom = seen.contains(room.id());
                if (!seenRoom && !near.contains(room.id())) continue;   // vô hình
                int u1 = seenRoom ? 0 : 16;     // phòng 1x1
                int u2 = seenRoom ? 32 : 48;    // phòng 1x2 (dọc và ngang)
                int sx = room.cx() * CELL_PX;
                int sy = room.cz() * CELL_PX;
                int w = room.cellsX(), h = room.cellsZ();
                if (w == 1 && h == 1) {
                    g.blit(ICONS, sx, sy, 8, 8, 0, 32, 16, 16, 256, 256);
                } else if (w == 1 && h == 2) {
                    g.blit(ICONS, sx, sy, 8, 16, 32, 32, 16, 32, 256, 256);
                } else if (w == 2 && h == 1) {
                    g.pose().pushPose();
                    g.pose().translate(sx + 16, sy, 0);              // ① dời gốc sang mép phải
                    g.pose().mulPose(Axis.ZP.rotationDegrees(90));   // ② xoay giấy 90°
                    g.blit(ICONS, 0, 0, 8, 16, 32, 32, 16, 32, 256, 256);   // ③ vẽ ảnh dọc tại gốc (0,0)
                    g.pose().popPose();                              // ④ trả giấy về như cũ
                }
                int iconU = switch (layout.roleOf(room)) {
                    case SHOP -> 0;
                    case TREASURE -> 16;
                    case BOSS -> 32;
                    case LIBRARY -> 48;
                    case MYSTERY -> 64;
                    case ELITE -> 80;
                    default -> -1;    // START, NORMAL: không có icon
                };
                if (iconU >= 0) {
                    g.blit(ICONS, sx, sy, 8, 8, iconU, 16, 16, 16, 256, 256);
                }
            }
            float px = (float) ((mc.player.getX() + 96) * CELL_PX / 16F);
            float pz = (float) ((mc.player.getZ() + 96) * CELL_PX / 16F);
            g.pose().pushPose();
            g.pose().translate(px, pz, 0);                                  // tâm xoay = vị trí người chơi
            g.pose().mulPose(Axis.ZP.rotationDegrees(mc.player.getYRot() + 180F));
            g.blit(ICONS, -4, -4, 8, 8, 0, 0, 16, 16, 256, 256);           // vẽ lệch -4 để tâm ảnh trùng tâm xoay
            g.pose().popPose();
        }

        // Tọa độ người chơi
        String pos = "X: " + mc.player.getBlockX() + " Z: " + mc.player.getBlockZ();
        g.setColor(1F, 1F, 1F, 1F);          // ◄ trước popPose(), trả lại màu
        g.pose().popPose();
        g.drawString(mc.font, pos, x, y + SIZE + 6, 0xFFFFFF, true);

    }
}
