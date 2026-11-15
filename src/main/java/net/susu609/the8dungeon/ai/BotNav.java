package net.susu609.the8dungeon.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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
    private Vec3 heading = new Vec3(0.0D, 0.0D, 1.0D);

    /** true nếu đích hiện tại không đi tới được. Reset khi đổi đích. */
    boolean failed;

    /**
     * true nếu đường tìm được là đường cụt: không tới được đích và điểm cuối còn cách đích hơn 2,5 khối
     * (ví dụ món đồ rơi trên tán cây). Reset khi đổi đích.
     */
    boolean deadEnd;

    BotNav(BotPlayer bot) {
        this.bot = bot;
    }

    void stop() {
        path = null;
        goal = null;
        failed = false;
        deadEnd = false;
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
            deadEnd = false;
            stuck = 0;
            repathAt = 0;
            checkAt = now + 20;
            checkPos = bot.position();
        }
        if (path == null || now >= repathAt) {
            path = computePath(goal);
            deadEnd = path != null && !path.canReach()
                    && (path.getEndNode() == null || path.getEndNode().asBlockPos().distSqr(goal) > 6.25D);
            final Path found = path;
            BotLog.log(bot, "NAV", () -> found == null ? "KHÔNG có đường tới " + goal.toShortString()
                    : "đường " + found.getNodeCount() + " nút tới " + goal.toShortString()
                            + (found.canReach() ? "" : " (cụt, chỉ tới gần)"));
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

        // Cửa gỗ đóng trên đường đi: mở ra (như người chơi bấm chuột phải)
        BlockPos np = path.getNextNodePos();
        for (BlockPos q : new BlockPos[] {np, np.above()}) {
            net.minecraft.world.level.block.state.BlockState ds = bot.level().getBlockState(q);
            if (ds.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door
                    && ds.is(net.minecraft.tags.BlockTags.WOODEN_DOORS)
                    && !ds.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)
                    && bot.position().distanceToSqr(Vec3.atCenterOf(q)) < 3.0D * 3.0D) {
                door.setOpen(bot, bot.level(), ds, q, true);
            }
        }
        Vec3 next = Vec3.atBottomCenterOf(np);
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
        bot.xxa = 0.0F;
        double hd = Math.sqrt(dx * dx + dz * dz);
        heading = new Vec3(dx / Math.max(hd, 1.0E-4D), 0.0D, dz / Math.max(hd, 1.0E-4D));

        // Đi cẩn thận: sắp rẽ / đổi độ cao thì chậm lại; xuống dốc thì chậm
        boolean turnSoon = false;
        int idx = path.getNextNodeIndex();
        if (idx + 1 < path.getNodeCount()) {
            net.minecraft.world.level.pathfinder.Node n2 = path.getNode(idx + 1);
            double ax = n2.x + 0.5D - next.x, az = n2.z + 0.5D - next.z;
            double dot = (ax * dx + az * dz) / Math.max(1.0E-4D, Math.sqrt(ax * ax + az * az) * hd);
            turnSoon = dot < 0.7D || n2.y != np.getY(); // góc rẽ > ~45° hoặc lên/xuống bậc
        }
        float speed = 1.0F;
        if (hd < 1.3D && turnSoon) speed = 0.5F;
        if (dy < -0.5D) speed = Math.min(speed, 0.6F);
        // Mép vực: phía trước sâu hơn 3 khối (hoặc dung nham) mà đường không định đi xuống đó -> dừng, tìm đường khác
        boolean edge = dangerousEdgeAhead(heading);
        if (edge && dy > -0.5D) {
            speed = 0.0F;
            path = null;
            BotLog.log(bot, "NAV", () -> "dừng trước mép vực tại " + bot.blockPosition().toShortString());
        }
        bot.zza = speed;
        bot.setSprinting(sprint && speed >= 1.0F && Math.abs(dy) < 0.5D && !turnSoon && hd > 2.0D);

        // Nhảy: bậc cao hơn, va chạm ngang, hoặc đang bơi
        if (bot.isInWater() || bot.isInLava() || dy > 0.6D || bot.horizontalCollision) {
            bot.setJumping(true);
        }

        // Phát hiện kẹt: 20 tick mà gần như không nhúc nhích
        if (now >= checkAt) {
            if (bot.position().distanceToSqr(checkPos) < 0.16D) {
                stuck++;
                path = null; // tính lại đường
                final int n = stuck;
                BotLog.log(bot, "NAV_STUCK", () -> "kẹt lần " + n + " tại " + bot.blockPosition().toShortString());
                if (stuck >= 4) failed = true;
            } else {
                stuck = 0;
            }
            checkPos = bot.position();
            checkAt = now + 20;
        }
        return false;
    }

    /** Hướng đang đi (để biết khối nào chắn phía trước khi bị kẹt). */
    Vec3 heading() {
        return heading;
    }

    int stuckLevel() {
        return stuck;
    }

    /** Đích hiện tại (null nếu không đi đâu). */
    BlockPos goal() {
        return goal;
    }

    /** Sau khi tự gỡ kẹt (đào, kê khối): tính lại đường từ đầu. */
    void resetStuck() {
        stuck = 0;
        failed = false;
        path = null;
        checkPos = bot.position();
    }

    /** Phía trước 0,8 khối: rơi sâu hơn 3 khối, hoặc có dung nham bên dưới. */
    private boolean dangerousEdgeAhead(Vec3 dir) {
        if (!bot.onGround()) return false;
        net.minecraft.world.level.Level level = bot.level();
        int x = net.minecraft.util.Mth.floor(bot.getX() + dir.x * 0.8D);
        int z = net.minecraft.util.Mth.floor(bot.getZ() + dir.z * 0.8D);
        int y = net.minecraft.util.Mth.floor(bot.getY() + 0.01D);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        if (!level.getBlockState(m.set(x, y, z)).getCollisionShape(level, m).isEmpty()) return false; // phía trước là tường/bậc
        for (int k = 1; k <= 4; k++) {
            m.set(x, y - k, z);
            net.minecraft.world.level.block.state.BlockState s = level.getBlockState(m);
            if (s.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) return true;
            if (!s.getCollisionShape(level, m).isEmpty() || !s.getFluidState().isEmpty()) return false; // có chỗ đứng / nước đỡ
        }
        return true;
    }

    private Path computePath(BlockPos target) {
        if (helper == null || helper.level() != bot.level()) {
            helper = new Zombie(bot.level());
            if (helper.getNavigation() instanceof GroundPathNavigation ground) {
                ground.setCanOpenDoors(true);
                ground.setCanFloat(true);
                helper.setPathfindingMalus(BlockPathTypes.WATER, 8.0F); // đi được nhưng ưu tiên tránh nước
            }
        }
        helper.moveTo(bot.getX(), bot.getY(), bot.getZ());
        helper.setOnGround(true); // createPath từ chối khi mob không đứng trên đất, mà helper không bao giờ được tick
        return helper.getNavigation().createPath(target, 1);
    }
}
