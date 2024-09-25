package net.ss.sudungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerBarsGui {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("ss", "/textures/gui/player_bars.png");
    public static boolean print = true;

    @SubscribeEvent
    public static void onRenderGuiOverlay (RenderGuiOverlayEvent.Post event) {
        if (mc.player == null) return;

        mc.player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(playerStats -> {
            // Sử dụng playerStats như trước


            if (print) {
                System.out.println("Rendering custom player bars");
                print = false;
            }

            int x = 4;
            int y = 4;

            // Tính toán chiều dài của thanh máu
            float currentHealth = mc.player.getHealth();
            float maxHealth = mc.player.getMaxHealth();
            int healthBarWidth = calculateBarWidth(maxHealth, 20, 22);

            // Tính toán chiều dài của thanh mana
            float currentMana = (float) playerStats.mana;
            float maxMana = (float) playerStats.maxMana;
            int manaBarWidth = calculateBarWidth(maxMana, 20, 22);

            // Tính toán chiều dài của thanh stamina
            float currentStamina = (float) playerStats.stamina;
            float maxStamina = (float) playerStats.maxStamina;
            int staminaBarWidth = calculateBarWidth(maxStamina, 20, 22);

            // Render thanh máu
            renderEmptyBar(event.getGuiGraphics(), x, y, healthBarWidth); // Thanh rỗng máu
            renderFilledBar(event.getGuiGraphics(), x, y, (int) currentHealth, (int) maxHealth, 2, 1.0f, 0.0f, 0.0f); // Thanh đầy máu (màu đỏ)
            y += 5 + 2;

            // Render thanh mana
            renderEmptyBar(event.getGuiGraphics(), x, y, manaBarWidth); // Thanh rỗng mana
            renderFilledBar(event.getGuiGraphics(), x, y, (int) currentMana, (int) maxMana, 2, 0.0f, 0.0f, 1.0f); // Thanh đầy mana (màu xanh dương)
            y += 5 + 2;

            // Render thanh stamina
            renderEmptyBar(event.getGuiGraphics(), x, y, staminaBarWidth); // Thanh rỗng stamina
            renderFilledBar(event.getGuiGraphics(), x, y, (int) currentStamina, (int) maxStamina, 2, 0.0f, 1.0f, 0.0f); // Thanh đầy stamina (màu xanh lá)
        });
    }


    // Hàm tính toán chiều dài của thanh dựa trên chỉ số hiện tại, với chiều dài thêm 2 pixel miễn phí
    private static int calculateBarWidth (float currentStat, float maxStat, int referenceWidth) {
        return Math.round((currentStat / maxStat) * referenceWidth);
    }


    // Hàm render thanh rỗng
    private static void renderEmptyBar (GuiGraphics guiGraphics, int x, int y, int dynamicWidth) {
        // Thiết lập màu sắc cho thanh rỗng (ví dụ: xám)
        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);

        // Render phần mở của thanh rỗng
        guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, 0, 2, 5);

        // Render phần giữa của thanh rỗng
        for (int i = 2; i < dynamicWidth; i++) {
            guiGraphics.blit(GUI_BARS_LOCATION, x + i, y, 3, 0, 1, 5);
        }

        // Render phần đóng của thanh rỗng
        guiGraphics.blit(GUI_BARS_LOCATION, x + dynamicWidth, y, 5, 0, 1, 5);

        // Đặt lại màu mặc định sau khi render
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }


    private static void renderFilledBar (GuiGraphics guiGraphics, int x, int y, int currentStat, int maxStat, int borderOffset, float red, float green, float blue) {
        // Thiết lập màu sắc cho thanh đầy
        RenderSystem.enableBlend();  // Kích hoạt chế độ blend để màu được áp dụng
        RenderSystem.defaultBlendFunc();  // Sử dụng chế độ blend mặc định
        RenderSystem.setShaderColor(red, green, blue, 1.0f);  // Thiết lập màu cho thanh đầy

        // Tính toán độ rộng của thanh đầy dựa trên giá trị hiện tại
        int filledWidth = (int) ((currentStat / (float) maxStat) * (20));

        // Render phần mở của thanh đầy (y = 6)
        guiGraphics.blit(GUI_BARS_LOCATION, x + borderOffset, y, 0, 6, 2, 5);

        // Render phần giữa của thanh đầy (tọa độ 3,6, kích thước 1x5)
        for (int i = 2; i < filledWidth - 2; i++) {
            guiGraphics.blit(GUI_BARS_LOCATION, x + borderOffset + i, y, 3, 6, 1, 5);
        }

        // Render phần đóng của thanh đầy (tọa độ 5,6, kích thước 1x5)
        guiGraphics.blit(GUI_BARS_LOCATION, x + borderOffset + filledWidth - 2, y, 5, 6, 1, 5);

        // Đặt lại màu về mặc định sau khi render xong
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();  // Tắt chế độ blend sau khi render xong
    }

}