package net.susu609.the8dungeon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.dungeon.DungeonClientState;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientboundDungeonSeedPacket {
    public final boolean present;
    public final long seed;

    public ClientboundDungeonSeedPacket(boolean present, long seed) {
        this.present = present;
        this.seed = seed;
    }

    // Constructor đọc lại từ buffer, thứ tự phải khớp với encode
    public ClientboundDungeonSeedPacket(FriendlyByteBuf buf) {
        this.present = buf.readBoolean();
        this.seed = buf.readLong();
    }

    public static void encode(ClientboundDungeonSeedPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.present);
        buf.writeLong(packet.seed);
    }

    public static void handle(ClientboundDungeonSeedPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DungeonClientState.apply(packet.present, packet.seed));
        ctx.setPacketHandled(true);   // báo là đã xử lý xong
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        Susu609Mod.addNetworkMessage(ClientboundDungeonSeedPacket.class, ClientboundDungeonSeedPacket::encode, ClientboundDungeonSeedPacket::new, ClientboundDungeonSeedPacket::handle);
    }
}