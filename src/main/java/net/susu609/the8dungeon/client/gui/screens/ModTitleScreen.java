package net.susu609.the8dungeon.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
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
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;

import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.client.gui.ScreenUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Function;

/**
 * Màn hình tiêu đề riêng của The8Dungeons, kế thừa TitleScreen của vanilla.
 * Thay vì dùng layout vanilla (nút căn giữa màn hình), lớp này dựng menu
 * dạng "panel bên trái":
 * <ul>
 *   <li>Cột nút chính: New Game, Continue, Mods, Options.</li>
 *   <li>Hàng dưới cùng: icon ngôn ngữ, icon trợ năng, nút Quit.</li>
 *   <li>Dòng bản quyền ở góc dưới bên phải màn hình.</li>
 * </ul>
 * Mod chỉ có MỘT world, tên cố định {@link #WORLD_NAME}: New Game xóa world cũ
 * (sau khi hỏi xác nhận) rồi tạo world void mới, Continue mở lại đúng world đó.
 */
public class ModTitleScreen extends TitleScreen implements TitleScreenInterface {
    // ===== Hằng số =====

    /** Số nút trong cột nút chính (New Game, Continue, Mods, Options). */
    private static final int MAIN_BUTTON_COUNT = 4;

    /** Tên thư mục của world duy nhất. New Game tạo nó, Continue mở nó. */
    private static final String WORLD_NAME = "The8Dungeons";

