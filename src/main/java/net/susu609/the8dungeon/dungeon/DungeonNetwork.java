package net.susu609.the8dungeon.dungeon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.susu609.the8dungeon.Susu609Mod;


import java.util.function.Supplier;

/**
 * Gửi seed dungeon từ server xuống client để inspector dựng lại layout và vẽ.
 * Gửi khi vừa dựng dungeon (cho mọi người chơi) và khi một người chơi vào world.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class DungeonNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
             ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "dungeon"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private DungeonNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, SyncPacket.class,
                SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
    }

    public static void sendToAll(long seed) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncPacket(true, seed));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DungeonSavedData data = DungeonSavedData.get(player.serverLevel());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncPacket(data.present(), data.seed()));
    }

    public record SyncPacket(boolean present, long seed) {
        static void encode(SyncPacket p, FriendlyByteBuf buf) {
            buf.writeBoolean(p.present);
            buf.writeLong(p.seed);
        }

        static SyncPacket decode(FriendlyByteBuf buf) {
            return new SyncPacket(buf.readBoolean(), buf.readLong());
        }

        static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
            // Gói đến ở luồng mạng; việc đụng vào trạng thái game phải chuyển sang luồng chính.
            ctx.get().enqueueWork(() -> DungeonClientState.apply(p.present, p.seed));
            ctx.get().setPacketHandled(true);
        }
    }
}
