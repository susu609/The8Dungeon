package net.susu609.the8dungeon.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * Điều hướng cho bot. Mượn bộ tìm đường vanilla của một Zombie "ma" (không bao giờ vào thế giới)
 * để tính Path, rồi tự lái ServerPlayer theo từng nút. Tìm đường chỉ chạy thưa (mỗi 40 tick
 * hoặc khi đích đổi), việc đi theo đường là phép tính vector rẻ.
 */
final class BotNav {

    private final BotPlayer bot;
    private Zombie helper;
    private Path path;
    private BlockPos goal;
    private int repathAt;
    private int stuck;
    private int checkAt;
    private Vec3 checkPos = Vec3.ZERO;

    /** true nếu đích hiện tại không đi tới được. Reset khi đổi đích. */
    boolean failed;

    BotNav(BotPlayer bot) {
        this.bot = bot;
    }

    void stop() {
        path = null;
        goal = null;
        failed = false;
        stuck = 0;
        bot.zza = 0.0F;
        bot.xxa = 0.0F;
        bot.setSprinting(false);
    }

    /** Đi tới target. Trả true khi đã đi hết đường (có thể chưa sát đích nếu đường cụt). */
    boolean moveTo(BlockPos target, int now, boolean sprint) {
        if (goal == null || goal.distSqr(target) > 2.0D) {
            goal = target.immutable();
            path = null;
            failed = false;
            stuck = 0;
            repathAt = 0;
            checkAt = now + 20;
            checkPos = bot.position();
        }
        if (path == null || now >= repathAt) {
            path = computePath(goal);
            repathAt = now + 40;
        }
        if (path == null) {
            failed = true;
            bot.zza = 0.0F;
            return false;
        }
        if (path.isDone()) {
            bot.zza = 0.0F;
            bot.setSprinting(false);
            return true;
        }

        Vec3 next = Vec3.atBottomCenterOf(path.getNextNodePos());
        double dx = next.x - bot.getX();
        double dz = next.z - bot.getZ();
        double dy = next.y - bot.getY();
        if (dx * dx + dz * dz < 0.25D && Math.abs(dy) < 1.2D) {
            path.advance();
            return false;
        }

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        bot.setYRot(yaw);
        bot.setYHeadRot(yaw);
        bot.yBodyRot = yaw;
        bot.setXRot(0.0F);
        bot.zza = 1.0F;
        bot.xxa = 0.0F;
        bot.setSprinting(sprint);

        // Nhảy: bậc cao hơn, va chạm ngang, hoặc đang bơi
        if (bot.isInWater() || bot.isInLava() || dy > 0.6D || bot.horizontalCollision) {
            bot.setJumping(true);
        }

        // Phát hiện kẹt: 20 tick mà gần như không nhúc nhích
        if (now >= checkAt) {
            if (bot.position().distanceToSqr(checkPos) < 0.16D) {
                stuck++;
                path = null; // tính lại đường
                if (stuck >= 4) failed = true;
            } else {
                stuck = 0;
            }
            checkPos = bot.position();
            checkAt = now + 20;
        }
        return false;
    }

    private Path computePath(BlockPos target) {
        if (helper == null || helper.level() != bot.level()) {
            helper = new Zombie(bot.level());
            if (helper.getNavigation() instanceof GroundPathNavigation ground) {
                ground.setCanOpenDoors(true);
                ground.setCanFloat(true);
            }
        }
        helper.moveTo(bot.getX(), bot.getY(), bot.getZ());
        return helper.getNavigation().createPath(target, 1);
    }
}
