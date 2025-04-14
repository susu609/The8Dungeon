package net.ss.sudungeon.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.ss.sudungeon.SsMod;

import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FillPacketHandler {

    private final BlockPos pos1;
    private final BlockPos pos2;
    private final String blockId;

    public FillPacketHandler(BlockPos pos1, BlockPos pos2, String blockId) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.blockId = blockId;
    }

    public FillPacketHandler(FriendlyByteBuf buf) {
        this.pos1 = buf.readBlockPos();
        this.pos2 = buf.readBlockPos();
        this.blockId = buf.readUtf();
    }

    public static void sendFillPacket(BlockPos pos1, BlockPos pos2, String blockId) {
        SsMod.PACKET_HANDLER.sendToServer(new FillPacketHandler(pos1, pos2, blockId));
    }

    public static void buffer(FillPacketHandler message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos1);
        buf.writeBlockPos(message.pos2);
        buf.writeUtf(message.blockId);
    }

    public static void handler(FillPacketHandler message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            Level world = player.level();
            BlockState state = resolveBlockState(message.blockId);
            fill(world, message.pos1, message.pos2, state);
        });
        context.setPacketHandled(true);
    }

    public static BlockState resolveBlockState(String id) {
        ResourceLocation rl = new ResourceLocation(id);
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        return block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    public static void fill(Level level, BlockPos pos1, BlockPos pos2, BlockState state) {
        BlockPos.betweenClosedStream(pos1, pos2).forEach(p -> level.setBlock(p, state, 3));
    }

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        SsMod.addNetworkMessage(FillPacketHandler.class, FillPacketHandler::buffer, FillPacketHandler::new, FillPacketHandler::handler);
    }
}
