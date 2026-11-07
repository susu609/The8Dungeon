package net.susu609.the8dungeons.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import net.susu609.the8dungeons.The8DungeonsMod;
import net.susu609.the8dungeons.client.gui.ScreenUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Function;

/**
 * Màn hình tiêu đề riêng của The8Dungeons, kế thừa TitleScreen của vanilla.
 * Thay vì dùng layout vanilla (nút căn giữa màn hình), class này dựng menu
 * dạng "panel bên trái" với danh sách nút xếp dọc: New Game, Continue, Mods,
 * Options, Quit, cộng thêm 3 nút phụ (ngôn ngữ, trợ năng, bản quyền).
 */
public class ModTitleScreen extends TitleScreen implements TitleScreenInterface {
    private static final int MAIN_BUTTON_COUNT = 5;

    /**
     * Ghi lỗi vào logs/latest.log.
     */
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * Kích thước/vị trí của panel và nút. Tính một lần trong init(), dùng lại cho mọi nút.
     */
    private Layout layout;

    /**
     * [Forge] Chỉ báo "có bản cập nhật mod" (dấu chấm nhỏ cạnh nút Mods).
     */
    private TitleScreenModUpdateIndicator modUpdateNotification;

    public ModTitleScreen() {
        super(false); // false = không dùng hiệu ứng fade-in của vanilla
    }

    /**
     * Giới hạn giá trị v trong khoảng [min, max].
     */
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static Options options() {
        return Minecraft.getInstance().options;
    }

    private static LanguageManager LanguageManager() {
        return Minecraft.getInstance().getLanguageManager();
    }

    /**
     * Được gọi khi màn hình mở hoặc khi cửa sổ đổi kích thước.
     * Vanilla sẽ xóa sạch widget cũ trước khi gọi, nên ở đây phải thêm lại toàn bộ.
     */
    @Override
    public void init() {
        // Không gọi super.init(): vanilla sẽ thêm bộ nút gốc và đè lên menu tự chế này.

        // Tính layout theo kích thước cửa sổ hiện tại (init chạy lại mỗi khi resize)
        layout = calculateLayout();

        // ===== Cột nút chính (hàng 0..4, khớp MAIN_BUTTON_COUNT) =====
        addMainButton(0, Component.literal("New Game"), b -> onNewGame());
        addMainButton(1, Component.literal("Continue"), b -> onContinue());

        // [Forge] Nút Mods: hứng kết quả trả về để gắn chỉ báo cập nhật
        Button modButton = addMainButton(2, Component.translatable("fml.menu.mods"), b -> ScreenUtil.open(new ModListScreen(this)));
        this.modUpdateNotification = TitleScreenModUpdateIndicator.init(this, modButton);

        addMainButton(3, Component.translatable("menu.options"), b -> ScreenUtil.open(new OptionsScreen(this, options())));
        addMainButton(4, Component.translatable("menu.quit"), b -> ScreenUtil.quit());

        // ===== Hàng icon phụ, nằm ngay dưới cột nút chính =====
        // Đáy cột nút = buttonTop + 5 * (cao + hở). Số này đã gồm sẵn một khoảng hở dưới nút cuối.
        int iconSize = 20;
        int iconY = layout.buttonTop() + MAIN_BUTTON_COUNT * (layout.buttonHeight() + layout.buttonGap());

        // Icon ngôn ngữ: sát mép trái của cột nút
        this.addRenderableWidget(new ImageButton(layout.buttonX(), iconY, iconSize, iconSize, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, btn -> ScreenUtil.open(new LanguageSelectScreen(this, options(), LanguageManager())), Component.translatable("narrator.button.language")));

        // Icon trợ năng: sát mép phải của cột nút
        this.addRenderableWidget(new ImageButton(layout.buttonX() + layout.buttonWidth() - iconSize, iconY, iconSize, iconSize, 0, 0, 20, Button.ACCESSIBILITY_TEXTURE, 32, 64, btn -> ScreenUtil.open(new AccessibilityOptionsScreen(this, options())), Component.translatable("narrator.button.accessibility")));

        // ===== Dòng bản quyền, góc dưới bên phải màn hình =====
        int copyrightWidth = this.font.width(COPYRIGHT_TEXT);
        this.addRenderableWidget(new PlainTextButton(this.width - copyrightWidth - 2, this.height - 10, copyrightWidth, 10, COPYRIGHT_TEXT, btn -> ScreenUtil.open(new CreditsAndAttributionScreen(this)), this.font));
    }

