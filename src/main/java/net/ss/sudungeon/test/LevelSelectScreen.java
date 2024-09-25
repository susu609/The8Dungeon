package net.ss.sudungeon.test;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LevelSelectScreen extends Screen {
    private final int gridWidth = 5; // number of squares in a row
    private final int gridHeight = 3; // number of squares in a column
    private final List<GameLevel> levels; // List of levels
    private final GameLevel currentLevel; // The current level

    public LevelSelectScreen (List<GameLevel> levels, GameLevel currentLevel) {
        super(Component.literal("Select Level"));
        this.levels = levels;
        this.currentLevel = currentLevel;
    }

    @Override
    protected void init () {
        int buttonWidth = 100;
        int buttonHeight = 100;
        int startX = (this.width - gridWidth * buttonWidth) / 2;
        int startY = (this.height - gridHeight * buttonHeight) / 2;

        for (int i = 0; i < levels.size(); i++) {
            GameLevel level = levels.get(i);
            int x = startX + (i % gridWidth) * buttonWidth;
            int y = startY + (i / gridWidth) * buttonHeight;

            // Check if the level is locked
            if (level.locked()) {
                // Create a button for locked levels
                Button lockedButton = Button.builder(Component.literal("Locked"), button -> {
                    // Handle event when the player tries to click a locked level
                }).bounds(x, y, buttonWidth, buttonHeight).build();
                this.addRenderableWidget(lockedButton);
            } else {
                // Create a button for unlocked levels
                Button levelButton = Button.builder(Component.literal(level.displayName()), button -> {
                    // Handle event when selecting a level
                    System.out.println("Selected level: " + level.displayName());
                }).bounds(x, y, buttonWidth, buttonHeight).build();

                // Check if this is the current level
                if (level == currentLevel) {
                    // Add glowing border or change button color to highlight the current level
                    levelButton.setFocused(true);
                }

                this.addRenderableWidget(levelButton);
            }
        }
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }
}

