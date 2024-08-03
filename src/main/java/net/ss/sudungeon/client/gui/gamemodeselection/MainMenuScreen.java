package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainMenuScreen extends Screen {
    private static final Component TITLE = Component.literal("Mode Selection");
    private boolean shouldBlockExit = false;
    private String selectedGameMode = "Standard"; // Chế độ chơi mặc định
    private String selectedCharacter = "steve"; // Nhân vật chơi mặc định

    public MainMenuScreen () {
        super(TITLE);
    }

    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Lấy đối tượng font từ Minecraft
        Font font = Minecraft.getInstance().font;

        // Nút chọn chế độ chơi
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.select_gamemode"),
                button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GameModeSelectionScreen(this));
                }
        ).bounds(centerX - 100, centerY - 20, 200, 20).build());

        // Nút chọn nhân vật
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.select_character"),
                button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new CharacterSelectionScreen(this));
                }
        ).bounds(centerX - 100, centerY + 10, 200, 20).build());

        // Hiển thị chế độ chơi đã chọn
        this.addRenderableWidget(new PlainTextButton(
                centerX - 100, centerY + 50, 200, 20,
                Component.literal("Selected Game Mode: " + getSelectedGameMode()),
                button -> {
                    // Optional: Handle button press here
                },
                font // Cung cấp đối tượng font khi tạo PlainTextButton
        ));

        // Hiển thị nhân vật đã chọn
        this.addRenderableWidget(new PlainTextButton(
                centerX - 100, centerY + 80, 200, 20,
                Component.literal("Selected Character: " + getSelectedCharacter()),
                button -> {
                    // Optional: Handle button press here
                },
                font // Cung cấp đối tượng font khi tạo PlainTextButton
        ));

        // Nút khởi động chế độ chơi đã chọn
        this.addRenderableWidget(Button.builder(
                Component.translatable("menu.start_selected_mode"),
                button -> startSelectedGameMode()
        ).bounds(centerX - 100, centerY + 110, 200, 20).build());
    }

    @Override
    public void onClose() {
        if (this.shouldBlockExit) {
            // Ngăn chặn người chơi thoát nếu vẫn đang chờ xác nhận
            Minecraft.getInstance().setScreen(this);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics); // Render the background
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void setSelectedGameMode (String selectedGameMode) {
        this.selectedGameMode = selectedGameMode;
        // Cập nhật lại giao diện để phản ánh thay đổi
        this.init();
    }

    public String getSelectedGameMode () {
        return selectedGameMode;
    }

    public void setSelectedCharacter (String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
        // Cập nhật lại giao diện để phản ánh thay đổi
        this.init();
    }

    public String getSelectedCharacter () {
        return selectedCharacter;
    }

    private void startSelectedGameMode() {
        assert this.minecraft != null;
        LocalPlayer player = this.minecraft.player;
        this.shouldBlockExit = false;
        Minecraft.getInstance().setScreen(null); // Đóng màn hình hiện tại
        if (player != null && "Standard".equals(selectedGameMode)) {
            // Gửi lệnh tới server từ phía client
            Objects.requireNonNull(player.connection).sendCommand("/create_dungeon random true");

            // Khởi động chế độ Standard
            this.minecraft.setScreen(null); // Đặt màn hình thành null để bắt đầu chơi
        } else if ("Custom".equals(selectedGameMode)) {
            // Khởi động chế độ Custom
            assert player != null;
            Objects.requireNonNull(player.connection).sendCommand("/create_dungeon random true");
            this.minecraft.setScreen(null);
//        } else {
            // Xử lý các chế độ khác nếu có
        }
    }
}