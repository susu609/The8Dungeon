package net.ss.sudungeon.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.screens.MainMenuScreen;
import net.ss.sudungeon.util.Log;

import java.util.HashMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OpenModeSelectionPacket {
    private final int buttonID, x, y, z;

    public OpenModeSelectionPacket (FriendlyByteBuf buffer) {
        this.buttonID = buffer.readInt();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
    }
    public OpenModeSelectionPacket() {
        this(0, 0, 0, 0); // hoặc bất kỳ giá trị mặc định nào bạn muốn
    }

    public OpenModeSelectionPacket (int buttonID, int x, int y, int z) {
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void buffer (OpenModeSelectionPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.buttonID);
        buffer.writeInt(message.x);
        buffer.writeInt(message.y);
        buffer.writeInt(message.z);
    }

    public static void handler (OpenModeSelectionPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player entity = context.getSender();
            int buttonID = message.buttonID;
            int x = message.x;
            int y = message.y;
            int z = message.z;
            handleButtonAction(entity, buttonID, x, y, z);
        });
        contextSupplier.get().enqueueWork(() -> {
            Log.i("[OpenModeSelectionPacket] Đang mở màn hình chọn chế độ!");
            Minecraft.getInstance().setScreen(new MainMenuScreen());
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static void handleButtonAction (Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        HashMap guistate = MainMenuScreen.guistate;
        // security measure to prevent arbitrary chunk generation
        if (!world.hasChunkAt(new BlockPos(x, y, z)))
            return;
        if (buttonID == 0) {

        }
        if (buttonID == 1) {

        }
    }

    @SubscribeEvent
    public static void registerMessage (FMLCommonSetupEvent event) {
        SsMod.addNetworkMessage(OpenModeSelectionPacket.class, OpenModeSelectionPacket::buffer, OpenModeSelectionPacket::new, OpenModeSelectionPacket::handler);
    }

}