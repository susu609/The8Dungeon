package net.susu609.the8dungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.network.OpenBotInventoryPacket;

/**
 * Spectator bấm E (phím inventory) khi đang theo dõi bot: thay vì mở kho của chính mình,
 * xin server mở kho của bot (chỉ xem, không lấy đồ ra được).
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BotInventoryKey {

    private BotInventoryKey() {}

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof InventoryScreen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isSpectator() || !BotHud.isActive()) return;
        event.setCanceled(true);
        Susu609Mod.PACKET_HANDLER.sendToServer(new OpenBotInventoryPacket());
    }
}
