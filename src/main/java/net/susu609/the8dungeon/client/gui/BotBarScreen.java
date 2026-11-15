package net.susu609.the8dungeon.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.ai.BotInfo;
import net.susu609.the8dungeon.client.SpectatorCam;
import net.susu609.the8dungeon.network.SkipNightPacket;
import org.lwjgl.glfw.GLFW;

/**
 * Màn hình trong suốt chỉ để có con trỏ chuột khi giữ Alt: trỏ vào mặt bot xem chỉ số,
 * bấm để camera bay tới bot đó; bấm "Bỏ qua đêm" khi nút đang hiện. Thả Alt là đóng.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BotBarScreen extends Screen {

    public static final KeyMapping KEY = new KeyMapping("key.susu609.cursor", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT, "key.categories.susu609");

    private BotBarScreen() {
        super(Component.literal("Bot"));
    }

    @Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Keys {
        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(KEY);
        }
    }

    /** Giữ phím thì mở (khi có gì để bấm), thả ra thì đóng. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean held = KEY.isDown();
        if (held && mc.screen == null && (BotHud.showFaces() || BotHud.skipVisible())) {
            mc.setScreen(new BotBarScreen());
        }
    }

    private boolean keyHeld() {
        // Khi màn hình đang mở, KeyMapping không còn nhận phím: đọc trực tiếp trạng thái phím
        InputConstants.Key k = KEY.getKey();
        return k.getType() == InputConstants.Type.KEYSYM
                && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), k.getValue());
    }

    @Override
    public void tick() {
        if (!keyHeld()) onClose();
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // không làm tối màn hình: chỉ cần con trỏ
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        BotInfo hover = faceAt(mx, my);
        if (hover != null) {
            List<Component> lines = List.of(
                    Component.literal(hover.name()),
                    Component.literal(BotHud.label(hover.goal())),
                    Component.literal("Máu " + (int) hover.hp() + "/20   Đói " + hover.food() + "/20"),
                    Component.literal("Gỗ " + hover.wood() + "   Đá " + hover.stone() + "   Cuốc: " + BotHud.pick(hover.pick())),
                    Component.literal("Bấm để xem bot này"));
            g.renderComponentTooltip(font, lines, mx, my);
        }
    }

    private BotInfo faceAt(double mx, double my) {
        if (!BotHud.showFaces()) return null;
        List<BotInfo> bots = BotHud.allBots();
        for (int i = 0; i < bots.size(); i++) {
            int[] r = BotHud.faceRect(width, i);
            if (mx >= r[0] && mx < r[0] + BotHud.FACE && my >= r[1] && my < r[1] + BotHud.FACE + 5) return bots.get(i);
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        BotInfo face = faceAt(mx, my);
        if (face != null) {
            SpectatorCam.flyTo(face.name());
            return true;
        }
        if (BotHud.skipVisible()) {
            int[] r = BotHud.skipRect(width);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                Susu609Mod.PACKET_HANDLER.sendToServer(new SkipNightPacket());
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
