package net.ss.sudungeon.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ss.sudungeon.command.DungeonCommand;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.util.BaseScreen;
import net.ss.sudungeon.util.DungeonRandom;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.inventory.FillToolMenu;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public class MainMenuScreen extends BaseScreen {
// dự án mới đây
    private final boolean isClientPlayer; // Xác định vai trò người chơi
    private EditBox seedInput;            // Ô nhập seed
    private String selectedButton;        // Nút đã chọn để highlight
    private String selectedCharacter = "steve"; // Mặc định là Steve
    public final static HashMap<String, Object> guistate = MainMenuScreen.guistate;

    @SuppressWarnings("ConstantConditions")
    public MainMenuScreen () {
        super(Component.literal("Select Game Mode"));
        this.isClientPlayer = Minecraft.getInstance() != null;
        this.selectedButton = ""; // Khởi tạo nút chưa được chọn
        Log.i("[MainMenuScreen] Đã tạo màn hình chọn chế độ."); // ✅ Ghi log
    }

    @Override
    protected void init () {
        Log.i("[MainMenuScreen] Đã khởi tạo giao diện chọn chế độ."); // ✅ Ghi log
        super.init();
        clearAllButtons(); // Xóa widget trước
        backgroundRendered = false; // Biến để kiểm tra đã vẽ nền hay chưa
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonSpacing = 25;

        // Thêm ô nhập seed
        this.seedInput = new EditBox(this.font, centerX - 100, centerY - 70, 200, 20, Component.literal("Enter seed"));
        this.seedInput.setMaxLength(20); // Tối đa 20 ký tự
        this.seedInput.setValue(""); // Giá trị mặc định là rỗng
        this.addRenderableWidget(seedInput);

        // Nút chọn chế độ Dungeon Mode
        addButtonWithBounds(centerX - 100, centerY - buttonSpacing, 200, 20,
                Component.literal("Dungeon Mode"),
                button -> {
                    if (isClientPlayer) {
                        this.selectedButton = "Dungeon Mode"; // Lưu trạng thái nút đã chọn
                        this.selectDungeonMode();
                    } else {
                        this.serverCannotInteract("Dungeon Mode");
                    }
                });

        // Nút chọn chế độ Arena Mode
        addButtonWithBounds(centerX - 100, centerY, 200, 20,
                Component.literal("Arena Mode"),
                button -> {
                    if (isClientPlayer) {
                        this.selectedButton = "Arena Mode"; // Lưu trạng thái nút đã chọn
                        this.selectArenaMode();
                    } else {
                        this.serverCannotInteract("Arena Mode");
                    }
                });

    }

    /**
     * Gửi lệnh tới server để thực thi một lệnh cụ thể.
     *
     * @param command Lệnh cần gửi (bao gồm tiền tố "/").
     */
    private void sendCommandToServer (String command) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            // Gửi lệnh qua chat để server xử lý
            minecraft.player.connection.sendCommand(command);
            Log.i("Command sent to server: " + command);
        } else {
            Log.e("Player instance is null. Cannot send command.");
        }
    }

    private void selectDungeonMode() {
        long seed = parseSeedInput();
        Log.i("Selected Dungeon Mode with seed: " + seed);
        DungeonRandom.setSeed(seed);
        applyModeAndSeed(seed, "dungeon");
        mc.setScreen(null);
    }

    private void selectArenaMode() {
        long seed = parseSeedInput();
        Log.i("Arena Mode selected with seed: " + seed);
        DungeonRandom.setSeed(seed);
        applyModeAndSeed(seed, "arena");
        mc.setScreen(null);
    }


    private void serverCannotInteract (String modeName) {
        Log.i("Người chơi server đã cố gắng chọn chế độ: " + modeName + ", nhưng không có quyền.");
    }

    private long parseSeedInput() {
        String input = this.seedInput.getValue();
        if (input.isEmpty()) {
            long randomSeed = new Random().nextLong();
            Log.i("Không nhập seed. Sử dụng seed ngẫu nhiên: " + randomSeed);
            return randomSeed;
        }

        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            long fallback = input.hashCode();
            Log.w("Seed không hợp lệ. Dùng hashCode làm seed: " + fallback);
            return fallback;
        }
    }


    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);

        // Hiển thị tiêu đề
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Với người server, thêm thông báo chỉ để xem
        if (!isClientPlayer) {
            guiGraphics.drawCenteredString(this.font, "(Chế độ xem chỉ dành cho Server Player)",
                    this.width / 2, this.height / 2 + 80, 0xFF5555);
        }

        // Vẽ viền cho nút đã chọn
        if (!selectedButton.isEmpty()) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            if (selectedButton.equals("Dungeon Mode")) {
                // Vẽ viền cho Dungeon Mode
                guiGraphics.renderOutline(centerX - 102, centerY - 50, 200, 20, 0xFF0000FF); // Viền xanh
            } else if (selectedButton.equals("Arena Mode")) {
                // Vẽ viền cho Arena Mode
                guiGraphics.renderOutline(centerX - 102, centerY - 25, 200, 20, 0xFFFF0000); // Viền đỏ
            }
        }

        // Render ô nhập seed
        this.seedInput.render(guiGraphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Toạ độ để đặt mô hình nhân vật trong khung vuông
        int boxX = centerX + 60;  // sang phải
        int boxY = centerY - 20;  // thấp hơn
        int scale = 50;           // to hơn

        Minecraft mc = Minecraft.getInstance();
        Player fake = createPreviewPlayer(mc.player, selectedCharacter);
        if (mc.player != null) {
            // Vẽ mô hình 3D trong khung vuông
            InventoryScreen.renderEntityInInventoryFollowsAngle(
                    guiGraphics,
                    boxX, boxY, scale,
                    -0.5f, 0.2f, // góc nghiêng trái & hơi nhìn lên
                    fake
            );
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    private Player createPreviewPlayer(Player original, String characterId) {
        Minecraft mc = Minecraft.getInstance();

        if (!(original instanceof LocalPlayer originalLocal)) {
            Log.w("Không thể tạo preview vì player không phải LocalPlayer.");
            return original;
        }

        // Tạo bản sao preview (dummy)
        assert mc.level != null;
        LocalPlayer copy = new LocalPlayer(
                mc,
                mc.level,
                originalLocal.connection,
                originalLocal.getStats(),
                originalLocal.getRecipeBook(),
                false,
                false
        );

        copy.setPos(original.position());
        copy.setYRot(original.getYRot());
        copy.setXRot(original.getXRot());
        copy.setPose(original.getPose());

        // Gán biến "character"
//        copy.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {
//            vars.setVariable("character", characterId);
//        });

        return copy;
    }

    private void applyModeAndSeed(long seed, String mode) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
//            SsModVariables.WorldVariables.setDungeonState(level, seed, true);
        }

        Log.i("[MainMenuScreen] Mode: " + mode + " | Seed: " + seed);

        Minecraft.getInstance().execute(() -> {
            var server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) return;

            var player = server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
            if (player == null) return;

            {
                ServerLevel serverlevel = server.overworld(); // hoặc dungeon_dimension nếu cần
                switch (mode.toLowerCase()) {
                    case "dungeon" -> DungeonCommand.createDungeon(serverlevel, seed);
                    case "arena" -> DungeonCommand.createArena(serverlevel, seed);
                }
            }

        });
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }
}