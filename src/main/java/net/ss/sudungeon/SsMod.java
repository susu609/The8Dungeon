package net.ss.sudungeon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.ss.sudungeon.init.*;
import net.ss.sudungeon.util.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("ss")
public class SsMod {
	public static final Logger LOGGER = LogManager.getLogger(SsMod.class);
	public static final String MODID = "ss";


	public SsMod () {
		Log.i("SsMod is initializing...");
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		SsModAttributes.REGISTRY.register(bus);
		SsModEntities.REGISTRY.register(bus);
		SsModItems.REGISTRY.register(bus);
		SsModBlockEntities.REGISTRY.register(bus);
		SsModBlocks.REGISTRY.register(bus);
		SsModMenus.REGISTRY.register(bus);
		SsModTabs.REGISTRY.register(bus);
		SsModSounds.REGISTRY.register(bus);
		SsModParticleTypes.REGISTRY.register(bus);
		Log.i("SsMod initialization completed.");
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		Log.d("Registering network message: " + messageType.getSimpleName());
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	// ✅ Optimized task system
	private static final ConcurrentHashMap<String, TaskData> taskQueue = new ConcurrentHashMap<>();

	public static void queueServerWork(String taskName, int tickDelay, Runnable action) {
		Log.d("Queueing server work: " + taskName + " with delay: " + tickDelay);
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			// ✅ Prevent continuous delay loops
			if (!taskQueue.containsKey(taskName)) {
				taskQueue.put(taskName, new TaskData(tickDelay, action));
				Log.i("Task '{}' added to the queue.", taskName);
			} else {
				LOGGER.warn("⚠ Task '{}' already exists in the queue, not adding again.", taskName);
			}
		}
	}

	// ✅ Check if task exists
	public static boolean hasServerWork(String taskName) {
		Log.d("Checking if task exists: " + taskName);
		return taskQueue.containsKey(taskName);
	}

	public static void cancelServerWork(String taskName) {
		Log.i("Cancelling task: " + taskName, taskName);
		taskQueue.remove(taskName);
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			taskQueue.forEach((name, task) -> {
				task.ticksRemaining--;
				if (task.ticksRemaining <= 0) {
					task.action.run();
					taskQueue.remove(name);
				}
			});
		}
	}

	private static class TaskData {
		int ticksRemaining;
		Runnable action;

		TaskData(int ticks, Runnable action) {
			Log.d("Creating new TaskData with ticks: " + ticks);
			this.ticksRemaining = ticks;
			this.action = action;
		}
	}
}
