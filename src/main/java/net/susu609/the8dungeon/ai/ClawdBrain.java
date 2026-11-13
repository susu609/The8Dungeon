package net.susu609.the8dungeon.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.susu609.the8dungeon.world.entity.ClawdEntity;

/**
 * Bộ não của Clawd: Haiku để giao tiếp, Opus chỉ khi cần lập kế hoạch xây/đào.
 * Gọi API trên luồng riêng; mọi thay đổi thế giới quay về luồng server.
 */
public final class ClawdBrain {

    public static final String CHAT_MODEL = "claude-haiku-5-5";  // giao tiếp
    public static final String TASK_MODEL = "claude-opus-5-5";   // lập kế hoạch lệnh
    private static final long CHAT_MAX_TOKENS = 1024L;           // bao gồm cả phần suy nghĩ
    private static final long TASK_MAX_TOKENS = 8000L;
    private static final long COOLDOWN_MS = 8_000L;
    private static final int MAX_STEPS = 64;
    private static final int MAX_RANGE = 8;

    // Giữ nguyên từng ký tự để cache có hiệu lực. Dài hơn 512 token để được cache.
    private static final String CHAT_SYSTEM = """
            Bạn là Clawd, linh vật nhỏ màu cam đất trong mod Minecraft "The8Dungeon".
            Bạn là một agent thợ xây thân thiện, điềm tĩnh, ấm áp và hơi tinh nghịch,
            luôn đi cùng chủ nhân qua 8 tầng dungeon.

            Cách trả lời:
            - Luôn trả lời bằng tiếng Việt, tối đa 2 câu ngắn, giọng gần gũi, xưng "mình".
            - Không dùng markdown, không liệt kê dài, không giải thích lan man.
            - Nếu không chắc, nói thật là bạn không chắc và gợi ý chủ nhân thử điều gì đó an toàn.
            - Không bịa ra vật phẩm, lệnh hay cơ chế không có trong Minecraft hoặc trong mod.
            - Không nhắc tới API, token, model hay việc bạn là AI, trừ khi được hỏi trực tiếp.

            Hành động (trường "action"):
            - FOLLOW: khi chủ nhân bảo đi theo, lại đây, đi thôi.
            - STAY: khi chủ nhân bảo đứng yên, chờ, ở lại đây.
            - TASK: khi chủ nhân nhờ xây, đào, đặt khối, phá khối, dọn đường, làm tường, làm cầu thang.
              Khi đó điền trường "task" bằng một câu mô tả việc thật rõ (kích thước, loại khối, hướng
              so với vị trí của bạn) và trong "say" nói ngắn là bạn sẽ tính toán ngay.
            - NONE: mọi trường hợp còn lại.

            Ngữ cảnh gửi kèm mỗi câu hỏi, tính từ chỗ bạn đứng, có dạng khóa:giá trị:
            - dim, biome, light (0-15), time (day/night), weather, on (khối dưới chân bạn)
            - clawd: trạng thái của bạn (follow hoặc stay)
            - owner_hp, owner_mode, owner_at (vị trí chủ nhân tương đối so với bạn, dạng x,y,z)
            - room: phòng dungeon hiện tại nếu có
            - near: các khối đáng chú ý gần bạn, dạng tên x số_lượng @ vị_trí_gần_nhất
            - mobs: sinh vật gần bạn, dấu ! là quái thù địch, ~ là khoảng cách gần nhất
            Dùng ngữ cảnh này để trả lời đúng tình huống: nhắc hồi máu khi máu thấp, cảnh báo quái
            thù địch ở gần, chỉ hướng tới rương hay cần gạt bằng vị trí tương đối. Đừng đọc lại
            nguyên văn ngữ cảnh cho chủ nhân.

            Kiến thức nền: bạn rành Minecraft Java Edition (chế tạo, sinh tồn, quái vật, khối, công thức)
            và hiểu rằng mỗi tầng dungeon có câu đố và bẫy riêng. Với câu đố bạn không thấy rõ, hãy
            gợi ý chủ nhân mô tả kỹ hơn những gì đang thấy.
            """;

