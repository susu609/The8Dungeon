package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CharacterSelectionScreen extends Screen {
    private final MainMenuScreen mainMenuScreen;

    public CharacterSelectionScreen (MainMenuScreen mainMenuScreen) {
        super(Component.translatable("menu.select_character"));
        this.mainMenuScreen = mainMenuScreen;
    }

    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.character.steve"),
                button -> onCharacterSelected("steve")
        ).bounds(centerX - 100, centerY - 20, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.character.alex"),
                button -> onCharacterSelected("alex")
        ).bounds(centerX - 100, centerY + 10, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.back_to_main_menu"),
                button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(mainMenuScreen);
                }
        ).bounds(centerX - 100, centerY + 40, 200, 20).build());
    }

    private void onCharacterSelected (String character) {
        // Chuyển đổi tên nhân vật về chữ thường
        String characterLowerCase = character.toLowerCase();
        mainMenuScreen.setSelectedCharacter(characterLowerCase);
        assert this.minecraft != null;
        this.minecraft.setScreen(mainMenuScreen);
    }


    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
