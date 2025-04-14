package net.ss.sudungeon.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.ss.sudungeon.client.gui.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SsModScreens {
    @SubscribeEvent
    public static void clientLoad (FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
//            MenuScreens.register(SsModMenus.CHOSEN_MODE_GUI.get(), ChosenModeGuiScreen::new);
//            MenuScreens.register(SsModMenus.MENU_MOD_GUI.get(), MenuModGuiScreen::new);
//            MenuScreens.register(SsModMenus.STARTER_GEAR_GUI.get(), StarterGearGuiScreen::new);
//            MenuScreens.register(SsModMenus.SHOP_GUI.get(), ShopGuiScreen::new);
              MenuScreens.register(SsModMenus.FILL_TOOL_GUI.get(), FillToolScreen::new);

        });
    }
}
