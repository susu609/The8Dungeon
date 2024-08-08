package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.ss.sudungeon.utils.PlayerSkinManager;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;
import net.ss.sudungeon.world.level.levelgen.dungeongen.SeedManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainMenuScreen extends Screen {
    private static final Component TITLE = Component.literal("Mode Selection");
    private String selectedGameMode = "standard"; // Default game mode
    private String selectedCharacter = "steve"; // Default character
    private SeedManager seedManager;

    public MainMenuScreen () {
        super(TITLE);
    }

    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        PlayerSkinManager.setSelectedCharacter(selectedCharacter); // Mặc định khi màn hình được tải

        // Button for selecting game mode
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.select_game_mode"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new GameModeSelectionScreen(this));
                    }
                }
        ).bounds(centerX - 100, centerY - 50, 200, 20).build());

        // Button for selecting character
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.select_character"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new CharacterSelectionScreen(this));
                    }
                }
        ).bounds(centerX - 100, centerY, 200, 20).build());

        // Button for starting the selected game mode
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.start_selected_mode"),
                button -> startSelectedGameMode()
        ).bounds(centerX - 100, centerY + 50, 200, 20).build());
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Display the selected game mode
        guiGraphics.drawCenteredString(this.font, "Selected Game Mode: " + getSelectedGameMode(), this.width / 2, 20, 0xFFFFFF);
        // Display the selected character
        guiGraphics.drawCenteredString(this.font, "Selected Character: " + getSelectedCharacter(), this.width / 2, 40, 0xFFFFFF);
    }

    public String getSelectedGameMode () {
        return selectedGameMode;
    }

    public String getSelectedCharacter () {
        return selectedCharacter;
    }

    public void setSelectedGameMode (String gameMode) {
        this.selectedGameMode = gameMode;
    }

    public void setSelectedCharacter (String character) {
        this.selectedCharacter = character;
        PlayerSkinManager.setSelectedCharacter(character); // Cập nhật skin khi nhân vật được chọn
    }

    @Override
    public boolean shouldCloseOnEsc () {
        return false; // Prevent closing with Esc
    }

    private void startSelectedGameMode () {
        if (this.minecraft == null || this.minecraft.player == null) {
            return; // Early return if conditions are not met
        }

        // Initialize SeedManager if not already initialized
        if (this.seedManager == null) {
            this.seedManager = new SeedManager(RandomSource.create().nextLong());
        }

        long seed = this.seedManager.getRandom().nextLong(); // Use a random seed from SeedManager

        LocalPlayer player = this.minecraft.player;

        // Ensure we're running on the server thread for single-player
        Objects.requireNonNull(this.minecraft.getSingleplayerServer()).execute(() -> {
            ServerLevel world = this.minecraft.getSingleplayerServer().getLevel(player.level().dimension());
            if (world == null) {
                player.sendSystemMessage(Component.literal("Cannot access server level!"));
                return;
            }

            BlockPos startPos = new BlockPos(0, 1, 0);
            // Create the dungeon generator and pass it to the loading screen
            DrunkardWalk dungeonGenerator = new DrunkardWalk(RoomType.START);
            LoadingScreen loadingScreen = new LoadingScreen(dungeonGenerator);
            this.minecraft.execute(() -> this.minecraft.setScreen(loadingScreen));

            // Execute the dungeon generation
            dungeonGenerator.generate(world, startPos, seed);

            // Update the UI after dungeon generation
            this.minecraft.execute(() -> {
                this.minecraft.setScreen(null); // Return to the previous screen
                player.sendSystemMessage(Component.literal("Dungeon created successfully!"));
            });
        });
    }
}
