package net.ss.sudungeon.client.gui.gamemodeselection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.entity.player.PlayerCharacterStats;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;
import net.ss.sudungeon.world.level.levelgen.dungeongen.SeedManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class MainMenuScreen extends Screen {
    private static final Component TITLE = Component.literal("Mode Selection");
    private String selectedGameMode = "standard"; // Chế độ chơi mặc định
    private String selectedCharacter = "steve"; // Nhân vật mặc định
    private SeedManager seedManager;

    public MainMenuScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonSpacing = 30; // khoảng cách giữa các nút

        this.addRenderableWidget(Button.builder(
                        Component.translatable("menu.select_game_mode"),
                        button -> openScreen(new GameModeSelectionScreen(this)))
                .bounds(centerX - 100, centerY - buttonSpacing, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("menu.select_character"),
                        button -> openScreen(new CharacterSelectionScreen(this)))
                .bounds(centerX - 100, centerY, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("menu.start_selected_mode"),
                        button -> startSelectedGameMode())
                .bounds(centerX - 100, centerY + buttonSpacing, 200, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Hiển thị chế độ chơi và nhân vật đã chọn
        guiGraphics.drawCenteredString(this.font, "Selected Game Mode: " + this.selectedGameMode, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Selected Character: " + this.selectedCharacter, this.width / 2, 40, 0xFFFFFF);
    }

    // Phương thức để đặt nhân vật được chọn và cập nhật skin
    public void setSelectedCharacter(String character) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            this.selectedCharacter = character;

            // Thiết lập chỉ số và skin của nhân vật
            PlayerCharacterStats.setCharacterSkinAndModel(player, character);
        } else {
            SsMod.LOGGER.error("Player not found!");
        }
    }

    // Phương thức để đặt chế độ chơi được chọn
    public void setSelectedGameMode(String gameMode) {
        this.selectedGameMode = gameMode;
    }

    public String getSelectedGameMode() {
        return selectedGameMode;
    }

    private void startSelectedGameMode() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        LocalPlayer player = this.minecraft.player;

        // Thiết lập vật phẩm khởi đầu cho từng nhân vật
        PlayerCharacterStats.setCharacterAttributes(player, selectedCharacter);
        PlayerCharacterStats.giveCharacterItems(player, selectedCharacter);

        // Các bước còn lại của phương thức
        if (this.seedManager == null) {
            this.seedManager = new SeedManager(RandomSource.create().nextLong());
        }

        long seed = this.seedManager.getRandom().nextLong();
        ServerLevel world = Objects.requireNonNull(this.minecraft.getSingleplayerServer()).getLevel(player.level().dimension());

        if (world == null) {
            player.sendSystemMessage(Component.literal("Cannot access server level!"));
            return;
        }

        // Tạo generator dungeon và hiển thị màn hình loading
        DrunkardWalk dungeonGenerator = new DrunkardWalk(RoomType.START);
        this.minecraft.execute(() -> this.minecraft.setScreen(new LoadingScreen(dungeonGenerator)));

        // Thực hiện tạo dungeon trên luồng server
        this.minecraft.getSingleplayerServer().execute(() -> {
            BlockPos startPos = new BlockPos(0, 1, 0);
            dungeonGenerator.generate(world, startPos, seed);

            // Sau khi tạo dungeon xong, kích hoạt lại trọng lực cho người chơi
            this.minecraft.execute(() -> {
                this.minecraft.setScreen(null); // Quay lại màn hình chính
            });
        });
    }

    private void openScreen(Screen screen) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(screen);
        }
    }
}