/*
package net.ss.sudungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class ModGui extends Gui {
    public ModGui(Minecraft p_232355_, ItemRenderer p_232356_) {
        super(p_232355_, p_232356_);
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (Minecraft.getInstance().player != null) {
            // Thực hiện render tùy chỉnh ở đây
            ModGui customGui = new ModGui(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer());
            customGui.renderHearts(
                    event.getGuiGraphics(),               // GuiGraphics
                    Minecraft.getInstance().player,       // Player
                    10,  // Đây là các giá trị giả định cho các tham số int
                    10,  // Bạn cần thay thế bằng các giá trị hoặc biến phù hợp
                    20,
                    20,
                    1.0f, // Đây là giá trị float
                    15,  // Giá trị int khác
                    15,
                    15,
                    true // Đây là giá trị boolean, bạn có thể thay đổi thành false nếu cần
            );

        }
    }

    @SubscribeEvent
    public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        if (overlay.id().equals(new ResourceLocation("minecraft", "health"))) { // ID của overlay cần chặn
            event.setCanceled(true); // Hủy bỏ render overlay này
        }
    }

    @Override
    private void renderPlayerHealth(GuiGraphics p_283143_) {

    }
    @Override
    protected void renderHearts(@NotNull GuiGraphics p_282497_, @NotNull Player p_168690_, int p_168691_, int p_168692_, int p_168693_, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean p_168699_) {
        // Không gọi super.renderHearts() để ẩn thanh máu
        // Nếu bạn muốn tùy chỉnh lại render, bạn có thể thêm code ở đây
    }

    @Override
    public void renderExperienceBar(@NotNull GuiGraphics p_281906_, int p_282731_) {
        // Không gọi super.renderExperienceBar() để ẩn thanh kinh nghiệm
    }

    // Ghi đè các phương thức khác nếu cần
}
*/
