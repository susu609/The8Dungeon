package net.susu609.the8dungeon.client.gui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.InventoryMenu;
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
 * Menu nằm ở "panel bên trái":
 * <ul>
 *   <li>Cột nút chính: New Game, Continue, Mods, Options.</li>
 *   <li>Hàng dưới: icon ngôn ngữ, icon trợ năng, nút Quit.</li>
 *   <li>Dòng bản quyền ở góc dưới bên phải.</li>
 * </ul>
 * Nền là lưới ô đá stone bricks ngẫu nhiên, trôi chậm xuống; logo mod rơi nhẹ vào vị trí.
 * Mod chỉ có MỘT world, tên cố định {@link #WORLD_NAME}.
 */
public class ModTitleScreen extends TitleScreen implements TitleScreenInterface {

    // ===================== 1. HẰNG SỐ =====================

    // --- Cột nút chính ---
    /**
     * Số nút trong cột nút chính (New Game, Continue, Mods, Options).
     */
    private static final int MAIN_BUTTON_COUNT = 4;

    // --- Nền đá ---
    private static final int TILE = 16;                 // kích thước gốc của một texture block (px)
    private static final int TILE_SCALE = 4;            // phóng to x4 cho dễ thấy
    private static final int TILE_DRAW = TILE * TILE_SCALE;
    private static final float SCROLL_PX_PER_SEC = 12f; // tốc độ nền trôi xuống (px/giây)

    /**
     * Pool biến thể đá; mỗi ô lưới chọn ngẫu nhiên một cái.
     */
    private static final ResourceLocation[] STONE_BRICK_TILES = {ResourceLocation.withDefaultNamespace("block/stone_bricks"), ResourceLocation.withDefaultNamespace("block/mossy_stone_bricks"), ResourceLocation.withDefaultNamespace("block/cracked_stone_bricks"), ResourceLocation.withDefaultNamespace("block/chiseled_stone_bricks")};
    // --- Logo mod ---
    private static final long LOGO_ANIM_MS = 900L;      // logo rơi vào chỗ trong 0,9 giây
    /**
     * Logo mod, file 1024x256 (tỉ lệ 4:1). Đường dẫn lấy từ MODID, không viết cứng.
     */
    private static final ResourceLocation MOD_LOGO = ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "textures/gui/title/mod_title.png");

    // --- World ---
    /**
     * Tên thư mục của world duy nhất. New Game tạo nó, Continue mở nó.
     */
    private static final String WORLD_NAME = "The8Dungeons";
    /**
     * Khóa world preset void (data/susu609/worldgen/world_preset/void.json).
     * Dùng MODID thật của mod, không phải "the8dungeons".
     */
    private static final ResourceKey<WorldPreset> VOID_PRESET = ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "void"));

    // --- Tiện ích ---
    /**
     * Ghi lỗi vào logs/latest.log.
     */
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * Instance Minecraft, lấy một lần khi lớp được nạp. An toàn vì lớp chỉ được nạp
     * ở client, lúc game đã mở màn hình tiêu đề.
     */
    private static final Minecraft mc = Minecraft.getInstance();

    // ===================== 2. TRẠNG THÁI =====================
    // Khoảng đệm giữa khung và nội dung (px)
    private static final int PANEL_PAD = 12;
    // Kích thước icon ở hàng dưới; trước đây là biến cục bộ trong init()
    private static final int ICON_SIZE = 20;
    // Logo Minecraft gốc (vanilla): vùng vẽ 256x44 trong file 256x64
    private static final ResourceLocation VANILLA_LOGO = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/minecraft.png");
    private static final int VANILLA_LOGO_W = 256;
    private static final int VANILLA_LOGO_H = 44;
    private static final int VANILLA_TEX_H = 64;
    // Logo mod đè lên đáy logo vanilla bao nhiêu px (vanilla dùng 7px ở kích thước gốc)
    private static final int TITLE_OVERLAP = 4;

    // ===================== 3. KHỞI TẠO VÀ VÒNG ĐỜI =====================
    /**
     * Thời điểm màn hình được tạo; mọi chuyển động (nền, logo) tính từ mốc này.
     */
    private final long openedAt = System.currentTimeMillis();
    /**
     * Kích thước/vị trí panel và nút. Tính lại trong init() mỗi lần resize.
     */
    private Layout layout;
    /**
     * [Forge] Chấm "có bản cập nhật mod" cạnh nút Mods.
     */
    private TitleScreenModUpdateIndicator modUpdateNotification;

    // ===================== 4. VẼ MỖI KHUNG HÌNH =====================
    /**
     * Sprite đá đã lấy từ atlas, theo đúng thứ tự STONE_BRICK_TILES.
     */
    private TextureAtlasSprite[] tileSprites;
    /**
     * Chỉ số pool của từng ô lưới, xếp theo hàng rồi cột.
     */
    private int[] tileIndex;
    /**
     * Số cột và số hàng của lưới nền (đã dư thêm để không lộ khoảng trống khi trôi).
     */
    private int cols;

    // Số hàng của lưới nền.
    private int rows;

    /**
     * Hàm khởi tạo màn hình menu chính của mod. Gọi TitleScreen(false) để tắt hiệu ứng hiện dần (fade-in) mặc định của Minecraft.
     */
    public ModTitleScreen() {
        super(false); // false = không dùng hiệu ứng fade-in của vanilla
    }

    /**
     * Đọc các hình (sprite) dùng để vẽ nền từ atlas chứa texture block. Trả về một mảng sprite cho các ô nền. LƯU Ý: dòng atlas.getSprite(...) hiện đang dùng đường dẫn logo mod thay vì STONE_BRICK_TILES[i], nên chưa lấy đúng bốn loại đá; cần kiểm tra và sửa tại chính dòng đó.
     */
    private static TextureAtlasSprite[] loadTileSprites() {
        TextureAtlas atlas = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[STONE_BRICK_TILES.length];
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = atlas.getSprite(ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "textures/gui/title/mod_title.png"));
        }
        return sprites;
    }

    // ===================== 7. LAYOUT VÀ TIỆN ÍCH =====================

    /**
     * Giữ số v trong khoảng từ min đến max: nhỏ hơn min thì trả về min, lớn hơn max thì trả về max, còn lại giữ nguyên. Dùng để giới hạn kích thước panel và nút.
     */
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Được Minecraft gọi định kỳ để cập nhật màn hình. Hàm để trống nhằm bỏ các cập nhật mặc định của TitleScreen, ví dụ phần Realms.
     */
    @Override
    public void tick() {
    }

    /**
     * Được gọi khi mở menu hoặc thay đổi kích thước cửa sổ. Tạo lại lưới nền, tính vị trí giao diện, thêm các nút New Game/Continue/Mods/Options, nút ngôn ngữ/trợ năng/Quit và dòng bản quyền. Không gọi super.init() vì sẽ tạo thêm các nút menu vanilla.
     */
    @Override
    public void init() {
        // Không gọi super.init(): vanilla sẽ thêm bộ nút gốc và đè lên menu này.

        // Bước 1: nạp sprite đá và dựng lưới ngẫu nhiên
        tileSprites = loadTileSprites();
        cols = width / TILE_DRAW + 2;
        rows = height / TILE_DRAW + 2;
        tileIndex = new int[cols * rows];
        RandomSource random = RandomSource.create();
        for (int i = 0; i < tileIndex.length; i++) {
            tileIndex[i] = random.nextInt(tileSprites.length);
        }

        // Bước 2: tính layout theo kích thước hiện tại
        layout = calculateLayout();

        // Bước 3: cột nút chính (hàng 0..MAIN_BUTTON_COUNT-1)
        addMainButton(0, Component.literal("New Game"), b -> onNewGame());

        // Continue chỉ bấm được khi world đã tồn tại. init() chạy lại mỗi lần quay về
        // màn hình tiêu đề nên trạng thái này luôn mới.
        Button continueButton = addMainButton(1, Component.literal("Continue"), b -> onContinue());
        continueButton.active = mc.getLevelSource().levelExists(WORLD_NAME);

        // [Forge] Nút Mods: giữ kết quả để gắn chấm cập nhật.
        Button modButton = addMainButton(2, Component.translatable("fml.menu.mods"), b -> ScreenUtil.open(new ModListScreen(this)));
        this.modUpdateNotification = TitleScreenModUpdateIndicator.init(this, modButton);

        addMainButton(3, Component.translatable("menu.options"), b -> ScreenUtil.open(new OptionsScreen(this, mc.options)));

        // Bước 4: hàng dưới [ngôn ngữ] [trợ năng] [Quit]
        int iconSize = 20;
        int iconGap = 4;
        // Đáy cột nút, đã gồm khoảng hở sau nút cuối
        int iconY = layout.buttonTop() + MAIN_BUTTON_COUNT * (layout.buttonHeight() + layout.buttonGap());
        int languageX = layout.buttonX();
        int accessibilityX = languageX + iconSize + iconGap;
        int quitX = accessibilityX + iconSize + iconGap;
        int quitWidth = layout.buttonX() + layout.buttonWidth() - quitX; // Quit chiếm phần còn lại

        this.addRenderableWidget(new ImageButton(languageX, iconY, iconSize, iconSize, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, btn -> ScreenUtil.open(new LanguageSelectScreen(this, mc.options, mc.getLanguageManager())), Component.translatable("narrator.button.language")));

        this.addRenderableWidget(new ImageButton(accessibilityX, iconY, iconSize, iconSize, 0, 0, 20, Button.ACCESSIBILITY_TEXTURE, 32, 64, btn -> ScreenUtil.open(new AccessibilityOptionsScreen(this, mc.options)), Component.translatable("narrator.button.accessibility")));

        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), b -> ScreenUtil.quit()).bounds(quitX, iconY, quitWidth, iconSize).build());

        // Bước 5: dòng bản quyền góc dưới bên phải
        int copyrightWidth = this.font.width(COPYRIGHT_TEXT);
        this.addRenderableWidget(new PlainTextButton(this.width - copyrightWidth - 2, this.height - 10, copyrightWidth, 10, COPYRIGHT_TEXT, btn -> ScreenUtil.open(new CreditsAndAttributionScreen(this)), this.font));
    }

    /**
     * Vẽ một khung hình của menu. Thứ tự: nền đá -> panel tối -> hai logo -> các nút -> thông báo cập nhật mod. g là đối tượng vẽ; mouseX/mouseY là vị trí chuột; partialTick là phần thời gian giữa các tick.
     */
    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderStoneBackground(g);   // 1. nền đá trôi
        renderPanel(g);
        renderTitle(g);// 2. title rơi vào chỗ
        // 3. widget (không gọi super.render vì nó vẽ panorama và logo vanilla)
        for (Renderable r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // 4. chấm cập nhật của Forge, vẽ sau cùng để nằm trên cùng
        if (modUpdateNotification != null) {
            modUpdateNotification.render(g, mouseX, mouseY, partialTick);
        }
    }

    // ===================== 8. KIỂU DỮ LIỆU =====================

    /**
     * Vẽ các ô đá phủ màn hình và cho chúng trôi xuống theo thời gian. shiftPx là quãng đường đã trôi, wraps đếm số ô đã đi qua, offsetY là độ lệch còn lại. Dùng floorMod để chọn hàng sprite hợp lệ khi lặp nền.
     */
    private void renderStoneBackground(GuiGraphics g) {
        float elapsedMs = System.currentTimeMillis() - openedAt;
        int shiftPx = (int) (elapsedMs / 1000f * SCROLL_PX_PER_SEC); // tổng số px đã trôi
        int wraps = shiftPx / TILE_DRAW;    // số lần trôi đúng một ô
        int offsetY = shiftPx % TILE_DRAW;  // phần lẻ trong ô hiện tại

        for (int row = 0; row < rows; row++) {
            // Hàng nguồn lệch theo số vòng đã trôi, dùng floorMod để không ra chỉ số âm
            int srcRow = Math.floorMod(row - wraps, rows);
            for (int col = 0; col < cols; col++) {
                int sprite = tileIndex[srcRow * cols + col];
                int x = col * TILE_DRAW;
                int y = row * TILE_DRAW + offsetY - TILE_DRAW; // bắt đầu lệch lên một ô
                g.blit(x, y, 0, TILE_DRAW, TILE_DRAW, tileSprites[sprite]);
            }
        }
    }

    /**
     * Xử lý khi nhấn New Game. Nếu chưa có world The8Dungeons thì tạo ngay. Nếu đã có, mở hộp thoại xác nhận; chọn Đồng ý sẽ xóa world cũ rồi tạo lại, chọn Hủy sẽ quay về menu. Xóa world làm mất dữ liệu cũ.
     */
    private void onNewGame() {
        if (!mc.getLevelSource().levelExists(WORLD_NAME)) {
            createWorld();
            return;
        }
        // Xóa là mất toàn bộ tiến trình, nên hỏi trước. Lambda nhận true (có) hoặc false (không).
        mc.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    deleteOldWorld();
                    createWorld();
                } catch (IOException e) {
                    LOGGER.error("Không xóa được world cũ", e);
                    mc.setScreen(this);
                }
            } else {
                mc.setScreen(this);
            }
        }, Component.literal("Bắt đầu lại?"), Component.literal("World \"" + WORLD_NAME + "\" hiện tại sẽ bị xóa vĩnh viễn.")));
    }

    /**
     * Xử lý khi nhấn Continue. Kiểm tra world The8Dungeons có tồn tại không; nếu có thì mở world đó. Nếu không có thì không làm gì.
     */
    private void onContinue() {
        if (mc.getLevelSource().levelExists(WORLD_NAME)) {
            // Tham số đầu: màn hình quay về nếu mở thất bại; tham số sau: tên thư mục world.
            mc.createWorldOpenFlows().loadLevel(this, WORLD_NAME);
        }
    }

    /**
     * Tạo world mới với tên cố định The8Dungeons và tự vào chơi. Cài Creative, độ khó Normal, bật lệnh, seed ngẫu nhiên; lấy bộ sinh Overworld từ world preset void của mod và giữ cấu hình dimension thông thường cho các dimension khác. Nếu thiếu preset, ném IllegalStateException.
     */
    private void createWorld() {
        // Luật chơi
        LevelSettings settings = new LevelSettings(WORLD_NAME,                      // tên hiển thị
                GameType.CREATIVE,               // chế độ chơi
                false,                           // hardcore: tắt
                Difficulty.NORMAL,               // độ khó
                true,                            // cho phép lệnh
                new GameRules(),                 // gamerule mặc định
                WorldDataConfiguration.DEFAULT); // datapack mặc định

        // Seed ngẫu nhiên; không sinh công trình; không tặng rương
        WorldOptions options = new WorldOptions(RandomSource.create().nextLong(), false, false);

        // Mô tả các dimension: lấy preset void, thay bộ sinh của Overworld
        Function<RegistryAccess, WorldDimensions> dimensions = access -> {
            WorldPreset preset = access.registryOrThrow(Registries.WORLD_PRESET).get(VOID_PRESET);
            if (preset == null) {
                throw new IllegalStateException("Không tìm thấy world preset: " + VOID_PRESET.location());
            }
            LevelStem stem = preset.overworld().orElseThrow();
            return WorldPresets.createNormalWorldDimensions(access).replaceOverworldGenerator(access, stem.generator());
        };

        // Tạo thư mục, nạp datapack, rồi vào world
        mc.createWorldOpenFlows().createFreshLevel(WORLD_NAME, settings, options, dimensions);
    }

    /**
     * Xóa world có tên The8Dungeons trong thư mục saves. Dùng try-with-resources để tự giải phóng khóa truy cập sau khi xóa. Có thể ném IOException nếu xóa thất bại.
     */
    private void deleteOldWorld() throws IOException {
        // try-with-resources: tự đóng access (giải phóng khóa thư mục) kể cả khi lỗi
        try (LevelStorageSource.LevelStorageAccess access = mc.getLevelSource().createAccess(WORLD_NAME)) {
            access.deleteLevel();
        }
    }

    /**
     * Tính kích thước và vị trí các nút dựa trên chiều rộng/chiều cao cửa sổ. Panel rộng khoảng 30% màn hình (giới hạn 168–280 px); nút được căn giữa trong panel. Trả về Layout chứa các số đo để các hàm vẽ dùng chung.
     */
    private Layout calculateLayout() {
        int panelWidth = clamp(Math.round(width * 0.30f), 168, 280);   // panel 30% màn hình
        int buttonWidth = clamp(panelWidth - 40, 120, 190);            // nút hẹp hơn panel 40px
        int buttonX = (panelWidth - buttonWidth) / 2;                  // căn giữa trong panel

        int buttonHeight = 20;  // chiều cao nút chuẩn vanilla
        int buttonGap = 4;      // khoảng hở giữa hai nút
        int buttonRows = MAIN_BUTTON_COUNT;
        int groupHeight = buttonRows * buttonHeight + (buttonRows - 1) * buttonGap;
        int buttonTop = Math.round(height * 0.61f) - groupHeight / 2;  // nhóm nút quanh mốc 61%

        return new Layout(panelWidth, buttonX, buttonWidth, buttonTop, buttonHeight, buttonGap);
    }

    /**
     * Tạo một nút trong cột nút chính. row là số thứ tự hàng (bắt đầu từ 0), label là chữ trên nút, action là hành động khi bấm. Trả về Button để có thể bật/tắt nút hoặc gắn thông báo.
     */
    private Button addMainButton(int row, Component label, Button.OnPress action) {
        int y = layout.buttonTop() + row * (layout.buttonHeight() + layout.buttonGap());
        return addRenderableWidget(Button.builder(label, action).bounds(layout.buttonX(), y, layout.buttonWidth(), layout.buttonHeight()).build());
    }

    /**
     * Tính bốn tọa độ của khung nền tối bao quanh logo và các nút. Lấy vị trí nút, vị trí logo và khoảng đệm PANEL_PAD để trả về Box(x0, y0, x1, y1).
     */
    private Box panelBox() {
        // Cạnh trái/phải: bám theo cột nút, chừa đệm 12px mỗi bên
        int x0 = layout.buttonX() - PANEL_PAD;
        int x1 = layout.buttonX() + layout.buttonWidth() + PANEL_PAD;

        // Cạnh trên: đỉnh của khối tiêu đề (đã tính sẵn), trừ đệm
        int y0 = titleTop() - PANEL_PAD;

        // Cạnh dưới: đáy hàng icon, cộng đệm
        // Hàng icon nằm ngay sau 4 nút chính, giống công thức iconY trong init()
        int iconY = layout.buttonTop() + MAIN_BUTTON_COUNT * (layout.buttonHeight() + layout.buttonGap());
        int y1 = iconY + ICON_SIZE + PANEL_PAD;

        return new Box(x0, y0, x1, y1);
    }

    /**
     * Vẽ khung panel phía sau logo và các nút: nền đen bán trong suốt và viền trắng mờ. Gọi panelBox() để lấy tọa độ khung.
     */
    private void renderPanel(GuiGraphics g) {
        Box b = panelBox();

        // Đen với alpha 0x80 = 128/255 ≈ 50% độ mờ
        g.fill(b.x0(), b.y0(), b.x1(), b.y1(), 0x80000000);

        // Viền trắng mờ (~25%). renderOutline nhận (x, y, rộng, cao), không nhận góc phải dưới
        g.renderOutline(b.x0(), b.y0(), b.x1() - b.x0(), b.y1() - b.y0(), 0x40FFFFFF);
    }

    /**
     * Tính chiều cao logo Minecraft gốc theo chiều rộng nút, giữ nguyên tỉ lệ hình 256:44.
     */
    private int vanillaLogoHeight() {
        return layout.buttonWidth() * VANILLA_LOGO_H / VANILLA_LOGO_W;
    }

    /**
     * Tính chiều cao logo The8Dungeon theo tỉ lệ 4:1 (chiều cao bằng một phần tư chiều rộng).
     */
    private int modLogoHeight() {
        return layout.buttonWidth() / 4;
    }

    /**
     * Tính tổng chiều cao của hai logo sau khi trừ phần chúng chồng lên nhau (TITLE_OVERLAP).
     */
    private int titleHeight() {
        return vanillaLogoHeight() + modLogoHeight() - TITLE_OVERLAP;
    }

    /**
     * Tính tọa độ Y của mép trên cụm logo khi animation đã kết thúc, đặt phía trên cột nút và cách 12 px.
     */
    private int titleTop() {
        return layout.buttonTop() - titleHeight() - 12;
    }

    /**
     * Vẽ logo Minecraft và logo mod. Trong LOGO_ANIM_MS mili giây đầu, cụm logo dịch dần về vị trí chuẩn theo đường cong ease-out; sau đó đứng yên. Logo mod nằm chồng lên phần đáy logo Minecraft.
     */
    private void renderTitle(GuiGraphics g) {
        // Chuyển động rơi nhẹ (giống renderModLogo cũ)
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / (float) LOGO_ANIM_MS);
        float ease = 1f - (1f - t) * (1f - t) * (1f - t);
        int drop = Math.round((1f - ease) * 30f);

        int x = layout.buttonX();
        int w = layout.buttonWidth();
        int top = titleTop() - drop;
        int vH = vanillaLogoHeight();

        // 1. Logo Minecraft: co theo chiều rộng nút
        g.blit(VANILLA_LOGO, x, top, w, vH, 0, 0, VANILLA_LOGO_W, VANILLA_LOGO_H, VANILLA_LOGO_W, VANILLA_TEX_H);

        // 2. Logo mod: đặt ở vị trí của Edition, chồng lên đáy logo vanilla
        g.blit(MOD_LOGO, x, top + vH - TITLE_OVERLAP, w, modLogoHeight(), 0, 0, 1024, 256, 1024, 256);
    }

    /**
     * Gói các số đo layout thành một khối bất biến.
     * panelWidth: rộng panel; buttonX / buttonWidth: vị trí và rộng nút;
     * buttonTop: Y của nút đầu; buttonHeight / buttonGap: cao nút và khoảng hở.
     */
    private record Layout(int panelWidth, int buttonX, int buttonWidth, int buttonTop, int buttonHeight,
                          int buttonGap) {
    }

    // Record nhỏ gói 4 số của khung, thay vì trả về 4 biến rời rạc
    private record Box(int x0, int y0, int x1, int y1) {
    }
}