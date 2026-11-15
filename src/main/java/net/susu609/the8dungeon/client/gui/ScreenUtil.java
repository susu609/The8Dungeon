package net.susu609.the8dungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenUtil {
    private ScreenUtil() {}

    public static void open(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    public static void quit() {
        Minecraft.getInstance().stop();
    }
}