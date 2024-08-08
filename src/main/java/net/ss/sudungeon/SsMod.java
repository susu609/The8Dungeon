package net.ss.sudungeon;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
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
import net.ss.sudungeon.network.packet.ShowMainMenuScreenPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("ss")
public class SsMod {
    public static final Logger LOGGER = LogManager.getLogger(SsMod.class);
    public static final String MODID = "ss";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
    private static int messageID = 0;

    public SsMod() {
        LOGGER.info("Initializing SsMod...");
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        try {
            SsModBlockEntities.REGISTRY.register(bus);
            SsModBlocks.REGISTRY.register(bus);
            SsModEntities.REGISTRY.register(bus);
            SsModItems.REGISTRY.register(bus);
            SsModTabs.REGISTRY.register(bus);
        } catch (Exception e) {
            LOGGER.error("Error during registration: ", e);
        }

        LOGGER.info("SsMod initialized successfully.");
        addNetworkMessage(ShowMainMenuScreenPacket.class, ShowMainMenuScreenPacket::encode, ShowMainMenuScreenPacket::decode, ShowMainMenuScreenPacket::handle);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialization code
    }

    @SubscribeEvent
    public void onInitialize(FMLCommonSetupEvent event) {
        LOGGER.debug("Running onInitialize with event: {}", event);
        // Additional setup
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client-specific setup code
    }

    public static <T> void addNetworkMessage(
            Class<T> messageType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer
    ) {
        PACKET_HANDLER.registerMessage(messageID++, messageType, encoder, decoder, messageConsumer);
    }

    @SubscribeEvent
    public static synchronized void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            synchronized (workQueue) {
                workQueue.forEach(work -> {
                    work.setValue(work.getValue() - 1);
                    if (work.getValue() <= 0) // đảm bảo không âm
                        actions.add(work);
                });
                actions.forEach(e -> e.getKey().run());
                workQueue.removeAll(actions);
            }
        }
    }
}
