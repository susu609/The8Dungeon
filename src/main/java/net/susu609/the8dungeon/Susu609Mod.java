package net.susu609.the8dungeon; // "Địa chỉ" của class, phải khớp với thư mục src/main/java/net/susu609/the8dungeon

// ===== IMPORT: báo cho Java biết các class bên dưới lấy từ đâu =====

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("susu609") // Annotation: báo cho Forge "đây là class chính của mod có id susu609"
public class Susu609Mod {

	// static final = hằng số dùng chung, chỉ có 1 bản cho cả mod, không gán lại được
	public static final Logger LOGGER = LogManager.getLogger(Susu609Mod.class); // Dùng: Susu609Mod.LOGGER.info("...")
	public static final String MODID = "susu609"; // Phải giống trong @Mod và mods.toml

	// ===== CONSTRUCTOR: Forge gọi đúng 1 lần khi load mod =====
	public Susu609Mod(FMLJavaModLoadingContext context) {
		// Start of user code block mod constructor
		// End of user code block mod constructor

		// Đăng ký class này lên EVENT_BUS của Forge (bus "game"),
		// để các hàm có @SubscribeEvent bên dưới (ví dụ tick) được gọi
		MinecraftForge.EVENT_BUS.register(this);

		// Lấy MOD bus: bus riêng dùng cho sự kiện lúc khởi động
		// (đăng ký item, block, entity... sẽ gắn vào bus này)
		IEventBus bus = context.getModEventBus();
		// Start of user code block mod init

		// End of user code block mod init
	}

	// Start of user code block mod methods
	// End of user code block mod methods

	// ===== MẠNG (NETWORK): gửi dữ liệu giữa client và server =====
	private static final String PROTOCOL_VERSION = "1"; // Client và server phải cùng phiên bản mới kết nối được

	// Tạo 1 kênh gói tin tên "susu609:susu609".
	// 2 tham số cuối: kiểm tra phiên bản bên client / server có bằng "1" không
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(MODID, MODID),
			() -> PROTOCOL_VERSION,      // lambda: trả về phiên bản của mình
			PROTOCOL_VERSION::equals,    // method reference: kiểm tra phiên bản client
			PROTOCOL_VERSION::equals);   // kiểm tra phiên bản server

	private static int messageID = 0; // Mỗi loại gói tin cần một số ID riêng, tăng dần

	// Hàm generic <T>: dùng được cho mọi loại gói tin (T = class gói tin của bạn)
	// encoder: ghi T vào buffer | decoder: đọc buffer ra T | messageConsumer: xử lý khi nhận được
	public static <T> void addNetworkMessage(Class<T> messageType,
											 BiConsumer<T, FriendlyByteBuf> encoder,
											 Function<FriendlyByteBuf, T> decoder,
											 BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++; // Gói tin tiếp theo sẽ nhận ID mới
	}

	// ===== HẸN GIỜ: "chạy việc này sau N tick" (20 tick = 1 giây) =====

	// Hàng chờ tạm: chứa (số tick chờ, việc cần làm). Dùng Concurrent vì có thể bị thêm từ luồng khác
	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();

	// Hàng đợi chính: tự sắp xếp, việc nào có tick nhỏ nhất (đến hạn sớm nhất) nằm đầu
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

	// Gọi hàm này để hẹn việc. Ví dụ: Susu609Mod.queueServerWork(40, () -> { ... }); // sau 2 giây
	public static void queueServerWork(int delay, Runnable action) {
		// Chỉ nhận việc nếu đang ở luồng SERVER. Gọi từ client thì việc bị BỎ QUA, không báo lỗi!
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	// Forge gọi hàm này mỗi tick của server (nhờ dòng EVENT_BUS.register(this) ở trên)
	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		// Mỗi tick event bắn 2 lần (START và END), chỉ xử lý lần END để không chạy 2 lần
		if (event.phase == TickEvent.Phase.END) {
			int currentTick = event.getServer().getTickCount(); // Tick hiện tại của server

			// Bước 1: chuyển việc từ hàng chờ tạm sang hàng đợi chính,
			// đổi "chờ N tick" thành "chạy ở tick số currentTick + N"
			IntObjectPair<Runnable> work;
			while ((work = workToBeScheduled.poll()) != null) { // poll() lấy ra phần tử đầu, rỗng thì trả về null
				workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
			}

			// Bước 2: chạy tất cả việc đã đến hạn (peek = xem đầu hàng, poll = lấy ra)
			while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
				workQueue.poll().run();
			}
		}
	}
}