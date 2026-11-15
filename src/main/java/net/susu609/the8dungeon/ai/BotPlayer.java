package net.susu609.the8dungeon.ai;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Người chơi giả chạy trên server (ServerPlayer thật) do BotBrain điều khiển.
 * Vì là ServerPlayer nên đói, hồi máu, nhặt đồ, đào khối, rơi, chết... đều theo luật vanilla.
 */
public final class BotPlayer extends ServerPlayer {

    private final BotBrain brain = new BotBrain(this);
    int deadTicks;
    private BlockPos home = BlockPos.ZERO;
    /** Thời gian trung bình (ns) của brain.tick(), trung bình trượt, dùng cho thống kê hiệu năng. */
    long avgNanos;
    private double lastX, lastY, lastZ;

    private BotPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile);
    }

    /** Tạo bot và cho vào thế giới tại điểm spawn của overworld. */
    /** Đặt true trong lúc hồi sinh để mixin PlayerListJoinMixin bỏ dòng "joined the game". */
    public static boolean quietJoin;

    public static BotPlayer spawn(MinecraftServer server, String name) {
        return spawn(server, name, false);
    }

    /** quiet=true: hồi sinh sau khi chết, không báo "joined the game". */
    public static BotPlayer spawn(MinecraftServer server, String name, boolean quiet) {
        ServerLevel overworld = server.overworld();
        GameProfile profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        BotPlayer bot = new BotPlayer(server, overworld, profile);
        quietJoin = quiet;
        try {
            server.getPlayerList().placeNewPlayer(new BotConnection(), bot);
        } finally {
            quietJoin = false;
        }

        // Dữ liệu lưu cũ có thể là của lần đã chết: dọn lại cho sạch
        if (bot.getHealth() <= 0.0F || bot.deathTime > 0) {
            bot.deathTime = 0;
            bot.hurtTime = 0;
            bot.getInventory().clearContent();
            bot.removeAllEffects();
            bot.setHealth(bot.getMaxHealth());
            bot.getFoodData().setFoodLevel(20);
        }

        ServerLevel level = bot.serverLevel();
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level.getSharedSpawnPos());
        bot.teleportTo(level, top.getX() + 0.5D, top.getY(), top.getZ() + 0.5D, 0.0F, 0.0F);
        bot.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        bot.home = bot.blockPosition();
        bot.lastX = bot.getX();
        bot.lastY = bot.getY();
        bot.lastZ = bot.getZ();
        return bot;
    }

    /** Điểm nhà (nơi sinh ra), dùng cho luật bán kính. */
    public BlockPos home() {
        return home;
    }

    /**
     * Gọi trên bot đã chết, ngay trước khi gỡ để hồi sinh: dữ liệu được lưu phải là trạng thái "sống",
     * nếu không bot mới sẽ nạp máu 0 / deathTime > 0 và client vẽ nó đỏ, nằm nghiêng 90° như đang chết.
     */
    void prepareRespawn() {
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
        this.hurtTime = 0;
        this.removeAllEffects();
        this.clearFire();
        this.fallDistance = 0.0F;
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.getFoodData().setFoodLevel(20);
        this.getFoodData().setSaturation(5.0F);
    }

    public void onHurtBy(net.minecraft.world.entity.player.Player attacker, boolean accidental) {
        brain.onHurtBy(attacker, accidental);
    }

    public void onGift(net.minecraft.world.entity.player.Player giver, int count) {
        brain.onGift(giver, count);
    }

    /** Bot khác đề nghị làm hòa với bot này. */
    boolean acceptPeace(BotPlayer from) {
        return this.isAlive() && brain.acceptPeace(from);
    }

    /** Xóa thù (/bot peace). target null = với tất cả. Trả về số mối thù đã xóa. */
    public int forgive(java.util.UUID target) {
        return brain.forgive(target);
    }

    public String socialStatus() {
        return brain.socialStatus();
    }

    public BotInfo info() {
        return brain.info(getGameProfile().getName());
    }

    public String status() {
        return brain.status();
    }

    @Override
    public void tick() {
        if (this.getServer() != null && this.getServer().getTickCount() % 10 == 0) {
            this.connection.resetPosition();
            this.serverLevel().getChunkSource().move(this); // giữ chunk quanh bot được tải
        }
        if (this.isAlive()) {
            long t0 = System.nanoTime();
            brain.tick();
            avgNanos += ((System.nanoTime() - t0) - avgNanos) / 64L;
        }
        super.tick();
        this.doTick(); // không có gói tin của client nên phải tự gọi

        if (this.isAlive()) {
            // Tính hao đói theo quãng đường đi (vanilla chỉ làm việc này khi nhận gói di chuyển)
            this.checkMovementStatistics(getX() - lastX, getY() - lastY, getZ() - lastZ);
        }
        lastX = getX();
        lastY = getY();
        lastZ = getZ();
    }
}
