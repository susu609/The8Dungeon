package net.ss.sudungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.network.SsModVariables;

import static net.ss.sudungeon.event.PlayerManaStaminaEvents.getPlayerAttribute;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerStatBarGui {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("ss", "/textures/gui/player_stat_bar.png");

    private static final int BAR_WIDTH = 76;   // Độ rộng của thanh
    private static final int BAR_HEIGHT = 5;   // Độ cao của thanh

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private static Position position = Position.BOTTOM_LEFT; // Mặc định là góc dưới bên trái

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (mc.player == null) return;

        SsModVariables.PlayerVariables playerStats = getPlayerStats(mc.player);
        if (playerStats == null) return;

        float currentHealth = mc.player.getHealth();
        float currentMana = (float) playerStats.mana;
        float currentStamina = (float) playerStats.stamina;

        int x = 0;
        int y = 0;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Xác định vị trí dựa trên giá trị của enum Position
        switch (position) {
            case TOP_LEFT:
                x = 0;
                y = 0;
                break;
            case TOP_RIGHT:
                x = screenWidth - 99;
                y = 0;
                break;
            case BOTTOM_LEFT:
                x = 0;
                y = screenHeight - 22;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - 99 ;
                y = screenHeight - 22 ;
                break;
        }

        renderBars(event.getGuiGraphics(), currentHealth, currentMana, currentStamina, mc.player, x, y);
    }

    private static void renderBars(GuiGraphics guiGraphics, float currentHealth, float currentMana, float currentStamina, Player player, int x, int y) {
        // Render khung nền duy nhất
        renderEmptyBar(guiGraphics, x, y);

        // Render mặt người chơi ở vị trí (3, 3) với kích thước 16x16
        renderPlayerFace(guiGraphics, player, x + 3, y + 3, 16);

        // Render thanh máu
        assert mc.player != null;
        float maxHealth = mc.player.getMaxHealth();
        if (currentHealth < maxHealth * 0.1f) {
            x += getShakeOffset();
            y += getShakeOffset();
        }
        renderFilledBar(guiGraphics, x, y, currentHealth, maxHealth, 3);  // Thanh đỏ, đặt tại vị trí 21, 3

        // Render thanh mana
        float maxMana = getPlayerAttribute(player, SsModAttributes.MAX_MANA.get());
        if (currentMana < maxMana * 0.1f) {
            x += getShakeOffset();
            y += getShakeOffset();
        }
        renderFilledBar(guiGraphics, x, y + 6, currentMana, maxMana, 9);  // Thanh xanh dương, đặt tại vị trí 21, 9

        // Render thanh stamina
        float maxStamina = getPlayerAttribute(player, SsModAttributes.MAX_STAMINA.get());
        if (currentStamina < maxStamina * 0.1f) {
            x += getShakeOffset();
            y += getShakeOffset();
        }
        renderFilledBar(guiGraphics, x, y + 12, currentStamina, maxStamina, 15);  // Thanh xanh lá, đặt tại vị trí 21, 15
    }

    // Render thanh rỗng (phần khung nền)
    private static void renderEmptyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, 0, 99, 22);
    }

    // Render thanh đầy, tọạ độ cho mỗi thanh dựa vào y và vị trí tương ứng
    private static void renderFilledBar(GuiGraphics guiGraphics, int x, int y, float currentStat, float maxStat, int vOffset) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int filledWidth = calculateBarWidth(currentStat, maxStat);

        // Render thanh đầy
        guiGraphics.blit(GUI_BARS_LOCATION, x + 21, y + vOffset, 21, 25 + (vOffset - 3), filledWidth, BAR_HEIGHT);

        RenderSystem.disableBlend();
    }

    // Tính toán độ dài của thanh dựa trên giá trị hiện tại
    private static int calculateBarWidth(float currentStat, float maxStat) {
        return Math.round((currentStat / maxStat) * BAR_WIDTH);
    }

    // Phương thức render mặt người chơi
    private static void renderPlayerFace(GuiGraphics guiGraphics, Player player, int x, int y, int size) {
        // Lấy URL skin của người chơi từ capability
        String skinUrl = getPlayerSkinUrl(player);
        ResourceLocation playerSkin = new ResourceLocation(skinUrl);

        // Vẽ mặt người chơi, lấy phần 8x8 (phần mặt) từ skin 64x64
        guiGraphics.blit(playerSkin, x, y, size, size, 8, 8, 8, 8, 64, 64);
    }

    // Phương thức lấy URL skin từ capability
    private static String getPlayerSkinUrl(Player player) {
        return player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(playerVars -> playerVars.skinUrl != null && !playerVars.skinUrl.isEmpty()
                        ? playerVars.skinUrl
                        : "ss:textures/entity/player/wide/steve.png")  // Skin mặc định
                .orElse("ss:textures/entity/player/wide/steve.png"); // Trường hợp không có capability
    }

    private static SsModVariables.PlayerVariables getPlayerStats(Player player) {
        return player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new SsModVariables.PlayerVariables());
    }

    // Phương thức tạo hiệu ứng rung bằng cách tạo offset ngẫu nhiên
    private static int getShakeOffset() {
        return (int) (Math.random() * 4 - 2); // Giá trị dao động từ -2 đến 2
    }
}