package net.susu609.the8dungeon.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.ai.BotInfo;
import net.susu609.the8dungeon.client.SpectatorCam;

/**
 * Overlay cho người xem (góc phải trên):
 * - Bảng bot đang theo dõi: việc đang làm, máu, đói, gỗ/đá/cuốc, chế độ camera.
 * - Hàng mặt mọi bot (kèm thanh máu); giữ Alt để có con trỏ: trỏ vào xem chỉ số, bấm để camera bay tới.
 * - Nút "Bỏ qua đêm" khi mọi bot đã ngủ (hiện cả cho người ở Sáng tạo).
 * Tự ẩn nếu 3 giây không có dữ liệu mới.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BotHud {

    static final int PANEL_W = 150, PANEL_H = 69, FACE = 16, FACE_GAP = 4, MARGIN = 6;

    private static BotInfo info;                 // bot đang theo dõi (null ở Sáng tạo)
    private static List<BotInfo> all = List.of();
    private static boolean canSkipNight;
    private static long receivedAt;

    private BotHud() {}

    public static void accept(BotInfo focus, List<BotInfo> bots, boolean skip) {
        info = focus;
        all = bots;
        canSkipNight = skip;
        Minecraft mc = Minecraft.getInstance();
        receivedAt = mc.level != null ? mc.level.getGameTime() : 0L;
    }

    private static boolean fresh() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getGameTime() - receivedAt <= 60L;
    }

    /** true khi đang có dữ liệu bot mới (người xem đang theo dõi một bot). */
    public static boolean isActive() {
        return info != null && fresh();
    }

    /** Tên bot đang hiện trên HUD (null nếu chưa có hoặc dữ liệu đã cũ). */
    public static String botName() {
        return isActive() ? info.name() : null;
    }

    /** Tên mọi bot (để chuyển bằng phím [ ]). */
    public static List<String> botNames() {
        return isActive() ? all.stream().map(BotInfo::name).toList() : List.of();
    }

    /** Tên mọi bot theo thứ tự (dùng để gán màu viền). */
    public static List<String> allBotNames() {
        return allBots().stream().map(BotInfo::name).toList();
    }

    static List<BotInfo> allBots() {
        return fresh() ? all : List.of();
    }

    static boolean skipVisible() {
        Minecraft mc = Minecraft.getInstance();
        return canSkipNight && fresh() && mc.player != null && (mc.player.isSpectator() || mc.player.isCreative());
    }

    // ===== Bố cục (dùng chung với BotBarScreen để biết chỗ trỏ chuột) =====

    static int panelX(int width) {
        return width - PANEL_W - MARGIN;
    }

    static boolean showPanel() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.isSpectator() && isActive();
    }

    static int facesY() {
        return MARGIN + (showPanel() ? PANEL_H + 4 : 0);
    }

    /** Ô mặt thứ i: {x, y}. Mặt xếp hàng từ phải sang trái dưới bảng. */
    static int[] faceRect(int width, int i) {
        int x = width - MARGIN - FACE - i * (FACE + FACE_GAP);
        return new int[] {x, facesY()};
    }

    static boolean showFaces() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.isSpectator() && !allBots().isEmpty();
    }

    /** Nút bỏ qua đêm: {x, y, w, h}. */
    static int[] skipRect(int width) {
        int y = facesY() + (showFaces() ? FACE + 8 : 0);
        return new int[] {width - MARGIN - 90, y, 90, 14};
    }

    static ResourceLocation skinOf(String name) {
        Minecraft mc = Minecraft.getInstance();
        PlayerInfo pi = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(name) : null;
        return pi != null ? pi.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin();
    }

    // ===== Vẽ =====

    @SubscribeEvent
    public static void onOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bot_status", BotHud::render);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        Font font = mc.font;

        if (showPanel()) {
            BotInfo i = info;
            int x = panelX(width), y = MARGIN;
            g.fill(x, y, x + PANEL_W, y + PANEL_H, 0x90000000);
            g.fill(x, y, x + 2, y + PANEL_H, i.danger() ? 0xFFE05050 : 0xFF50C878); // đỏ khi có quái
            List<String> names = botNames();
            int idx = names.indexOf(i.name());
            String pos = names.size() > 1 && idx >= 0 ? "  (" + (idx + 1) + "/" + names.size() + ")" : "";
            g.drawString(font, "Bot: " + i.name() + pos, x + 7, y + 4, 0xFFFFFF, false);
            g.drawString(font, label(i.goal()) + (i.sheltered() ? " (trú)" : ""), x + 7, y + 15, 0xFFE9A5, false);
            bar(g, x + 7, y + 27, 54, i.hp() / 20.0F, 0xFFD03030);
            bar(g, x + 70, y + 27, 54, i.food() / 20.0F, 0xFFC8803C);
            g.drawString(font, "Gỗ " + i.wood() + "  Đá " + i.stone() + "  Cuốc: " + pick(i.pick()),
                    x + 7, y + 40, 0xC8C8C8, false);
            String key = SpectatorCam.KEY.getTranslatedKeyMessage().getString();
            g.drawString(font, "Camera: " + SpectatorCam.modeLabel() + " [" + key + "]", x + 7, y + 51, 0x9FB4FF, false);
        }

        if (showFaces()) {
            List<BotInfo> bots = allBots();
            for (int k = 0; k < bots.size(); k++) {
                BotInfo b = bots.get(k);
                int[] r = faceRect(width, k);
                boolean focused = info != null && b.name().equals(info.name());
                int color = 0xFF000000 | SpectatorCam.BOT_COLORS[k % SpectatorCam.BOT_COLORS.length];
                int border = focused ? 2 : 1; // cùng màu viền với bot trong thế giới; bot đang xem viền dày hơn
                g.fill(r[0] - border, r[1] - border, r[0] + FACE + border, r[1] + FACE + border, color);
                PlayerFaceRenderer.draw(g, skinOf(b.name()), r[0], r[1], FACE);
                bar(g, r[0], r[1] + FACE + 2, FACE, b.hp() / 20.0F, b.danger() ? 0xFFE05050 : 0xFFD03030);
                if (b.goal().equals("SLEEP")) g.drawString(font, "z", r[0] + FACE - 4, r[1] - 2, 0xFFFFFF, true);
            }
            if (!(mc.screen instanceof BotBarScreen)) {
                String alt = BotBarScreen.KEY.getTranslatedKeyMessage().getString();
                int[] r = faceRect(width, bots.size() - 1);
                g.drawString(font, "[" + alt + "]", r[0] - font.width("[" + alt + "]") - 4, r[1] + 4, 0x808080, false);
            }
        }

        if (skipVisible()) {
            int[] r = skipRect(width);
            g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0xC0304060);
            g.drawCenteredString(font, "Bỏ qua đêm", r[0] + r[2] / 2, r[1] + 3, 0xFFFFFF);
        }
    }

    static void bar(GuiGraphics g, int x, int y, int w, float frac, int color) {
        int fill = Math.round(w * Math.max(0.0F, Math.min(1.0F, frac)));
        g.fill(x, y, x + w, y + 3, 0xFF303030);
        g.fill(x, y, x + fill, y + 3, color);
    }

    static String pick(int rank) {
        return switch (rank) {
            case 0 -> "không";
            case 1 -> "gỗ";
            case 2 -> "đá";
            default -> "sắt+";
        };
    }

    static String label(String goal) {
        return switch (goal) {
            case "FLEE" -> "Đang chạy trốn";
            case "FIGHT" -> "Đang đánh";
            case "EAT" -> "Đang ăn";
            case "HUNT" -> "Đang săn thức ăn";
            case "COLLECT" -> "Đang nhặt đồ";
            case "SHELTER" -> "Đang trú ẩn";
            case "WOOD" -> "Đang chặt gỗ";
            case "STONE" -> "Đang đào đá";
            case "DIRT" -> "Đang đào đất";
            case "EXPLORE" -> "Đang khám phá";
            case "HOME" -> "Đang về nhà";
            case "COOK" -> "Đang nấu ăn";
            case "GROUP" -> "Đang về với nhóm";
            case "SHARE" -> "Đang chia đồ";
            case "SLEEP" -> "Đang ngủ";
            case "CRAFT" -> "Đang chế đồ";
            case "BUILD" -> "Đang xây nhà";
            case "BASE" -> "Về nhà chung";
            case "SCOUT" -> "Đi tới chỗ làm nhà";
            case "FARM" -> "Đang làm ruộng";
            case "MINE" -> "Đang khai khoáng";
            default -> "Đang nghỉ";
        };
    }
}
