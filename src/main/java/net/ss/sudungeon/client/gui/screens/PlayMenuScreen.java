package net.ss.sudungeon.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


@Mod.EventBusSubscriber({Dist.CLIENT})
public class PlayMenuScreen extends Screen {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 10; // Tăng khoảng cách giữa các nút
    protected final Screen lastScreen;

    public PlayMenuScreen (Screen lastScreen) {
        super(Component.translatable("gui.ss.play_menu")); // Tên màn hình có thể dịch được
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init () {
        int numButtons = 3; // Số lượng nút
        int totalHeight = numButtons * BUTTON_HEIGHT + (numButtons - 1) * BUTTON_SPACING;
        int startY = (this.height - totalHeight) / 2; // Tính toán vị trí y bắt đầu

        // Nút Singleplayer
        this.addRenderableWidget(Button.builder(
                        Component.translatable("menu.singleplayer"),
                        (button) -> {
                            assert this.minecraft != null;
                            this.minecraft.setScreen(new SelectWorldScreen(this));
                        })
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        // Nút Multiplayer
        this.addRenderableWidget(Button.builder(
                        Component.translatable("menu.multiplayer"),
                        (button) -> {
                            assert this.minecraft != null;
                            Screen screen = this.minecraft.options.skipMultiplayerWarning ?
                                    new JoinMultiplayerScreen(this) : new SafetyScreen(this);
                            this.minecraft.setScreen(screen);
                        })
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, startY + BUTTON_HEIGHT + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        // Nút Cancel căn giữa với kích thước nhỏ hơn
        int cancelButtonWidth = 200; // Đặt kích thước nhỏ hơn cho nút "Cancel"
        this.addRenderableWidget(Button.builder(
                        CommonComponents.GUI_CANCEL,
                        (button) -> {
                            assert this.minecraft != null;
                            this.minecraft.setScreen(this.lastScreen);
                        })
                .bounds(this.width / 2 - cancelButtonWidth / 2, startY + 2 * (BUTTON_HEIGHT + BUTTON_SPACING), cancelButtonWidth, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Vẽ background mặc định của Menu chính
        RenderSystem.setShader(GameRenderer::getPositionTexShader); // Thiết lập shader
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION); // Đường dẫn texture
        super.renderDirtBackground(guiGraphics);
        // Vẽ các nút bấm
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
