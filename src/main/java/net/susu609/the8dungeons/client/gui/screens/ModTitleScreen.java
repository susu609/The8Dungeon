package net.susu609.the8dungeons.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class ModTitleScreen extends TitleScreen implements TitleScreenInterface {
    private Layout layout;

    public ModTitleScreen() {
        super(false); // false = không có hiệu ứng fade của vanilla
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void init() {
        super.init(); // để vanilla/Forge/mod khác thêm nút của họ
        layout = calculateLayout();   // tính một lần
        addMainButton(0, Component.literal("New Game"), b -> onNewGame());
        addMainButton(1, Component.literal("Continue"), b -> onContinue());
    }

    private void onNewGame() {
    }

    private void onContinue() {
    }

    private Layout calculateLayout() {
        int panelWidth  = clamp(Math.round(width * 0.30f), 168, 280);
        int buttonWidth = clamp(panelWidth - 40, 120, 190);
        int buttonX     = (panelWidth - buttonWidth) / 2;

        int buttonHeight = 20;
        int buttonGap    = 5;
        int rows         = 2;

        // TODO 1: 2 nút x 20 + 1 khoảng cách x 5 = 45
        int groupHeight = rows * buttonHeight + (rows - 1) * buttonGap;

        // TODO 2: tâm cụm ở 61% chiều cao, nút đầu cách tâm nửa cụm
        int buttonTop = Math.round(height * 0.61f) - groupHeight / 2;

        // TODO 3: thứ tự phải khớp với record
        return new Layout(panelWidth, buttonX, buttonWidth, buttonTop, buttonHeight, buttonGap);
    }

    private Button addMainButton(int row, Component label, Button.OnPress action) {
        // TODO 4: mọi số đều lấy từ layout
        int y = layout.buttonTop() + row * (layout.buttonHeight() + layout.buttonGap());
        return addRenderableWidget(
                Button.builder(label, action)
                        .bounds(layout.buttonX(), y, layout.buttonWidth(), layout.buttonHeight())
                        .build());
    }

    // TODO 5: thêm 2 trường vào cuối
    private record Layout(int panelWidth, int buttonX, int buttonWidth, int buttonTop,
                          int buttonHeight, int buttonGap) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // dùng layout.panelWidth() ... thay vì tính lại mỗi khung hình

        g.fill(0, 0, width, height, 0xFF101820); // nền xanh đen
        for (Renderable r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

}