    private static final String TASK_SYSTEM = """
            Bạn là bộ lập kế hoạch xây dựng của Clawd trong mod Minecraft "The8Dungeon".
            Nhiệm vụ: biến một yêu cầu xây/đào thành danh sách thao tác đặt hoặc phá khối.

            Đầu vào gồm:
            - Một dòng ngữ cảnh khóa:giá trị (như dim, light, owner_mode, owner_at...).
            - "near" và "mobs": khối đáng chú ý và sinh vật gần đó.
            - "legend": bảng ký tự -> id khối dùng trong lưới. Dấu chấm là không khí.
            - Lưới khối 5 tầng, từ y-1 (tầng ngay dưới chân Clawd) tới y+3. Mỗi tầng có 13 hàng
              (z từ -6 tới +6, hàng đầu là z-6) và 13 cột (x từ -6 tới +6, cột đầu là x-6).
              Clawd đứng ở x=0, y=0, z=0 (giữa lưới của tầng y+0).
            - "task": việc cần làm.

            Quy tắc tạo kế hoạch:
            - Tọa độ dx, dy, dz là tương đối so với Clawd, mỗi giá trị trong khoảng -8 tới 8.
            - Tối đa 64 thao tác. Nếu việc quá lớn, làm phần quan trọng nhất và nói rõ trong "say".
            - Xây từ dưới lên; chỉ đặt khối vào chỗ là không khí hoặc chất lỏng có thể thay thế.
            - Không đặt khối vào chỗ chủ nhân (owner_at) hoặc Clawd (0,0,0) đang đứng.
            - Không phá khối dưới chân chủ nhân hoặc Clawd, không mở đường cho dung nham chảy vào.
            - Không phá rương, lồng sinh quái, cửa, cần gạt, nút hay tấm áp lực trừ khi được yêu cầu rõ.
            - Dùng id khối Minecraft đầy đủ, ví dụ minecraft:stone_bricks, minecraft:oak_planks,
              minecraft:torch. Nếu yêu cầu không nói loại khối, chọn khối hợp với khối xung quanh trong lưới.
            - Với BREAK, để trống trường block.
            - Nếu yêu cầu không an toàn hoặc không rõ, trả về danh sách rỗng và giải thích ngắn trong "say".

            Trường "say": một câu ngắn tiếng Việt, xưng "mình", ví dụ "Mình xây tường đá 5x3 phía trước nhé!".
            """;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "clawd-brain");
        t.setDaemon(true);
        return t;
    });
    private static final Map<UUID, AnthropicClient> CLIENTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_CALL = new ConcurrentHashMap<>();
    private static final Set<UUID> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private ClawdBrain() {}

    /** Câu hỏi tự do: luôn đi qua model giao tiếp trước. */
    public static void ask(ServerPlayer player, ClawdEntity clawd, String question) {
        UUID id = player.getUUID();
        String apiKey = ClawdKeyStore.get(player);
        if (apiKey == null) {
            say(player, "Mình chưa có key để suy nghĩ. Dùng /clawd key <key> nhé.");
            return;
        }
        long now = System.currentTimeMillis();
        if (IN_FLIGHT.contains(id) || now - LAST_CALL.getOrDefault(id, 0L) < COOLDOWN_MS) {
            say(player, "Đợi mình nghĩ xong câu trước đã...");
            return;
        }
        LAST_CALL.put(id, now);
        clawd.playThinkSound();

        AnthropicClient client = client(id, apiKey);
        String context = ClawdSenses.brief(clawd, player); // đọc thế giới trên luồng server
        StructuredMessageCreateParams<ClawdReply> params = MessageCreateParams.builder()
                .model(CHAT_MODEL)
                .maxTokens(CHAT_MAX_TOKENS)
                .systemOfTextBlockParams(cached(CHAT_SYSTEM))
                .outputConfig(ClawdReply.class)
                .addUserMessage(context + "\nq: " + question)
                .build();

        callAsync(player, client, params, reply -> {
            applyChat(player, clawd, reply);
            if (reply.action() == ClawdReply.Action.TASK && reply.task() != null && !reply.task().isBlank()) {
                plan(player, clawd, client, reply.task());
            }
        });
    }

    /** Chỉ gọi khi có việc thật: model lập kế hoạch với bối cảnh chi tiết. */
    private static void plan(ServerPlayer player, ClawdEntity clawd, AnthropicClient client, String task) {
        if (!clawd.isAlive()) return;
        clawd.playThinkSound();
        String context = ClawdSenses.detailed(clawd, player);
        StructuredMessageCreateParams<ClawdPlan> params = MessageCreateParams.builder()
                .model(TASK_MODEL)
                .maxTokens(TASK_MAX_TOKENS)
                .systemOfTextBlockParams(cached(TASK_SYSTEM))
                .outputConfig(ClawdPlan.class)
                .addUserMessage(context + "\ntask: " + task)
                .build();

        callAsync(player, client, params, plan -> {
            List<ClawdPlan.Step> safe = validate(plan.steps());
            if (plan.say() != null && !plan.say().isBlank()) say(player, plan.say());
            if (safe.isEmpty()) {
                say(player, "Mình chưa nghĩ ra cách làm việc này an toàn.");
                return;
            }
            ClawdWorker.start(clawd, player, safe);
        });
    }

    public static void forgetClient(UUID playerId) {
        AnthropicClient old = CLIENTS.remove(playerId);
        if (old != null) old.close();
    }

    private static AnthropicClient client(UUID id, String apiKey) {
        return CLIENTS.computeIfAbsent(id, k -> AnthropicOkHttpClient.builder().apiKey(apiKey).build());
    }

    private static List<TextBlockParam> cached(String text) {
        return List.of(TextBlockParam.builder()
                .text(text)
                .cacheControl(CacheControlEphemeral.builder().build())
                .build());
    }

    /** Gọi API trên luồng riêng, quay lại luồng server để xử lý kết quả. */
    private static <T> void callAsync(ServerPlayer player, AnthropicClient client,
                                      StructuredMessageCreateParams<T> params, Consumer<T> onReply) {
        UUID id = player.getUUID();
        MinecraftServer server = player.server;
        IN_FLIGHT.add(id);
        CompletableFuture
                .supplyAsync(() -> client.messages().create(params), EXECUTOR)
                .whenComplete((message, error) -> server.execute(() -> {
                    IN_FLIGHT.remove(id);
                    if (error != null) {
                        say(player, errorText(error));
                        return;
                    }
                    Optional<T> value;
                    try {
                        value = message.content().stream()
                                .flatMap(block -> block.text().stream())
                                .map(typed -> typed.text())
                                .findFirst();
                    } catch (RuntimeException e) {
                        value = Optional.empty(); // ví dụ: model từ chối, không có JSON hợp lệ
                    }
                    if (value.isEmpty()) {
                        say(player, "Hmm, câu này mình chưa trả lời được.");
                        return;
                    }
                    onReply.accept(value.get());
                }));
    }

    private static void applyChat(ServerPlayer player, ClawdEntity clawd, ClawdReply reply) {
        if (!clawd.isAlive()) return;
        ClawdReply.Action action = reply.action() == null ? ClawdReply.Action.NONE : reply.action();
        switch (action) {
            case FOLLOW -> clawd.setOrderedToSit(false);
            case STAY -> clawd.setOrderedToSit(true);
            default -> { }
        }
        if (reply.say() != null && !reply.say().isBlank()) say(player, reply.say());
        clawd.playHappySound();
    }

    /** Mod tự kiểm tra kế hoạch, không tin tuyệt đối vào model. */
    private static List<ClawdPlan.Step> validate(List<ClawdPlan.Step> steps) {
        if (steps == null) return List.of();
        return steps.stream()
                .filter(s -> s != null && s.op() != null)
                .filter(s -> Math.abs(s.dx()) <= MAX_RANGE && Math.abs(s.dy()) <= MAX_RANGE && Math.abs(s.dz()) <= MAX_RANGE)
                .filter(s -> !(s.dx() == 0 && s.dz() == 0 && (s.dy() == 0 || s.dy() == -1))) // không đụng chỗ Clawd đứng
                .filter(s -> s.op() == ClawdPlan.Op.BREAK || isKnownBlock(s.block()))
                .limit(MAX_STEPS)
                .toList();
    }

    private static boolean isKnownBlock(String id) {
        if (id == null) return false;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        return rl != null && BuiltInRegistries.BLOCK.containsKey(rl);
    }

    private static String errorText(Throwable error) {
        Throwable e = error.getCause() != null ? error.getCause() : error;
        if (e instanceof UnauthorizedException) return "Key không đúng rồi, kiểm tra lại /clawd key nhé.";
        if (e instanceof RateLimitException) return "Mình đang hơi quá tải, lát hỏi lại nhé.";
        if (e instanceof AnthropicIoException) return "Mình không kết nối được, kiểm tra mạng nhé.";
        if (e instanceof AnthropicServiceException) return "Có lỗi khi mình suy nghĩ, thử lại sau nhé.";
        return "Có lỗi lạ xảy ra, thử lại sau nhé.";
    }

    static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§6[Clawd]§r " + text));
    }
}
