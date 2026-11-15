package net.susu609.the8dungeon.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Kế hoạch hầm trú đêm: các ô đứng bên trong (cells) và các khối vỏ cần đặt (shell).
 * Hầm riêng: 1 ô, 9 khối. Hầm nhóm: 2x2 ô (tối đa 4 bot), 28 khối; mọi thành viên cùng đặt và vá.
 */
final class ShelterPlan {

    final boolean shared;
    final ResourceKey<Level> dim;
    final BlockPos origin;
    final List<BlockPos> cells = new ArrayList<>();
    final List<BlockPos> shell = new ArrayList<>();
    private final Map<String, BlockPos> assigned = new HashMap<>();

    private ShelterPlan(boolean shared, ResourceKey<Level> dim, BlockPos origin) {
        this.shared = shared;
        this.dim = dim;
        this.origin = origin.immutable();
    }

    /** Hộp 1x1: 4 hướng x 2 tầng + nắp. */
    static ShelterPlan solo(ResourceKey<Level> dim, BlockPos feet) {
        ShelterPlan p = new ShelterPlan(false, dim, feet);
        p.cells.add(p.origin);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            p.shell.add(p.origin.relative(d));
            p.shell.add(p.origin.relative(d).above());
        }
        p.shell.add(p.origin.above(2));
        return p;
    }

    /** Hầm nhóm 2x2 bắt đầu từ feet (góc tây-bắc). null nếu địa hình không đứng được cả 4 ô. */
    static ShelterPlan shared(ServerLevel level, BlockPos feet) {
        ShelterPlan p = new ShelterPlan(true, level.dimension(), feet);
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos c = p.origin.offset(dx, 0, dz);
                BlockPos below = c.below();
                if (!level.getBlockState(c).getCollisionShape(level, c).isEmpty()
                        || !level.getBlockState(c.above()).getCollisionShape(level, c.above()).isEmpty()
                        || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    return null;
                }
                p.cells.add(c);
            }
        }
        for (int y = 0; y <= 1; y++) {
            for (int dx = -1; dx <= 2; dx++) {
                for (int dz = -1; dz <= 2; dz++) {
                    boolean inside = dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1;
                    if (!inside) p.shell.add(p.origin.offset(dx, y, dz));
                }
            }
        }
        for (BlockPos c : p.cells) p.shell.add(c.above(2));
        return p;
    }

    boolean hasFreeCell() {
        return assigned.size() < cells.size();
    }

    boolean isMember(String bot) {
        return assigned.containsKey(bot);
    }

    /** Ô đứng của bot này (cấp ô trống đầu tiên nếu chưa có), hoặc null nếu hết chỗ. */
    BlockPos cellFor(String bot) {
        BlockPos have = assigned.get(bot);
        if (have != null) return have;
        for (BlockPos c : cells) {
            if (!assigned.containsValue(c)) {
                assigned.put(bot, c);
                return c;
            }
        }
        return null;
    }

    /** Khối vỏ còn thiếu (hoặc bị phá) trong tầm với của bot, không có sinh vật đứng chắn; null nếu không có. */
    BlockPos nextMissing(ServerLevel level, BotPlayer bot, double reach) {
        for (BlockPos p : shell) {
            if (!level.getBlockState(p).canBeReplaced()) continue;
            if (bot.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p)) > reach * reach) continue;
            if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(p)).isEmpty()) continue;
            return p;
        }
        return null;
    }

    /** Vỏ đã kín hết chưa (một lỗ thủng cũng tính là chưa kín). */
    boolean complete(ServerLevel level) {
        for (BlockPos p : shell) {
            if (level.getBlockState(p).canBeReplaced()) return false;
        }
        return true;
    }

    /**
     * Khối cần phá để ra ngoài buổi sáng. Hầm riêng: phá hết để lấy lại vật liệu.
     * Hầm nhóm: chỉ mở một lối ra ở bức tường cạnh ô của mình; phần còn lại để lại thành chòi.
     */
    List<BlockPos> exitFor(BlockPos cell) {
        if (!shared) return new ArrayList<>(shell);
        List<BlockPos> out = new ArrayList<>();
        Direction dir = cell.getX() == origin.getX() ? Direction.WEST : Direction.EAST;
        out.add(cell.relative(dir).above());
        out.add(cell.relative(dir));
        return out;
    }
}
