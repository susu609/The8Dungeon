package net.ss.sudungeon.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.client.gui.components.ModLogoRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class ModTitleScreen extends TitleScreen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("ss:textures/screens/background1.png");
    private final boolean fading;
    private long fadeInStart;
    private final ModLogoRenderer modLogoRenderer;

    public ModTitleScreen (boolean fading) {
        super();
        this.fading = fading;
        this.modLogoRenderer = new ModLogoRenderer(true); // Khởi tạo ModLogoRenderer
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void eventHandler (ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen) {

            event.setNewScreen(new ModTitleScreen(true));
        }
    }

    @Override
    protected void init () {
        super.init();
        this.clearWidgets();
        // Thêm các nút khác của bạn ở đây
        int copyrightWidth = this.font.width(COPYRIGHT_TEXT);
        int copyrightX = (this.width - copyrightWidth) / 2;
        int copyrightY = this.height - 20;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 4;
        int startY = this.height / 4 + 48; // Vị trí y bắt đầu
        Button modButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("fml.menu.mods"), button -> {
                            assert this.minecraft != null;
                            this.minecraft.setScreen(new
                                    ModListScreen(this));
                        })
                .pos(this.width / 2 - 100, startY + 24 * 2)
                .size(98, 20)
                .build());
        net.minecraftforge.client.gui.TitleScreenModUpdateIndicator.init(this, modButton);

        this.addRenderableWidget(new ImageButton(this.width / 2 - 124, startY + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (p_280830_) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, Component.translatable("narrator.button.language")));
        this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), (p_280838_) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }).bounds(this.width / 2 - 100, startY + 72 + 12, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (p_280831_) -> {
            assert this.minecraft != null;
            this.minecraft.stop();
        }).bounds(this.width / 2 + 2, startY + 72 + 12, 98, 20).build());
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, startY + 72 + 12, 20, 20, 0, 0, 20, Button.ACCESSIBILITY_TEXTURE, 32, 64, (p_280835_) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options));
        }, Component.translatable("narrator.button.accessibility")));
        PlainTextButton copyrightButton = new PlainTextButton(copyrightX, copyrightY, copyrightWidth, 10, COPYRIGHT_TEXT, (p_280834_) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new CreditsAndAttributionScreen(this));
        }, this.font);
        copyrightButton.setAlpha(0.75f);
        this.addRenderableWidget(copyrightButton);
        // Nút "Singleplayer" tùy chỉnh thành "New Game" (ví dụ)
        this.addRenderableWidget(Button.builder(Component.literal("Play"), (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new PlayMenuScreen());
                }
        ).bounds((this.width - buttonWidth) / 2, startY + (buttonHeight + spacing), buttonWidth, buttonHeight).build());

    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        // Render background
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindForSetup(BACKGROUND_TEXTURE);
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        // Render các logo
        this.modLogoRenderer.renderLogo(guiGraphics, this.width, partialTicks);

        // Render other TitleScreen components (excluding Realms, Multiplayer)
        List<? extends GuiEventListener> children = this.children();
        for (GuiEventListener element : children) {
            if (!(element instanceof AbstractWidget button && (
                    button.getMessage().getString().equals("Realms") ||
                            button.getMessage().getString().equals("Multiplayer")
            ))) {
                assert element instanceof AbstractWidget;
                ((AbstractWidget) element).render(guiGraphics, mouseX, mouseY, partialTicks); // render trực tiếp
            }
        }
    }

}