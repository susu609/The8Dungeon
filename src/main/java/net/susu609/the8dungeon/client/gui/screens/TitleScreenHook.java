package net.susu609.the8dungeon.client.gui.screens;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, value = Dist.CLIENT)
public final class TitleScreenHook {
    /**
     * Thay TitleScreen vanilla bằng ModTitleScreen ngay khi game định mở nó.
     * Điều kiện `!(... instanceof ModTitleScreen)` ngăn vòng lặp: ModTitleScreen
     * cũng là TitleScreen nên nếu thiếu, ta sẽ thay nó bằng chính nó mãi.
     */
    @SubscribeEvent
    public static void onOpen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen
                && !(event.getNewScreen() instanceof ModTitleScreen)) {
            event.setNewScreen(new ModTitleScreen());
        }
    }
}