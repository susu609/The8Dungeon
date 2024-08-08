package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GameModeSelectionScreen extends Screen {
    private final MainMenuScreen mainMenuScreen;

    public GameModeSelectionScreen (MainMenuScreen mainMenuScreen) {
        super(Component.translatable("menu.select_game_mode"));
        this.mainMenuScreen = mainMenuScreen;
    }
    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Chọn chế độ Standard
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.standard_mode"),
                button -> {
                    mainMenuScreen.setSelectedGameMode("Standard");
                    assert this.minecraft != null;
                    this.minecraft.setScreen(mainMenuScreen);
                }
        ).bounds(centerX - 100, centerY - 20, 200, 20).build());

        // Chọn chế độ Custom
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.custom_mode"),
                button -> {
                    mainMenuScreen.setSelectedGameMode("Custom");
                    assert this.minecraft != null;
                    this.minecraft.setScreen(mainMenuScreen);
                }
        ).bounds(centerX - 100, centerY + 10, 200, 20).build());

        // Trở về Main Menu mà không chọn chế độ
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.back_to_main_menu"),
                button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(mainMenuScreen);
                }
        ).bounds(centerX - 100, centerY + 40, 200, 20).build());
    }

    @Override
    public boolean shouldCloseOnEsc () {
        return false;
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

    }
}