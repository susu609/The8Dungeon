package net.ss.sudungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.TemplatesGuiButtonMessage;
import net.ss.sudungeon.world.inventory.TemplatesGuiMenu;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Giao diện người dùng (GUI) cho menu "TemplatesGuiMenu".
 * Giao diện này bao gồm các thành phần như ô nhập văn bản (EditBox), nút bấm (Button), checkbox và một nút hình ảnh.
 * Đồng thời còn hiển thị thực thể (Entity) của người chơi trong giao diện.
 *
 * @see TemplatesGuiMenu
 */
public class TemplatesGuiScreen extends AbstractContainerScreen<TemplatesGuiMenu> {
    private final static HashMap<String, Object> guistate = TemplatesGuiMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    // Ô nhập văn bản
    EditBox editbox;
    // Checkbox tùy chọn
    Checkbox checkbox;
    // Nút chữ thường
    Button button_test;
    // Nút ảnh
    ImageButton imagebutton_adasasd;

    /**
     * Khởi tạo GUI với thông tin thế giới và người chơi.
     *
     * @param container menu liên kết
     * @param inventory kho đồ người chơi
     * @param text      tiêu đề giao diện
     */
    public TemplatesGuiScreen (TemplatesGuiMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    /**
     * Xác định xem GUI có tạm dừng game không. Trả về true để tạm dừng.
     */
    @Override
    public boolean isPauseScreen () {
        return true;
    }

    private static final ResourceLocation texture = new ResourceLocation("ss:textures/screens/templates_gui.png");

    /**
     * Vẽ toàn bộ GUI, bao gồm thực thể người chơi và tooltip.
     */
    @Override
    public void render (GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        editbox.render(guiGraphics, mouseX, mouseY, partialTicks);
            InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 173, this.topPos + 20, 30, 0.05f + (float) Math.atan((this.leftPos + 173 - mouseX) / 40.0), (float) Math.atan((this.topPos + -29 - mouseY) / 40.0),
                    entity);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (mouseX > leftPos + -24 && mouseX < leftPos + 0 && mouseY > topPos + 60 && mouseY < topPos + 84)
            guiGraphics.renderTooltip(font, Component.translatable("gui.ss.templates_gui.tooltip_checkbox"), mouseX, mouseY);
    }

    /**
     * Vẽ nền giao diện với texture chính và các ảnh phụ.
     */
    @Override
    protected void renderBg (GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        guiGraphics.blit(new ResourceLocation("ss:textures/screens/adasasd.png"), this.leftPos + -22, this.topPos + -6, 0, 0, 512, 3072, 512, 3072);

        guiGraphics.blit(new ResourceLocation("ss:textures/screens/adasasd.png"), this.leftPos + 1, this.topPos + -26, 0, 0, 512, 512, 512, 3072);

        RenderSystem.disableBlend();
    }

    /**
     * Xử lý phím nhấn, đóng GUI nếu nhấn ESC.
     */
    @Override
    public boolean keyPressed (int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (editbox.isFocused())
            return editbox.keyPressed(key, b, c);
        return super.keyPressed(key, b, c);
    }

    /**
     * Tick mỗi frame để cập nhật ô nhập.
     */
    @Override
    public void containerTick () {
        super.containerTick();
        editbox.tick();
    }

    /**
     * Khi người chơi thay đổi kích thước cửa sổ.
     */
    @Override
    public void resize (Minecraft minecraft, int width, int height) {
        String editboxValue = editbox.getValue();
        super.resize(minecraft, width, height);
        editbox.setValue(editboxValue);
    }

    /**
     * Vẽ nhãn (text) trong GUI.
     * Ví dụ có thể vẽ văn bản động từ thủ tục (procedure).
     */
    @Override
    protected void renderLabels (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String string = "ss";
        if (
            true
        )
            guiGraphics.drawString(this.font,

                    string, -115, 131, -12829636, false);
    }


    /**
     * Khởi tạo các widget khi mở GUI: ô nhập, nút, checkbox, nút hình ảnh.
     * Các widget được lưu trong guistate để sử dụng lại sau.
     */
    @Override
    public void init () {
        super.init();
        editbox = new EditBox(this.font, this.leftPos + -34, this.topPos + -1, 118, 18, Component.translatable("gui.ss.templates_gui.editbox")) {
            @Override
            public void insertText (String text) {
                super.insertText(text);
                if (getValue().isEmpty())
                    setSuggestion(Component.translatable("gui.ss.templates_gui.editbox").getString());
                else
                    setSuggestion(null);
            }

            @Override
            public void moveCursorTo (int pos) {
                super.moveCursorTo(pos);
                if (getValue().isEmpty())
                    setSuggestion(Component.translatable("gui.ss.templates_gui.editbox").getString());
                else
                    setSuggestion(null);
            }
        };
        editbox.setSuggestion(Component.translatable("gui.ss.templates_gui.editbox").getString());
        editbox.setMaxLength(32767);
        guistate.put("text:editbox", editbox);
        this.addWidget(this.editbox);
        button_test = new PlainTextButton(this.leftPos + -85, this.topPos + -7, 46, 20, Component.translatable("gui.ss.templates_gui.button_test"), e -> {
            if (true) {
                SsMod.PACKET_HANDLER.sendToServer(new TemplatesGuiButtonMessage(0, x, y, z));
                TemplatesGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
            }
        }, this.font);
        guistate.put("button:button_test", button_test);
        this.addRenderableWidget(button_test);
        imagebutton_adasasd = new ImageButton(this.leftPos + 136, this.topPos + 15, 512, 3072, 0, 0, 3072, new ResourceLocation("ss:textures/screens/atlas/imagebutton_adasasd.png"), 512, 6144, e -> {
            if (true) {
                SsMod.PACKET_HANDLER.sendToServer(new TemplatesGuiButtonMessage(1, x, y, z));
                TemplatesGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
            }
        });
        guistate.put("button:imagebutton_adasasd", imagebutton_adasasd);
        this.addRenderableWidget(imagebutton_adasasd);
        checkbox = new Checkbox(this.leftPos + -88, this.topPos + 43, 20, 20, Component.translatable("gui.ss.templates_gui.checkbox"), false);
        guistate.put("checkbox:checkbox", checkbox);
        this.addRenderableWidget(checkbox);
    }
}