    /**
     * Xử lý khi bấm "New Game": tạo một world mới và vào chơi ngay,
     * không qua màn hình CreateWorldScreen của vanilla.
     */
    private void onNewGame() {
        // Toàn bộ thân hàm nằm trong try vì findAvailableName có thể ném IOException
        // (ví dụ ổ đĩa đầy, hoặc không có quyền ghi thư mục saves).
        try {
            // BƯỚC 1: tên thư mục world, bảo đảm không trùng world có sẵn.
            // Nếu "The8Dungeons" đã tồn tại thì vanilla trả về "The8Dungeons (1)", v.v.
            // `minecraft` là field kế thừa từ Screen, chỉ dùng được sau khi màn hình đã init.
            String levelName = FileUtil.findAvailableName(minecraft.getLevelSource().getBaseDir(), // thư mục chứa các world
                    "The8Dungeons",                          // tên gốc
                    "");                                     // không đuôi, vì world là thư mục
            // BƯỚC 2: luật chơi (những thứ người chơi thường chỉnh ở màn tạo world).
            LevelSettings settings = new LevelSettings(levelName,                       // tên hiển thị của world
                    GameType.CREATIVE,               // chế độ chơi
                    false,                           // hardcore: tắt
                    Difficulty.NORMAL,             // độ khó
                    true,                            // cho phép dùng lệnh (cheats)
                    new GameRules(),                 // gamerule mặc định
                    WorldDataConfiguration.DEFAULT); // datapack mặc định

            // BƯỚC 3: seed và các cờ sinh công trình.
            WorldOptions options = new WorldOptions(RandomSource.create().nextLong(), // seed ngẫu nhiên, kiểu long
                    false,                            // không sinh công trình (làng, hầm mộ...)
                    false);                           // không tặng rương thưởng

            // BƯỚC 4: lambda mô tả "các dimension của world gồm gì".
            // Khai báo ngoài lambda (trong onNewGame, trước dòng dimensions):
            ResourceKey<WorldPreset> VOID = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(The8DungeonsMod.MODID, "void"));

            Function<RegistryAccess, WorldDimensions> dimensions = access -> {
                // 1. Registry.get(khóa) trả về WorldPreset, hoặc null nếu không có
                WorldPreset preset = access.registryOrThrow(Registries.WORLD_PRESET).get(VOID);

                // 2. null thì ném lỗi rõ ràng; lỗi hiện trong logs/latest.log
                if (preset == null) {
                    throw new IllegalStateException("Không tìm thấy world preset: " + VOID.location());
                }

                // 3. overworld() trả về Optional<LevelStem>; orElseThrow lấy LevelStem ra
                LevelStem stem = preset.overworld().orElseThrow();

                // 4. Kiểu của cả biểu thức này là WorldDimensions, khớp với kiểu ra của Function
                return WorldPresets.createNormalWorldDimensions(access).replaceOverworldGenerator(access, stem.generator());
            };

            // BƯỚC 5: giao việc cho vanilla.
            // Hàm này tự tạo thư mục, nạp datapack, ghép dimension datapack (bake) rồi vào world.
            // Lỗi bên trong chỉ được ghi vào log, nên nếu bấm mà không có gì xảy ra
            // hãy mở logs/latest.log và tìm "Failed to load datapacks".
            minecraft.createWorldOpenFlows().createFreshLevel(levelName, settings, options, dimensions);

        } catch (IOException e) {
            // BƯỚC 6: không xin được tên world. Ghi lỗi để còn tra, game vẫn ở title screen.
            // Việc báo cho người chơi (toast) để làm sau.
            LOGGER.error("Không thể tạo tên world mới", e);
        }
    }

    /**
     * Xử lý khi bấm "Continue". TODO: chưa cài đặt.
     */
    private void onContinue() {
    }

    /**
     * Tính layout theo kích thước cửa sổ hiện tại (gọi lại mỗi lần init()).
     * Panel nằm bên trái, các nút căn giữa theo chiều ngang của panel.
     */
    private Layout calculateLayout() {
        // Panel rộng 30% màn hình, giới hạn 168..280px
        int panelWidth = clamp(Math.round(width * 0.30f), 168, 280);
        // Nút hẹp hơn panel 40px (mỗi bên chừa 20px), giới hạn 120..190px
        int buttonWidth = clamp(panelWidth - 40, 120, 190);
        // X của nút: căn giữa trong panel
        int buttonX = (panelWidth - buttonWidth) / 2;

        int buttonHeight = 20; // chiều cao nút chuẩn của vanilla
        int buttonGap = 5;     // khoảng hở giữa hai nút liền nhau
        int rows = MAIN_BUTTON_COUNT;

        // Tổng chiều cao của cả nhóm nút = các nút + các khoảng hở giữa chúng
        int groupHeight = rows * buttonHeight + (rows - 1) * buttonGap;

        // Y của nút đầu tiên: nhóm nút được căn giữa quanh mốc 61% chiều cao màn hình
        int buttonTop = Math.round(height * 0.61f) - groupHeight / 2;

        return new Layout(panelWidth, buttonX, buttonWidth, buttonTop, buttonHeight, buttonGap);
    }

    /**
     * Thêm một nút ở hàng thứ `row` (bắt đầu từ 0) trong cột nút chính.
     *
     * @param row    chỉ số hàng, hàng 0 ở trên cùng
     * @param label  chữ hiển thị trên nút
     * @param action hành động khi bấm
     * @return chính nút vừa tạo (dùng để gắn thêm thứ khác như chỉ báo Mods)
     */
    private Button addMainButton(int row, Component label, Button.OnPress action) {
        // Y = đỉnh nhóm nút + số hàng × (chiều cao nút + khoảng hở)
        int y = layout.buttonTop() + row * (layout.buttonHeight() + layout.buttonGap());
        return addRenderableWidget(Button.builder(label, action).bounds(layout.buttonX(), y, layout.buttonWidth(), layout.buttonHeight()).build());
    }

    /**
     * Cố ý để trống: bỏ phần tick của vanilla (kiểm tra Realms, đồng hồ hiệu ứng)
     * vì màn hình này không dùng đến.
     */
    @Override
    public void tick() {
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Nền tạm: một màu đặc phủ kín (định dạng ARGB). Panel bên trái chưa được vẽ.
        g.fill(0, 0, width, height, 0xFF101820);
        // Không gọi super.render vì nó sẽ vẽ panorama và logo vanilla,
        // nên các widget phải tự vẽ lại ở đây.
        for (Renderable r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
        // Chấm "có bản cập nhật mod" của Forge cạnh nút Mods, vẽ sau cùng để nằm trên cùng.
        if (modUpdateNotification != null) {
            modUpdateNotification.render(g, mouseX, mouseY, partialTick);
        }
    }

    /**
     * Gói các số đo layout lại thành một khối bất biến:
     * rộng panel, X nút, rộng nút, Y nút đầu, cao nút, khoảng hở giữa các nút.
     */
    private record Layout(int panelWidth, int buttonX, int buttonWidth, int buttonTop, int buttonHeight,
                          int buttonGap) {
    }
}