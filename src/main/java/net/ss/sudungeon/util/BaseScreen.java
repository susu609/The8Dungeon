package net.ss.sudungeon.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Base class for custom menu screens.
 * It provides utilities for managing buttons and rendering the screen.
 */
public abstract class BaseScreen extends Screen {

    public final static HashMap<String, Object> guistate = new HashMap<>();
    protected final List<Button> buttons = new ArrayList<>(); // Keeps track of all buttons dynamically
    public boolean backgroundRendered = false; // Biến để kiểm tra đã vẽ nền hay chưa
    public Minecraft mc = MinecraftUtils.getMinecraft();
    public long lastLogTime = 0; // Lưu thời gian log lần cuối

    protected BaseScreen (Component title) {
        super(title);
    }

    /**
     * Utility method for adding buttons with bounds and their action.
     *
     * @param x       X coordinate of the button
     * @param y       Y coordinate of the button
     * @param width   Width of the button
     * @param height  Height of the button
     * @param text    Text of the button
     * @param onPress Action to perform when the button is clicked
     */
    protected void addButtonWithBounds (int x, int y, int width, int height, Component text, Button.OnPress onPress) {
        Button button = Button.builder(text, onPress)
                .bounds(x, y, width, height)
                .build();
        this.addRenderableWidget(button); // Thêm nút vào danh sách render
        this.buttons.add(button);         // Lưu nút vào danh sách quản lý
    }

    /**
     * Clears all buttons on the screen.
     */
    protected void clearAllButtons () {
        this.clearWidgets();  // Clears all widgets (buttons, GUIs, etc.)
        this.buttons.clear(); // Clears our button list
    }

    public boolean shouldReRenderBackground = true;

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (shouldReRenderBackground) {
            GuiUtils.drawBackground(guiGraphics, this.width, this.height);
            Log.i("Re-rendering background due to state change.");
            shouldReRenderBackground = false;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose () {
        super.onClose();
        backgroundRendered = false; // Reset trạng thái khi màn hình được đóng
    }

}