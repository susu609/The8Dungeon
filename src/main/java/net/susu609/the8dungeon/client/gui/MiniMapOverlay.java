
package net.susu609.the8dungeon.client.gui;

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

@Mod.EventBusSubscriber(
        modid = Susu609Mod.MODID,
        value = Dist.CLIENT
)
public final class MiniMapOverlay {

    // Đường dẫn đến PNG đang có trong repo
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(
                    Susu609Mod.MODID,
                    "textures/map/minimap_icons.png"
            );

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
        if (mc.player == null || mc.level == null
                || mc.options.hideGui) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();

        // Góc trên bên phải
        int x = g.guiWidth() - SIZE - MARGIN;
        int y = MARGIN;

        // Viền đen
        g.fill(x - 2, y - 2,
                x + SIZE + 2, y + SIZE + 2,
                0xDD000000);

        // Nền màu xám
        g.fill(x, y, x + SIZE, y + SIZE,
                0xFF454545);

        // Lưới thử nghiệm, mỗi ô 10 pixel
        for (int i = 0; i < SIZE; i += 10) {
            g.fill(x + i, y, x + i + 1,
                    y + SIZE, 0xFF606060);

            g.fill(x, y + i, x + SIZE,
                    y + i + 1, 0xFF606060);
        }
        DungeonLayout layout = DungeonClientState.layout();
        if (layout != null) {
            for (DungeonLayout.Room room : layout.rooms()) {
                int sx = x + room.cx() * CELL_PX;
                int sy = y + room.cz() * CELL_PX;
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
            int px = x + (mc.player.getBlockX() + 96) * CELL_PX / 16;
            int pz = y + (mc.player.getBlockZ() + 96) * CELL_PX / 16;
            g.blit(ICONS, px - 4, pz - 4, 8, 8, 0, 0, 16, 16, 256, 256);
        }
        // Icon lấy từ PNG 256x256 của dự án
        g.blit(ICONS,
                x + SIZE / 2 - 8,
                y + SIZE / 2 - 8,
                0, 0,
                16, 16,
                256, 256);

        // Tọa độ người chơi
        String pos = "X: " + mc.player.getBlockX()
                + " Z: " + mc.player.getBlockZ();

        g.drawString(mc.font, pos,
                x, y + SIZE + 6,
                0xFFFFFF, true);
    }
}
