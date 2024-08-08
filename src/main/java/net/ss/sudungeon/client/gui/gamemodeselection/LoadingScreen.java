package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.ss.sudungeon.client.gui.BlackScreenOverlay;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import org.jetbrains.annotations.NotNull;

public class LoadingScreen extends Screen {
    private static final Component TITLE = Component.literal("Loading...");
    private static final int PROGRESS_BAR_HEIGHT = 10; // Constant for progress bar height
    private final Minecraft minecraft;
    private final long startTime;
    private final DrunkardWalk dungeonGenerator; // Đối tượng DrunkardWalk để lấy tiến trình

    public LoadingScreen (DrunkardWalk dungeonGenerator) {
        super(TITLE);
        this.minecraft = Minecraft.getInstance();
        this.startTime = System.currentTimeMillis();
        this.dungeonGenerator = dungeonGenerator;
    }

    @Override
    protected void init () {
        super.init();
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Draw the loading message centered on the screen
        guiGraphics.drawCenteredString(this.font, "Please wait...", this.width / 2, this.height / 2, 0xFFFFFF);

        // Draw progress bar
        int progress = dungeonGenerator.getProgress();
        int progressBarWidth = this.width / 2;
        int progressBarX = (this.width - progressBarWidth) / 2;
        int progressBarY = this.height / 2 + 20;
        drawProgressBar(guiGraphics, progressBarX, progressBarY, progressBarWidth, progress);

        // Transition to BlackScreenOverlay after 3 seconds of loading
        if (System.currentTimeMillis() - startTime > 3000) {
            transitionToBlackScreenOverlay();
        }
    }

    private void drawProgressBar (GuiGraphics guiGraphics, int x, int y, int width, int progress) {
        // Draw background
        guiGraphics.fill(x, y, x + width, y + PROGRESS_BAR_HEIGHT, 0xFF8B8B8B);
        // Draw foreground
        int filledWidth = (int) (width * (progress / 100.0));
        guiGraphics.fill(x, y, x + filledWidth, y + PROGRESS_BAR_HEIGHT, 0xFFFFFFFF);
    }

    private void transitionToBlackScreenOverlay () {
        // Lấy người chơi hiện tại
        if (minecraft.player != null) {
            if (minecraft.getSingleplayerServer() != null) {
                // Trường hợp chơi đơn (singleplayer)
                // Dịch chuyển người chơi trong chế độ singleplayer
                ServerLevel world = minecraft.getSingleplayerServer().overworld(); // Lấy thế giới chính
                ServerPlayer player = minecraft.getSingleplayerServer().getPlayerList().getPlayer(minecraft.player.getUUID());

                if (player != null) {
                    BlockPos targetPos = new BlockPos(8, 1, 8);
                    player.teleportTo(world, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 0, 0);
                }

                // Hiển thị overlay đen
                BlackScreenOverlay.showOverlay();

                // Optionally close the loading screen if necessary
                if (System.currentTimeMillis() - startTime > 1000) {
                    this.minecraft.setScreen(null);
                }
            } else {
                // Trường hợp multiplayer, không thực hiện gì
                // Bạn có thể thêm thông báo hoặc ghi log nếu cần
                System.out.println("Multiplayer mode detected, skipping teleport.");
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc () {
        return false; // Prevent closing with Esc
    }
}
