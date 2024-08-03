package net.ss.sudungeon.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.client.gui.screens.DungeonReportScreen;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DungeonDataPacket {
    private final List<RoomData> rooms;

    public DungeonDataPacket(List<RoomData> rooms) {
        this.rooms = rooms;
    }

    public DungeonDataPacket(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        rooms = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rooms.add(RoomData.readFromNBT(Objects.requireNonNull(buffer.readNbt())));
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(rooms.size());
        for (RoomData room : rooms) {
            CompoundTag tag = new CompoundTag();
            room.writeToNBT(tag);
            buffer.writeNbt(tag);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // Open DungeonReportScreen on client-side
                Minecraft.getInstance().setScreen(new DungeonReportScreen(rooms));
            }
        });
        context.get().setPacketHandled(true);
    }
}
