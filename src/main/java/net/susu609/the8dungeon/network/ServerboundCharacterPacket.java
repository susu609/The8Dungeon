package net.susu609.the8dungeon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.character.CharacterApply;

import java.util.function.Supplier;

/**
 * Client gửi nghề đã chọn lên server
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerboundCharacterPacket {
    public final String classId;

    public ServerboundCharacterPacket(String classId) {
        this.classId = classId;
    }

    public ServerboundCharacterPacket(FriendlyByteBuf buf) {
        this.classId = buf.readUtf(32);
    }

    public static void encode(ServerboundCharacterPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.classId, 32);
    }

    public static void handle(ServerboundCharacterPacket p, Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ServerPlayer player = ctx.getSender();                        // gói lên server: biết ai gửi
        ctx.enqueueWork(() -> CharacterApply.apply(player, p.classId));
        ctx.setPacketHandled(true);
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        Susu609Mod.addNetworkMessage(ServerboundCharacterPacket.class, ServerboundCharacterPacket::encode, ServerboundCharacterPacket::new, ServerboundCharacterPacket::handle);
    }
}