package net.susu609.the8dungeons.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

public interface TitleScreenInterface extends ContainerEventHandler, Renderable {
    void init();

    //warn : Not annotated parameter overrides @ParametersAreNonnullByDefault parameter
    void render(GuiGraphics g, int mouseX, int mouseY, float partialTick);
}
