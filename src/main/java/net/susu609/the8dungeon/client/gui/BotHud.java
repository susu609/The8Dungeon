package net.susu609.the8dungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.ai.BotInfo;

/**
 * Bảng nhỏ ở góc phải trên: chỉ hiện khi bạn đang ở chế độ spectator,
 * cho biết bot đang làm gì. Tự ẩn nếu 3 giây không có dữ liệu mới.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BotHud {

    private static BotInfo info;
    private static java.util.List<String> names = java.util.List.of();
    private static long receivedAt;

    private BotHud() {}

    /** Tên mọi bot trên server (để chuyển bằng phím [ ]). */
    public static java.util.List<String> botNames() {
        return isActive() ? names : java.util.List.of();
    }

    /** Tên bot đang hiện trên HUD (null nếu chưa có hoặc dữ liệu đã cũ). */
    public static String botName() {
        return isActive() ? info.name() : null;
    }

    public static void accept(BotInfo data, java.util.List<String> bots) {
        info = data;
        names = bots;
        Minecraft mc = Minecraft.getInstance();
        receivedAt = mc.level != null ? mc.level.getGameTime() : 0L;
    }

    /** true khi đang có dữ liệu bot mới (nghĩa là người chơi đang theo dõi một bot). */
    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        return info != null && mc.level != null && mc.level.getGameTime() - receivedAt <= 60L;
    }

    @SubscribeEvent
    public static void onOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bot_status", BotHud::render);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (info == null || mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!mc.player.isSpectator() || mc.level.getGameTime() - receivedAt > 60L) return;

        Font font = mc.font;
        BotInfo i = info;
        int w = 150, h = 69;
        int x = width - w - 6, y = 6;
        g.fill(x, y, x + w, y + h, 0x90000000);
        g.fill(x, y, x + 2, y + h, i.danger() ? 0xFFE05050 : 0xFF50C878); // vạch màu: đỏ khi có quái

        int idx = names.indexOf(i.name());
        String pos = names.size() > 1 && idx >= 0 ? "  (" + (idx + 1) + "/" + names.size() + ")" : "";
        g.drawString(font, "Bot: " + i.name() + pos, x + 7, y + 4, 0xFFFFFF, false);
        g.drawString(font, label(i.goal()) + (i.sheltered() ? " (trú)" : ""), x + 7, y + 15, 0xFFE9A5, false);

        bar(g, x + 7, y + 27, 54, i.hp() / 20.0F, 0xFFD03030);
        bar(g, x + 70, y + 27, 54, i.food() / 20.0F, 0xFFC8803C);
        String key = net.susu609.the8dungeon.client.SpectatorCam.KEY.getTranslatedKeyMessage().getString();
        g.drawString(font, "Camera: " + net.susu609.the8dungeon.client.SpectatorCam.modeLabel()
                + " [" + key + "]", x + 7, y + 51, 0x9FB4FF, false);
        g.drawString(font, "Gỗ " + i.wood() + "  Đá " + i.stone() + "  Cuốc: " + pick(i.pick()),
                x + 7, y + 40, 0xC8C8C8, false);
    }

    private static void bar(GuiGraphics g, int x, int y, int w, float frac, int color) {
        int fill = Math.round(w * Math.max(0.0F, Math.min(1.0F, frac)));
        g.fill(x, y, x + w, y + 6, 0xFF303030);
        g.fill(x, y, x + fill, y + 6, color);
    }

    private static String pick(int rank) {
        return switch (rank) {
            case 0 -> "không";
            case 1 -> "gỗ";
            case 2 -> "đá";
            default -> "sắt+";
        };
    }

    private static String label(String goal) {
        return switch (goal) {
            case "FLEE" -> "Đang chạy trốn";
            case "FIGHT" -> "Đang đánh quái";
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
            default -> "Đang nghỉ";
        };
    }
}
