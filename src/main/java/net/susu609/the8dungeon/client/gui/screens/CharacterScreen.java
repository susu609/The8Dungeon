package net.susu609.the8dungeon.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.susu609.the8dungeon.character.CharacterClass;
import net.susu609.the8dungeon.client.gui.ScreenUtil;

import java.util.function.Consumer;

public class CharacterScreen extends Screen {
    private final Screen parent;
    private final Consumer<CharacterClass> onStart;
    private CharacterClass selected = CharacterClass.ROGUE;

    public CharacterScreen(Screen parent, Consumer<CharacterClass> onStart) {
        super(Component.translatable("screen.susu609.character"));
        this.parent = parent;
        this.onStart = onStart;
    }

    @Override
    protected void init() {
        int y = this.height / 4;
        for (CharacterClass c : CharacterClass.values()) {       // mỗi nghề 1 nút
            addRenderableWidget(Button.builder(Component.translatable("class.susu609." + c.id), b -> selected = c).bounds(20, y, 100, 20).build());
            y += 24;
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).bounds(this.width / 2 - 104, this.height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.susu609.start"), b -> onStart.accept(selected)).bounds(this.width / 2 + 4, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        g.drawString(font, Component.translatable("class.susu609." + selected.id + ".desc"), 140, height / 4, 0xCCCCCC);
        super.render(g, mouseX, mouseY, partialTick);           // vẽ nút sau cùng
    }

    @Override
    public void onClose() {
        ScreenUtil.open(parent);
    }
}