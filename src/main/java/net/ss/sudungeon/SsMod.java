package net.ss.sudungeon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.ss.sudungeon.init.*;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.network.packet.DungeonDataPacket;
import net.ss.sudungeon.network.packet.RequestDungeonDataPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("ss")
public class SsMod {
    public static final Logger LOGGER = LogManager.getLogger(SsMod.class);
    public static final String MOD_ID = "ss";
    public static final Capability<SsModVariables.PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, MOD_ID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static final Queue<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
    private static int messageID = 0;

    public SsMod() {
        LOGGER.info("Initializing SsMod...");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(DungeonSavedData.class);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // Đăng ký các sự kiện và đối tượng
        registerModComponents(bus);
        registerPackets();
    }

    private void registerModComponents (IEventBus bus) {
        SsModAttributes.REGISTRY.register(bus);
        SsModBlockEntities.REGISTRY.register(bus);
        SsModBlocks.REGISTRY.register(bus);
        SsModEntities.REGISTRY.register(bus);
        SsModItems.REGISTRY.register(bus);
        SsModParticleTypes.REGISTRY.register(bus);
        SsModTabs.REGISTRY.register(bus);
        SsModSounds.REGISTRY.register(bus);
    }


    public static void registerPackets () {
        int id = 0;

        // Đăng ký DungeonDataPacket để gửi dữ liệu dungeon từ server về client
        addNetworkMessage(DungeonDataPacket.class, DungeonDataPacket::encode, DungeonDataPacket::new, DungeonDataPacket::handle);

        // Đăng ký RequestDungeonDataPacket để client yêu cầu dữ liệu từ server
        addNetworkMessage(RequestDungeonDataPacket.class, RequestDungeonDataPacket::encode, RequestDungeonDataPacket::new, RequestDungeonDataPacket::handle);

    }



    public static <T> void addNetworkMessage(
            Class<T> messageType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer
    ) {
        PACKET_HANDLER.registerMessage(messageID++, messageType, encoder, decoder, messageConsumer);
    }

    private void setup (final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup complete.");
    }

    @SubscribeEvent
    public void onClientSetup (FMLClientSetupEvent event) {
        LOGGER.info("Client setup complete.");
    }


    public static void queueServerWork (int ticks, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            workQueue.add(new AbstractMap.SimpleEntry<>(action, ticks));
        }
    }

    @SubscribeEvent
    public void onServerTick (TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> completedActions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() <= 0) {
                    completedActions.add(work);
                }
            });

            completedActions.forEach(work -> {
                work.getKey().run();
                workQueue.remove(work);
            });
        }
    }
}
