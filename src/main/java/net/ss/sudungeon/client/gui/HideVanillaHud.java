package net.ss.sudungeon.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ss", value = Dist.CLIENT)
public class HideVanillaHud {

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        // Kiểm tra ID của overlay hiện tại để quyết định ẩn
        if (shouldHideOverlay(event.getOverlay().id())) {
            // Hủy render overlay này
            event.setCanceled(true);
        }
    }

    // Xác định các HUD overlay cần ẩn
    private static boolean shouldHideOverlay(ResourceLocation overlayId) {
        // Các overlay cần ẩn, ví dụ như hotbar, crosshair, health bar, v.v.
        return /*overlayId.equals(VanillaGuiOverlay.HOTBAR.id())
                ||*/ overlayId.equals(VanillaGuiOverlay.AIR_LEVEL.id())
                || overlayId.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
                || overlayId.equals(VanillaGuiOverlay.CROSSHAIR.id())
                || overlayId.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())
                || overlayId.equals(VanillaGuiOverlay.FOOD_LEVEL.id())
                || overlayId.equals(VanillaGuiOverlay.PLAYER_HEALTH.id());
    }
}
