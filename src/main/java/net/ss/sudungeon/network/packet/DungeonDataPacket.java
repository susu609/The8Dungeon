package net.ss.sudungeon.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.client.gui.DungeonMapGui;

import java.util.function.Supplier;

public class DungeonDataPacket {
    private final DungeonSavedData dungeonData;

    // Constructor nhận dữ liệu dungeon từ server
    public DungeonDataPacket (DungeonSavedData dungeonData) {
        this.dungeonData = dungeonData;
    }

    // Constructor từ buffer (client nhận dữ liệu từ server)
    public DungeonDataPacket(FriendlyByteBuf buffer) {
        // Giả sử bạn có phương thức read trong DungeonSavedData để đọc từ buffer
        this.dungeonData = DungeonSavedData.load(buffer.readNbt());
    }

    // Ghi dữ liệu vào buffer (server gửi dữ liệu đến client)
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(dungeonData.save(new CompoundTag()));
    }

    // Xử lý gói tin ở phía client
    public void handle (Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Mở GUI DungeonMapGui với dữ liệu nhận được từ server
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreen(new DungeonMapGui(dungeonData, mc.player));
            }
        });
        context.setPacketHandled(true);
    }
}
