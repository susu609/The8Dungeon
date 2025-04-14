package net.ss.sudungeon.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.ss.sudungeon.util.GuiUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin extends Screen {
    @Mutable
    @Final
    @Shadow
    private final StoringChunkProgressListener progressListener;

    protected LevelLoadingScreenMixin (Component p_96550_, StoringChunkProgressListener progressListener) {
        super(p_96550_);
        this.progressListener = progressListener;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void injectCustomBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int barWidth = (int)(screenWidth * 0.6F); // tăng chiều dài ngang lên 60%
        int barHeight = 16; // tăng độ dày thanh bar

        int x0 = centerX - barWidth / 2;
        int x1 = centerX + barWidth / 2;
        int y0 = centerY + 40; // đẩy xuống 1 chút
        int y1 = y0 + barHeight;
        float progress = Mth.clamp(progressListener.getProgress(), 0, 100) / 100.0f;
        int scrollY  = (int) ((progress * 300) % screenWidth);
        GuiUtils.renderMovingBackground(


                guiGraphics,
                new ResourceLocation("ss", "textures/gui/background.png"),
                scrollY,
                10.0f,
                screenWidth,
                screenHeight,
                screenWidth,
                screenHeight
        );

        // 🔲 Nền mờ đen phủ toàn màn hình
        guiGraphics.fill(x0 + 1, y0 + 1, x1 + 1, y1 + 1, 0x88000000);


        GuiUtils.drawProgressBar(guiGraphics, x0, y0, x1, y1, 1.0f, progress);

        // Hủy render mặc định
        ci.cancel();
    }

}

