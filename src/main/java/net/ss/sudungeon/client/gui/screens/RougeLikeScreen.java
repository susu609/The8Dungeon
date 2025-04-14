package net.ss.sudungeon.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.components.ModLogoRenderer;
import net.ss.sudungeon.util.BaseScreen;
import net.ss.sudungeon.util.GuiUtils;
import net.ss.sudungeon.util.Log;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class RougeLikeScreen extends BaseScreen {

    // Tên thế giới Rougelike (cụ thể từ phiên bản mặc định của bạn)
    public static final String WORLD_NAME = "RougeDungeon";
    private final ModLogoRenderer modlogorenderer;
    private final boolean fading;
    private long fadeInStart;
    private static final int FADE_IN_DURATION = 2000; // 2 giây
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(SsMod.MODID, "textures/gui/background.png");
    public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
    int scaleX = 1920;
    int scaleY = 960;
    private float scrollOffset = 0;
    private final float backgroundScale = 2.0f;

    public RougeLikeScreen () {
        this(false, null); // Hỗ trợ nullable
    }

    public RougeLikeScreen (boolean fading) {
        this(fading, null);
    }

    public RougeLikeScreen (boolean fading, @Nullable ModLogoRenderer modlogorenderer) {
        super(Component.literal("screen.rougelike.title")); // Gọi constructor lớp cha
        this.fading = fading; // Gán giá trị fading
        this.modlogorenderer = Objects.requireNonNullElseGet(modlogorenderer, () -> new ModLogoRenderer(fading)); // Fallback logic
    }

    @Override
    protected void init () {
        // Xóa tất cả các nút cũ
        clearAllButtons();

        // Tính toán vị trí và kích thước cơ bản
        int i = this.font.width(COPYRIGHT_TEXT);
        int j = this.width - i - 2;
        int centerX = this.width / 2; // Vị trí tâm ngang
        int startY = this.height / 4 + 58; // Vị trí dọc cho hàng đầu tiên
        int buttonWidthFull = 200; // Chiều rộng nút thông thường
        int buttonWidthHalf = 100; // Chiều rộng của nút nhỏ (49)
        int buttonHeight = 20; // Chiều cao cố định
        int spacing = 4; // Khoảng cách giữa các nút

        // Kiểm tra nếu bản đồ "RougeDungeon" đã tồn tại
        boolean worldExists = mc.getLevelSource().levelExists(WORLD_NAME);

        // Nếu bản đồ tồn tại, hiển thị 2 nút nhỏ (Continue và Abandon)
        if (worldExists) {
            // Nút "Continue" (nằm bên trái)
            addButtonWithBounds(
                    centerX - buttonWidthHalf - 1, // Đặt bên trái tâm
                    startY,
                    buttonWidthHalf,
                    buttonHeight,
                    Component.translatable("menu.continue"),
                    button -> this.startTransitionToContinueGame()
            );

            // Nút "Abandon" (nằm bên phải)
            addButtonWithBounds(
                    centerX + 1, // Đặt bên phải tâm
                    startY,
                    buttonWidthHalf,
                    buttonHeight,
                    Component.translatable("menu.abandon"),
                    button -> mc.execute(() -> {
                        try (var session = mc.getLevelSource().createAccess(WORLD_NAME)) {
                            session.deleteLevel();
                            Log.i("World " + WORLD_NAME + " deleted successfully.");
                        } catch (IOException e) {
                            Log.e("Failed to delete world: " + e.getMessage(), e);
                        }
                        mc.setScreen(new RougeLikeScreen());
                    })
            );
        } else {
            // Nếu không có bản đồ, hiển thị nút "New Game" ở giữa
            addButtonWithBounds(
                    centerX - buttonWidthFull / 2,
                    startY,
                    buttonWidthFull,
                    buttonHeight,
                    Component.translatable("menu.newgame"),
                    button -> this.startTransitionToNewGame()
            );
        }

        // Căn chỉnh các nút phía dưới dòng đầu tiên
        int offsetY = startY + buttonHeight + spacing;

        // Nút "Multiplayer"
        addButtonWithBounds(
                centerX - buttonWidthFull / 2,
                offsetY,
                buttonWidthFull,
                buttonHeight,
                Component.translatable("menu.multiplayer"),
                button -> mc.setScreen(new JoinMultiplayerScreen(this))
        );

        // Nút "Options"
        addButtonWithBounds(
                centerX - buttonWidthFull / 2,
                offsetY + buttonHeight + spacing,
                buttonWidthFull,
                buttonHeight,
                Component.translatable("menu.options"),
                button -> mc.setScreen(new OptionsScreen(this, mc.options))
        );

        // Nút "Quit"
        addButtonWithBounds(
                centerX - buttonWidthFull / 2,
                offsetY + 2 * (buttonHeight + spacing),
                buttonWidthFull,
                buttonHeight,
                Component.translatable("menu.quit"),
                button -> mc.stop()
        );

        // Nút "Language" (nút nhỏ bên trái dòng cuối)
        this.addRenderableWidget(new ImageButton(
                centerX - buttonWidthFull / 2 - 24, // Căn trái
                offsetY + 2 * (buttonHeight + spacing),
                20, 20, 0, 106, 20,
                Button.WIDGETS_LOCATION,
                256, 256,
                button -> mc.setScreen(new LanguageSelectScreen(this, mc.options, mc.getLanguageManager())),
                Component.translatable("narrator.button.language")
        ));

        // Nút "Accessibility" (nút nhỏ bên phải dòng cuối)
        this.addRenderableWidget(new ImageButton(
                centerX + buttonWidthFull / 2 + 4, // Căn phải
                offsetY + 2 * (buttonHeight + spacing),
                20, 20, 0, 0, 20,
                Button.ACCESSIBILITY_TEXTURE,
                32, 64,
                button -> mc.setScreen(new AccessibilityOptionsScreen(this, mc.options)),
                Component.translatable("narrator.button.accessibility")
        ));
        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, (p_280834_) -> mc.setScreen(new CreditsAndAttributionScreen(this)), this.font));
    }

    private void startTransitionToNewGame () {
        Log.i("Transition to New Game complete.");
        CreateWorldScreen.openFresh(mc, null);

    }

    private void startTransitionToContinueGame () {
        Log.i("Transition to Customize Toolbar… Game complete.");
        mc.createWorldOpenFlows().loadLevel(this, WORLD_NAME);

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Hiệu ứng fade-in
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        // Tính toán alpha cho hiệu ứng fade-in
        float alpha = this.fading ? calculateAlpha(Util.getMillis() - this.fadeInStart) : 1.0F;

        // Cập nhật scrollOffset để tính toán vị trí cuộn
        this.scrollOffset += partialTicks / backgroundScale; // Tốc độ giảm theo tỷ lệ scale
        if (this.scrollOffset >= scaleY * backgroundScale) {
            this.scrollOffset %= (scaleY * backgroundScale); // Reset giá trị cuộn
        }

        // Vẽ ảnh nền
        GuiUtils.renderMovingBackground(guiGraphics, BACKGROUND_TEXTURE, (int) scrollOffset, backgroundScale, this.width, this.height, this.width, this.height);
        // Vẽ hiệu ứng gradient fade-in
        GuiUtils.drawTopToBottomGradient(guiGraphics, this.width, this.height, alpha);

        // Vẽ logo
        this.modlogorenderer.renderLogo(guiGraphics, this.width, alpha);

        // Vẽ các thành phần khác (nút và giao diện)
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private float calculateAlpha (long elapsedTime) {
        return Mth.clamp((float) elapsedTime / FADE_IN_DURATION, 0.0F, 1.0F);
    }

    private void renderMovingBackground(GuiGraphics guiGraphics, int offsetY) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        // Điều chỉnh kích thước render theo backgroundScale
        int scaledWidth = (int) (scaleX / backgroundScale);  // Kích thước rộng theo tỷ lệ
        int scaledHeight = (int) (scaleY / backgroundScale); // Kích thước cao theo tỷ lệ

        // Tính toán vị trí offset (cuộn ảnh nền)
        offsetY = offsetY % scaledHeight;

        // Lặp để bao phủ toàn màn hình
        int requiredRepeatsX = (int) Math.ceil((double) this.width / scaledWidth);
        int requiredRepeatsY = (int) Math.ceil((double) this.height / scaledHeight) + 1;

        // Vẽ nền
        for (int x = 0; x < requiredRepeatsX; x++) {
            for (int y = 0; y < requiredRepeatsY; y++) {
                int xPos = x * scaledWidth;  // Vị trí từ bên trái
                int yPos = -offsetY + (y * scaledHeight); // Vị trí từ trên xuống (kèm theo cuộn)
                guiGraphics.blit(
                        BACKGROUND_TEXTURE,
                        xPos, yPos,       // Tọa độ render
                        0, 0,             // Tọa độ nguồn trong texture
                        scaledWidth, scaledHeight, // Kích thước render
                        scaleX, scaleY            // Kích thước ảnh gốc
                );
            }
        }
    }
}