package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class CharacterSelectionScreen extends Screen {
    private final MainMenuScreen parentScreen;
    private String selectedCharacter = "steve"; // Default character

    public CharacterSelectionScreen (MainMenuScreen parentScreen) {
        super(Component.literal("Character Selection"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Steve"),
                        button -> selectCharacter("steve"))
                .bounds(centerX - 100, centerY - 40, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Alex"),
                        button -> selectCharacter("alex"))
                .bounds(centerX - 100, centerY - 20, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Confirm"),
                        button -> confirmSelection())
                .bounds(centerX - 100, centerY + 20, 200, 20).build());
    }

    private void selectCharacter (String character) {
        this.selectedCharacter = character;
    }

    private void confirmSelection () {
        // Cập nhật nhân vật đã chọn vào MainMenuScreen
        this.parentScreen.setSelectedCharacter(this.selectedCharacter);
        // Quay lại màn hình chính
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parentScreen);
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, "Select Your Character", this.width / 2, 20, 0xFFFFFF);
    }
}