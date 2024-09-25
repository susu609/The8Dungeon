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

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadingScreen extends Screen {
    private static final Component TITLE = Component.literal("Loading...");
    private static final int PROGRESS_BAR_HEIGHT = 10; // Constant for progress bar height
    private final Minecraft minecraft;
    private final DrunkardWalk dungeonGenerator; // Đối tượng DrunkardWalk để lấy tiến trình
    private final long startTime;
    private Long doneTime = null; // Thời điểm hoàn thành tiến trình
    private int lastReportedProgress = -1; // Biến để theo dõi tiến trình đã báo cáo lần trước

    // Định dạng thời gian cho báo cáo
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

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

        // Lấy tiến trình tạo dungeon
        int progress = dungeonGenerator.getProgress();

        // Lấy thời gian hiện tại cho báo cáo
        String currentTime = timeFormatter.format(new Date());

        // Chỉ hiển thị nếu tiến trình khác với lần trước
        if (progress != lastReportedProgress) {
            lastReportedProgress = progress;
            System.out.println("[" + currentTime + "] Progress: " + progress + "%");
        }

        // Tính toán thời gian đã trôi qua
        long elapsedTime = System.currentTimeMillis() - startTime;
        String elapsedTimeStr = String.format("Time Loading : %d seconds", elapsedTime / 1000);

        // Hiển thị thông báo dựa trên tiến trình
        String message = progress < 100 ? "Please wait..." : "Done ! :D";
        guiGraphics.drawCenteredString(this.font, message, this.width / 2, this.height / 2 - 20, 0xFFFFFF);

        // Hiển thị thời gian đã trôi qua
        guiGraphics.drawCenteredString(this.font, elapsedTimeStr, this.width / 2, this.height / 2 - 10, 0xAAAAAA);

        // Vẽ thanh tiến trình
        int progressBarWidth = this.width / 2;
        int progressBarX = (this.width - progressBarWidth) / 2;
        int progressBarY = this.height / 2 + 20;
        drawProgressBar(guiGraphics, progressBarX, progressBarY, progressBarWidth, progress);

        // Chuyển sang BlackScreenOverlay khi tiến trình hoàn tất
        if (progress >= 100) {
            // Nếu tiến trình đạt 100%, lưu thời điểm hiện tại
            if (doneTime == null) {
                doneTime = System.currentTimeMillis();
                System.out.println("[" + currentTime + "] Progress completed.");
            }
            // Nếu đã qua 2 giây kể từ khi hoàn thành, chuyển đổi
            else if (System.currentTimeMillis() - doneTime >= 2000) {
                transitionToBlackScreenOverlay();
            }
        }
    }

    private void drawProgressBar (GuiGraphics guiGraphics, int x, int y, int width, int progress) {
        // Vẽ nền thanh tiến trình
        guiGraphics.fill(x, y, x + width, y + PROGRESS_BAR_HEIGHT, 0xFF8B8B8B);
        // Vẽ phần đã hoàn tất
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
                    BlockPos targetPos = new BlockPos(8, 6, 8);
                    player.teleportTo(world, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 0, 0);
                }

                // Hiển thị overlay đen
                BlackScreenOverlay.showOverlay();

                // Tùy chọn đóng màn hình tải nếu cần
                this.minecraft.setScreen(null);
            } else {
                // Trường hợp multiplayer, không thực hiện gì
                // Có thể thêm thông báo hoặc ghi log nếu cần
                System.out.println("Multiplayer mode detected, skipping teleport.");
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc () {
        return false; // Ngăn đóng bằng Esc
    }
}
