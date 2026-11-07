package net.susu609.the8dungeons.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeons.The8DungeonsMod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Công cụ "soi" giao diện cho lúc phát triển, giống Inspect Element của trình duyệt.
 * Bấm TOGGLE_KEY (mặc định F8) ở BẤT KỲ màn hình nào để bật/tắt:
 * - Mỗi widget (nút, ô nhập...) được vẽ một khung viền màu.
 * - Rê chuột vào widget nào thì hiện bảng thông tin: class, vị trí, kích thước, chữ trên nút.
 * <p>
 * Không dùng F3 vì đó là phím debug của vanilla (F3+B, F3+H, F3+T...).
 * Không dùng KeyMapping vì nó chỉ chạy khi đang trong world, không chạy ở title screen.
 */
@Mod.EventBusSubscriber(modid = The8DungeonsMod.MODID, value = Dist.CLIENT)
public final class ScreenInspector {
    /** Phím bật/tắt. Muốn đổi phím thì sửa đúng một chỗ này. */
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_F8;

    // Màu dạng ARGB (0xAARRGGBB)
    private static final int COLOR_ACTIVE = 0xFF55FF55;   // xanh: dùng được
    private static final int COLOR_DISABLED = 0xFFFF5555; // đỏ: bị vô hiệu (active = false)
    private static final int COLOR_HOVER = 0xFFFFFF55;    // vàng: đang rê chuột
    private static final int COLOR_PANEL_BG = 0xE0101010;

    /** Đang bật hay tắt. static nên giữ trạng thái khi chuyển màn hình. */
    private static boolean enabled = false;

    private ScreenInspector() {
    }

    /**
     * Bắt phím. `Pre` = chạy TRƯỚC khi màn hình tự xử lý phím.
     * setCanceled(true) để màn hình không xử lý phím F8 lần nữa.
     */
    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (event.getKeyCode() == TOGGLE_KEY) {
            enabled = !enabled;
            event.setCanceled(true);
        }
    }

    /** `Render.Post` = chạy SAU khi màn hình vẽ xong, nên khung nằm đè lên trên cùng. */
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {
        if (!enabled) return;

        Screen screen = event.getScreen();
        GuiGraphics g = event.getGuiGraphics();
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();

        // Gom widget vào một danh sách phẳng (kể cả widget nằm trong container lồng nhau)
        List<AbstractWidget> widgets = new ArrayList<>();
        collect(screen.children(), widgets);

        // Đẩy trục Z lên cao để khung không bị nút che
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);

        AbstractWidget hovered = null;
        for (AbstractWidget w : widgets) {
            if (!w.visible) continue;
            boolean isHover = w.isMouseOver(mouseX, mouseY);
            if (isHover) hovered = w; // widget cuối cùng trùng chuột = widget nằm trên cùng

            int color = isHover ? COLOR_HOVER : (w.active ? COLOR_ACTIVE : COLOR_DISABLED);
            g.renderOutline(w.getX(), w.getY(), w.getWidth(), w.getHeight(), color);
        }

        drawHeader(g, screen, widgets.size());
        if (hovered != null) {
            drawInfoPanel(g, screen, hovered, mouseX, mouseY);
        }

        g.pose().popPose();
    }

    /** Duyệt cây widget; gặp container (danh sách, tab...) thì đi sâu vào trong. */
    private static void collect(List<? extends GuiEventListener> listeners, List<AbstractWidget> out) {
        for (GuiEventListener l : listeners) {
            if (l instanceof AbstractWidget w) {
                out.add(w);
            }
            if (l instanceof ContainerEventHandler c) {
                collect(c.children(), out);
            }
        }
    }

    /** Dòng tiêu đề góc trên-trái: class của màn hình đang mở và số widget. */
    private static void drawHeader(GuiGraphics g, Screen screen, int count) {
        Font font = Minecraft.getInstance().font;
        String text = "[Inspector] " + screen.getClass().getName() + " | " + count + " widget | F8 để tắt";
        g.fill(0, 0, font.width(text) + 6, 12, COLOR_PANEL_BG);
        g.drawString(font, text, 3, 2, 0xFFFFFFFF, false);
    }

    /** Bảng thông tin cạnh con trỏ cho widget đang rê chuột. */
    private static void drawInfoPanel(GuiGraphics g, Screen screen, AbstractWidget w, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        List<String> lines = new ArrayList<>();
        lines.add("class:  " + w.getClass().getSimpleName());
        lines.add("chữ:    \"" + w.getMessage().getString() + "\"");
        lines.add("vị trí: x=" + w.getX() + ", y=" + w.getY());
        lines.add("cỡ:     " + w.getWidth() + " x " + w.getHeight());
        lines.add("active: " + w.active + " | focused: " + w.isFocused());

        int panelW = 0;
        for (String s : lines) panelW = Math.max(panelW, font.width(s));
        panelW += 8;
        int panelH = lines.size() * 10 + 6;

        // Đặt bảng bên phải-dưới con trỏ; nếu tràn ra ngoài màn hình thì đảo sang bên kia
        int px = mouseX + 12;
        int py = mouseY + 12;
        if (px + panelW > screen.width) px = mouseX - panelW - 6;
        if (py + panelH > screen.height) py = mouseY - panelH - 6;

        g.fill(px, py, px + panelW, py + panelH, COLOR_PANEL_BG);
        g.renderOutline(px, py, panelW, panelH, COLOR_HOVER);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), px + 4, py + 4 + i * 10, 0xFFFFFFFF, false);
        }
    }
}