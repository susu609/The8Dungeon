
package net.susu609.the8dungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

@Mod.EventBusSubscriber(
        modid = Susu609Mod.MODID,
        value = Dist.CLIENT
)
public final class MiniMapHud {

    // Đường dẫn đến PNG đang có trong repo
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(
                    Susu609Mod.MODID,
                    "textures/map/minimap_icons.png"
            );

    // Kích thước và khoảng cách của minimap
    private static final int SIZE = 100;
    private static final int MARGIN = 8;

    private MiniMapHud() {}

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
