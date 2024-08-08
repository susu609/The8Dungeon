package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.client.resources.SkinManager;
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
        String characterLowerCase = character.toLowerCase();
        ResourceLocation skinLocation = new ResourceLocation("ss", "textures/entity/player/" + characterLowerCase + ".png");

        // Lấy tên người chơi hiện tại
        assert this.minecraft != null;
        LocalPlayer player = this.minecraft.player;
        if (player != null) {
            String playerName = player.getName().getString();
            SkinManager.addCustomSkin(playerName, skinLocation);
        }

        mainMenuScreen.setSelectedCharacter(characterLowerCase);
        this.minecraft.setScreen(mainMenuScreen);
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