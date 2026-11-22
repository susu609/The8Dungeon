package net.susu609.the8dungeon.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.character.CharacterClass;
import net.susu609.the8dungeon.network.ServerboundCharacterPacket;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, value = Dist.CLIENT)
public final class PendingCharacter {
    private static CharacterClass value;

    public static void set(CharacterClass c) {
        value = c;
    }

    public static CharacterClass take() {
        CharacterClass c = value;
        value = null;
        return c;
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        CharacterClass c = PendingCharacter.take();
        if (c != null) Susu609Mod.PACKET_HANDLER.sendToServer(new ServerboundCharacterPacket(c.id));
    }
}