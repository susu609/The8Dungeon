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
    private double lastX, lastY, lastZ;

    private BotPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile);
    }

    /** Tạo bot và cho vào thế giới tại điểm spawn của overworld. */
    public static BotPlayer spawn(MinecraftServer server, String name) {
        ServerLevel overworld = server.overworld();
        GameProfile profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        BotPlayer bot = new BotPlayer(server, overworld, profile);
        server.getPlayerList().placeNewPlayer(new BotConnection(), bot);

        // Dữ liệu lưu cũ có thể là của lần đã chết: dọn lại cho sạch
        if (bot.getHealth() <= 0.0F || bot.deathTime > 0) {
            bot.deathTime = 0;
            bot.getInventory().clearContent();
            bot.removeAllEffects();
            bot.setHealth(bot.getMaxHealth());
            bot.getFoodData().setFoodLevel(20);
        }

        ServerLevel level = bot.serverLevel();
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level.getSharedSpawnPos());
        bot.teleportTo(level, top.getX() + 0.5D, top.getY(), top.getZ() + 0.5D, 0.0F, 0.0F);
        bot.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        bot.lastX = bot.getX();
        bot.lastY = bot.getY();
        bot.lastZ = bot.getZ();
        return bot;
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
            brain.tick();
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
