package net.ss.sudungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class BlackScreenOverlay {
    private static final int FADE_IN_DURATION = 1000; // Duration for fade-in (in ticks)
    private static final int HOLD_DURATION = 1000;    // Duration to hold the black screen (in ticks)
    private static final int FADE_OUT_DURATION = 1000; // Duration for fade-out (in ticks)
    private static int currentTick;
    private static boolean isActive;
    private static boolean fadingIn;
    private static boolean holding;
    private static boolean fadingOut;

    private static final ResourceLocation BLACK_SCREEN_TEXTURE = new ResourceLocation(SsMod.MOD_ID, "textures/gui/black_screen.png");

    public static void showOverlay() {
        isActive = true;
        currentTick = 0;
        fadingIn = true;
        holding = false;
        fadingOut = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void eventHandler(RenderGuiOverlayEvent.Post event) { // Dùng Post để render overlay cuối cùng
        if (!isActive) return;

        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();

        float alpha = 0.0F;

        if (fadingIn) {
            if (currentTick < FADE_IN_DURATION) {
                alpha = (float) currentTick / FADE_IN_DURATION;
                currentTick++;
            } else {
                fadingIn = false;
                holding = true;
                currentTick = 0;
            }
        } else if (holding) {
            if (currentTick < HOLD_DURATION) {
                currentTick++;
                alpha = 1.0F;
            } else {
                holding = false;
                fadingOut = true;
                currentTick = 0;
            }
        } else if (fadingOut) {
            if (currentTick < FADE_OUT_DURATION) {
                alpha = 1.0F - (float) currentTick / FADE_OUT_DURATION;
                currentTick++;
            } else {
                isActive = false;
                fadingIn = false;
                holding = false;
                fadingOut = false;
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, alpha);
        RenderSystem.setShaderTexture(0, BLACK_SCREEN_TEXTURE);
        event.getGuiGraphics().blit(BLACK_SCREEN_TEXTURE, 0, 0, 0, 0, w, h, w, h);
        RenderSystem.disableBlend();
    }
}