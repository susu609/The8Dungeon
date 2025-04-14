package net.ss.sudungeon.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.util.Log;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class BlackScreenOverlay {

    private static OverlayStateManager stateManager; // Quản lý trạng thái
    private static ResourceLocation overlayTexture;
    private static OverlayState currentState = OverlayState.INACTIVE;
    private static Runnable onFinish;

    public enum OverlayState {
        FADING_IN,
        HOLDING,
        FADING_OUT,
        INACTIVE
    }

    public static void showOverlay(ResourceLocation texture, int fadeIn, int hold, int fadeOut, Runnable onComplete) {
        overlayTexture = texture;
        stateManager = new OverlayStateManager(fadeIn, hold, fadeOut);
        currentState = OverlayState.FADING_IN;
        onFinish = onComplete;
    }

    public static void showOverlay (ResourceLocation texture, int fadeIn, int hold, int fadeOut) {
        overlayTexture = texture;
        stateManager = new OverlayStateManager(fadeIn, hold, fadeOut);
        currentState = OverlayState.FADING_IN;
    }

    private static void updateState() {
        if (currentState == OverlayState.INACTIVE) return;

        stateManager.update();

        if (stateManager.isFadingIn()) {
            currentState = OverlayState.FADING_IN;
        } else if (stateManager.isHolding()) {
            currentState = OverlayState.HOLDING;
        } else if (stateManager.isFadingOut()) {
            currentState = OverlayState.FADING_OUT;
        } else if (stateManager.isFinished()) {
            currentState = OverlayState.INACTIVE;
            if (onFinish != null) {
                Log.i("Overlay transition finished. Running callback.");
                onFinish.run();
            }
        }
    }

    private static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (currentState == OverlayState.INACTIVE || overlayTexture == null || stateManager == null) return;

        float alpha = stateManager.calculateAlpha();
        if (alpha <= 0F) return; // Không cần render nếu alpha là 0

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, overlayTexture);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        event.getGuiGraphics().blit(overlayTexture, 0, 0, 0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        RenderSystem.disableBlend();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGuiOverlay (RenderGuiOverlayEvent.Post event) {
        updateState();
        renderOverlay(event);
    }
}