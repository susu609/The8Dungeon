package net.ss.sudungeon.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;

import java.util.function.Supplier;

public class RequestDungeonDataPacket {

    public RequestDungeonDataPacket() {
    }

    public RequestDungeonDataPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                Level level = context.getSender().getCommandSenderWorld();
                if (level instanceof ServerLevel serverLevel) {  // Kiểm tra nếu level là ServerLevel
                    DungeonSavedData dungeonSavedData = DungeonSavedData.get(serverLevel);
                    SsMod.PACKET_HANDLER.sendTo(
                            new DungeonDataPacket(dungeonSavedData),
                            context.getSender().connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
        });
        context.setPacketHandled(true);
    }

}
