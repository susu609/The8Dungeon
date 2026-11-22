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
public class ClientboundVisitedRoomsPacket {
    public final int[] rooms;

    public ClientboundVisitedRoomsPacket(int[] rooms) {
        this.rooms = rooms;
    }

    public ClientboundVisitedRoomsPacket(FriendlyByteBuf buf) {
        this.rooms = buf.readVarIntArray();
    }

    public static void encode(ClientboundVisitedRoomsPacket p, FriendlyByteBuf buf) {
        buf.writeVarIntArray(p.rooms);
    }

    public static void handle(ClientboundVisitedRoomsPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DungeonClientState.setVisited(packet.rooms));   // trong handle
        ctx.setPacketHandled(true);   // báo là đã xử lý xong
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        Susu609Mod.addNetworkMessage(ClientboundVisitedRoomsPacket.class, ClientboundVisitedRoomsPacket::encode, ClientboundVisitedRoomsPacket::new, ClientboundVisitedRoomsPacket::handle);
    }
}