    /**
     * Khóa của world preset void (file
     * data/susu609/worldgen/world_preset/void.json).
     * Dùng MODID thật của mod, không phải "the8dungeons".
     */
    private static final ResourceKey<WorldPreset> VOID_PRESET =
            ResourceKey.create(
                    Registries.WORLD_PRESET,
                    ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "void"));

    /** Ghi lỗi vào logs/latest.log. */
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Instance Minecraft, lấy một lần khi lớp được nạp. An toàn vì lớp này chỉ
     * được nạp ở client, lúc game đã mở màn hình tiêu đề.
     */
    private static final Minecraft mc = Minecraft.getInstance();
    // ===== Trạng thái =====

    /**
     * Kích thước/vị trí của panel và nút. Tính trong init(), dùng lại cho mọi
     * nút.
     */
    private Layout layout;

    /** [Forge] Chỉ báo "có bản cập nhật mod" (dấu chấm nhỏ cạnh nút Mods). */
    private TitleScreenModUpdateIndicator modUpdateNotification;

    public ModTitleScreen() {
        super(false); // false = không dùng hiệu ứng fade-in của vanilla
    }
    // ===== Vòng đời màn hình =====
    /**
     * Cố ý để trống: bỏ phần tick của vanilla (kiểm tra Realms, đồng hồ hiệu
     * ứng) vì màn hình này không dùng đến.
     */
    @Override public void tick() {
    }
    /**
     * Được gọi khi màn hình mở hoặc khi cửa sổ đổi kích thước.
     * Vanilla xóa sạch widget cũ trước khi gọi, nên ở đây phải thêm lại toàn
     * bộ.
     */
    @Override
    public void init() {
        // Không gọi super.init(): vanilla sẽ thêm bộ nút gốc và đè lên menu tự chế này.
        // Tính layout theo kích thước cửa sổ hiện tại (init chạy lại mỗi khi
        // resize).
        layout = calculateLayout();
        // ===== Cột nút chính (hàng 0..3, khớp MAIN_BUTTON_COUNT) =====
        addMainButton(0, Component.literal("New Game"), b -> onNewGame());
        // Continue chỉ bấm được khi world đã tồn tại. init() chạy lại mỗi
        // lần quay về
        // màn hình tiêu đề nên trạng thái này luôn mới.
        Button continueButton = addMainButton(
                1, Component.literal("Continue"),
                b -> onContinue());
        continueButton.active = mc.getLevelSource().levelExists(WORLD_NAME);
        // [Forge] Nút Mods: hứng kết quả trả về để gắn chỉ báo cập nhật.
        Button modButton =
                addMainButton(
                        2, Component.translatable("fml.menu.mods"),
                        b -> ScreenUtil.open(new ModListScreen(this)));
        this.modUpdateNotification = TitleScreenModUpdateIndicator.init(
                this, modButton);
        addMainButton(
                3, Component.translatable("menu.options"),
                b -> ScreenUtil.open(new OptionsScreen(this, mc.options)));
        // ===== Hàng dưới: [ngôn ngữ] [trợ năng] [Quit] =====
        int iconSize = 20;
        int iconGap = 4;
        // Đáy cột nút = buttonTop + MAIN_BUTTON_COUNT * (cao + hở). Số này
        // đã gồm sẵn
        // một khoảng hở dưới nút cuối.
        int iconY = layout.buttonTop() + MAIN_BUTTON_COUNT * (layout.buttonHeight() + layout.buttonGap());
        int languageX = layout.buttonX();
        int accessibilityX = languageX + iconSize + iconGap;
        int quitX = accessibilityX + iconSize + iconGap;
        // Quit chiếm phần còn lại đến mép phải của cột nút.
        int quitWidth = layout.buttonX() + layout.buttonWidth() - quitX;
        // Icon ngôn ngữ: sát mép trái của cột nút.
        this.addRenderableWidget(new ImageButton(
                languageX, iconY, iconSize, iconSize, 0, 106, 20,
                Button.WIDGETS_LOCATION, 256, 256, btn -> ScreenUtil.open(
                new LanguageSelectScreen(
                        this, mc.options,
                        mc.getLanguageManager())),
                Component.translatable("narrator.button.language")));
        // Icon trợ năng: ngay cạnh icon ngôn ngữ.
        this.addRenderableWidget(new ImageButton(
                accessibilityX, iconY, iconSize, iconSize, 0, 0, 20,
                Button.ACCESSIBILITY_TEXTURE, 32, 64, btn -> ScreenUtil.open(
                new AccessibilityOptionsScreen(this, mc.options)),
                Component.translatable("narrator.button.accessibility")));
        // Nút Quit: phần còn lại của hàng.
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("menu.quit"),
                                b -> ScreenUtil.quit())
                        .bounds(quitX, iconY, quitWidth, iconSize).build());

        // ===== Dòng bản quyền, góc dưới bên phải màn hình =====
        int copyrightWidth = this.font.width(COPYRIGHT_TEXT);
        this.addRenderableWidget(new PlainTextButton(
                this.width - copyrightWidth - 2, this.height - 10,
                copyrightWidth, 10, COPYRIGHT_TEXT,
                btn -> ScreenUtil.open(new CreditsAndAttributionScreen(this))
                , this.font));
    }
    /**
     * Vẽ màn hình mỗi khung hình. Ghi đè hoàn toàn render của vanilla, nên
     * không có panorama, logo Minecraft hay splash text.
     */
    @Override public void render(@NotNull GuiGraphics g, int mouseX, int mouseY,
            float partialTick)
    {
        // Nền tạm: một màu đặc phủ kín (định dạng ARGB). Panel bên trái chưa
        // được vẽ.
        g.fill(0, 0, width, height, 0xFF101820);
        // Không gọi super.render vì nó sẽ vẽ panorama và logo vanilla,
        // nên các widget phải tự vẽ lại ở đây.
        for (Renderable r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
        // Chấm "có bản cập nhật mod" của Forge cạnh nút Mods, vẽ sau cùng để
        // nằm trên cùng.
        if (modUpdateNotification != null) {
            modUpdateNotification.render(g, mouseX, mouseY, partialTick);
        }
    }
    // ===== Hành động của nút =====

    /**
     * Xử lý khi bấm "New Game". Nếu world cũ đã tồn tại thì hỏi xác nhận,
     * xóa nó rồi
     * tạo lại; nếu chưa có thì tạo luôn. Không qua CreateWorldScreen của
     * vanilla.
     */
    private void onNewGame() {
        // Chưa có world cũ thì tạo luôn, không cần hỏi.
        if (!mc.getLevelSource().levelExists(WORLD_NAME)) {
            createWorld();
            return;
        }
        // Có world cũ thì hỏi trước, vì xóa là mất toàn bộ tiến trình chơi.
        // Lambda nhận một boolean: người chơi chọn "có" (true) hay "không"
        // (false).
        mc.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        try {
                            deleteOldWorld();
                            createWorld();
                        } catch (IOException e) {
                            LOGGER.error("Không xóa được world cũ", e);
                            mc.setScreen(this); // quay lại màn hình tiêu đề
                        }
                    } else {
                        mc.setScreen(
                                this); // người chơi hủy, quay lại màn hình
                        // tiêu đề
                    }
                }, Component.literal("Bắt đầu lại?"), Component.literal(
                "World \"" + WORLD_NAME + "\" hiện tại sẽ bị xóa vĩnh viễn.")));
    }
    /**
     * Tạo world mới tên {@link #WORLD_NAME} rồi vào chơi ngay. Overworld dùng
     * preset void; các dimension khác (kể cả dimension datapack của mod) giữ
     * như bình thường. Lỗi bên trong createFreshLevel chỉ được ghi vào log,
     * không hiện trên màn hình: nếu bấm mà không có gì xảy ra, mở
     * logs/latest.log và tìm "Failed to load datapacks".
     */
    private void createWorld() {
        // Luật chơi (những thứ người chơi thường chỉnh ở màn tạo world).
        LevelSettings settings = new LevelSettings(
                WORLD_NAME,                      // tên hiển thị của world
                GameType.CREATIVE,               // chế độ chơi
                false,                           // hardcore: tắt
                Difficulty.NORMAL,               // độ khó
                true,                            // cho phép dùng lệnh (cheats)
                new GameRules(),                 // gamerule mặc định
                WorldDataConfiguration.DEFAULT); // datapack mặc định
        // Seed ngẫu nhiên (kiểu long); không sinh công trình; không tặng
        // rương thưởng.
        WorldOptions options =
                new WorldOptions(
                        RandomSource.create().nextLong(), false,
                        false);
        // Lambda mô tả "các dimension của world gồm gì". createFreshLevel
        // gọi nó
        // (bằng .apply) và đưa vào `access`; ta chỉ trả về WorldDimensions.
        Function<RegistryAccess, WorldDimensions> dimensions = access -> {
            // 1. Registry.get(khóa) trả về WorldPreset, hoặc null nếu không có.
            WorldPreset preset = access.registryOrThrow(Registries.WORLD_PRESET)
                    .get(VOID_PRESET);
            // 2. Không có thì ném lỗi rõ ràng; lỗi hiện trong logs/latest.log.
            if (preset == null) {
                throw new IllegalStateException(
                        "Không tìm thấy world preset: " +
                                VOID_PRESET.location());
            }
            // 3. overworld() trả về Optional<LevelStem>; orElseThrow lấy
            // LevelStem ra.
            LevelStem stem = preset.overworld().orElseThrow();
            // 4. Giữ dimension bình thường, chỉ thay bộ sinh của Overworld
            // bằng void.
            return WorldPresets.createNormalWorldDimensions(access)
                    .replaceOverworldGenerator(access, stem.generator());
        };
        // Tự tạo thư mục, nạp datapack, ghép dimension datapack (bake) rồi
        // vào world.
        mc.createWorldOpenFlows()
                .createFreshLevel(WORLD_NAME, settings, options, dimensions);
    }
    // ===== Tạo và xóa world =====
    /**
     * Xóa thư mục world cũ. CHỈ xóa {@link #WORLD_NAME}, không bao giờ đụng
     * world khác.
     * Ném IOException nếu không xóa được (ví dụ world đang bị khóa).
     */
    private void deleteOldWorld() throws IOException {
        // try-with-resources: tự đóng `access` (giải phóng khóa thư mục) kể
        // cả khi có lỗi.
        try (LevelStorageSource.LevelStorageAccess access = mc.getLevelSource()
                .createAccess(WORLD_NAME))
        {
            access.deleteLevel();
        }
    }
    /**
     * Xử lý khi bấm "Continue": mở world có tên {@link #WORLD_NAME}. Nút này đã
     * bị làm mờ trong init() khi world chưa tồn tại; kiểm tra lại ở đây để an
     * toàn.
     */
    private void onContinue() {
        if (mc.getLevelSource().levelExists(WORLD_NAME)) {
            // Tham số đầu: màn hình quay về nếu mở thất bại; tham số sau:
            // tên thư mục world.
            mc.createWorldOpenFlows().loadLevel(this, WORLD_NAME);
        }
    }
    // ===== Layout =====
    /**
     * Tính layout theo kích thước cửa sổ hiện tại (gọi lại mỗi lần init()).
     * Panel nằm bên trái, các nút căn giữa theo chiều ngang của panel.
     */
    private Layout calculateLayout() {
        // Panel rộng 30% màn hình, giới hạn 168..280px.
        int panelWidth = clamp(Math.round(width * 0.30f), 168, 280);
        // Nút hẹp hơn panel 40px (mỗi bên chừa 20px), giới hạn 120..190px.
        int buttonWidth = clamp(panelWidth - 40, 120, 190);
        // X của nút: căn giữa trong panel.
        int buttonX = (panelWidth - buttonWidth) / 2;

        int buttonHeight = 20; // chiều cao nút chuẩn của vanilla
        int buttonGap = 4;     // khoảng hở giữa hai nút liền nhau
        int rows = MAIN_BUTTON_COUNT;
        // Tổng chiều cao của cả nhóm nút = các nút + các khoảng hở giữa chúng.
        int groupHeight = rows * buttonHeight + (rows - 1) * buttonGap;
        // Y của nút đầu tiên: nhóm nút được căn giữa quanh mốc 61% chiều cao
        // màn hình.
        int buttonTop = Math.round(height * 0.61f) - groupHeight / 2;

        return new Layout(panelWidth, buttonX, buttonWidth, buttonTop, buttonHeight, buttonGap);
    }
    /** Giới hạn giá trị v trong khoảng [min, max]. */
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
    /**
     * Thêm một nút ở hàng thứ `row` (bắt đầu từ 0) trong cột nút chính.
     *
     * @param row    chỉ số hàng, hàng 0 ở trên cùng
     * @param label  chữ hiển thị trên nút
     * @param action hành động khi bấm
     * @return chính nút vừa tạo (dùng để gắn thêm thứ khác như chỉ báo Mods
     * hoặc làm mờ nút)
     */
    private Button addMainButton(int row, Component label,
            Button.OnPress action)
    {
        // Y = đỉnh nhóm nút + số hàng × (chiều cao nút + khoảng hở).
        int y = layout.buttonTop() + row * (layout.buttonHeight() +
                layout.buttonGap());
        return addRenderableWidget(Button.builder(label, action)
                .bounds(
                        layout.buttonX(), y, layout.buttonWidth(),
                        layout.buttonHeight()).build());
    }

    /**
     * Gói các số đo layout lại thành một khối bất biến:
     * rộng panel, X nút, rộng nút, Y nút đầu, cao nút, khoảng hở giữa các nút.
     */
    private record Layout(int panelWidth, int buttonX, int buttonWidth, int buttonTop, int buttonHeight,
                          int buttonGap) {
    }
}