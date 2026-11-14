package net.susu609.the8dungeon.ai;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Giác quan của bot, thiết kế để rẻ:
 * - quét khối theo ngân sách (BUDGET khối mỗi lần suy nghĩ), không bao giờ quét cả vùng trong một tick;
 * - kết quả được kiểm tra lại khi dùng, nên dữ liệu hơi cũ cũng không sai;
 * - truy vấn thực thể trong hộp nhỏ, chỉ mỗi vài tick.
 */
final class BotSenses {

    static final int LOG = 0, STONE = 1, DIRT = 2;

    private static final int R = 16, RY = 6;
    private static final int W = 2 * R + 1, H = 2 * RY + 1, TOTAL = W * W * H;
    private static final int BUDGET = 700;

    private final BlockPos[] best = new BlockPos[3];
    private final Long2IntOpenHashMap banned = new Long2IntOpenHashMap(); // vị trí -> tick hết hạn cấm
    private final Int2IntOpenHashMap ignoredItems = new Int2IntOpenHashMap();
    private final BlockPos.MutableBlockPos cursorPos = new BlockPos.MutableBlockPos();
    private int cursor = TOTAL;
    private BlockPos center = BlockPos.ZERO;

    LivingEntity hostile;
    double hostileDist = Double.MAX_VALUE;
    LivingEntity prey;
    ItemEntity item;

    BlockPos best(int kind) {
        return best[kind];
    }

    void ban(BlockPos pos, int until) {
        banned.put(pos.asLong(), until);
        for (int k = 0; k < best.length; k++) {
            if (pos.equals(best[k])) best[k] = null;
        }
    }

    void ignoreItem(ItemEntity e, int until) {
        ignoredItems.put(e.getId(), until);
    }

    /** Cập nhật giác quan. blocks=false khi đang trú ẩn để tiết kiệm CPU. */
    void update(BotPlayer bot, int now, boolean blocks) {
        ServerLevel level = bot.serverLevel();
        entities(bot, level, now);
        if (blocks) {
            validate(level, now);
            scan(bot, level, now);
        }
    }

    // ===== Thực thể =====

    private void entities(BotPlayer bot, ServerLevel level, int now) {
        hostile = null;
        prey = null;
        hostileDist = Double.MAX_VALUE;
        double preyD = Double.MAX_VALUE;
        AABB box = bot.getBoundingBox().inflate(18.0D, 8.0D, 18.0D);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != bot && x.isAlive())) {
            double d = e.distanceToSqr(bot);
            if (e instanceof Enemy && !(e instanceof EnderMan) && !(e instanceof Player)) {
                if (d < hostileDist) {
                    hostileDist = d;
                    hostile = e;
                }
            } else if (e instanceof Cow || e instanceof Pig || e instanceof Sheep || e instanceof Chicken) {
                if (!e.isBaby() && d < preyD) {
                    preyD = d;
                    prey = e;
                }
            }
        }
        if (hostile != null) hostileDist = Math.sqrt(hostileDist);

        item = null;
        double itemD = Double.MAX_VALUE;
        ignoredItems.values().removeIf((int until) -> until <= now);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, bot.getBoundingBox().inflate(12.0D, 5.0D, 12.0D),
                x -> x.isAlive() && !ignoredItems.containsKey(x.getId()))) {
            double d = ie.distanceToSqr(bot);
            if (d < itemD) {
                itemD = d;
                item = ie;
            }
        }
    }

    // ===== Khối =====

    private static boolean matches(int kind, BlockState s) {
        return switch (kind) {
            case LOG -> s.is(BlockTags.LOGS);
            case STONE -> s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE);
            default -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT);
        };
    }

    private void validate(ServerLevel level, int now) {
        banned.values().removeIf((int until) -> until <= now);
        for (int k = 0; k < best.length; k++) {
            BlockPos p = best[k];
            if (p != null && (banned.containsKey(p.asLong()) || !matches(k, level.getBlockState(p)))) best[k] = null;
        }
    }

    /** Quét tiếp phần còn lại của khối lập phương quanh bot, tối đa BUDGET khối. */
    private void scan(BotPlayer bot, ServerLevel level, int now) {
        if (cursor >= TOTAL) {
            cursor = 0;
            center = bot.blockPosition();
        }
        int end = Math.min(TOTAL, cursor + BUDGET);
        BlockPos here = bot.blockPosition();
        for (; cursor < end; cursor++) {
            int i = cursor;
            int x = i % W;
            int z = (i / W) % W;
            int y = i / (W * W);
            cursorPos.set(center.getX() + x - R, center.getY() + y - RY, center.getZ() + z - R);
            if (!level.hasChunkAt(cursorPos)) continue;
            BlockState s = level.getBlockState(cursorPos);
            if (s.isAir()) continue;
            for (int k = 0; k < 3; k++) {
                if (matches(k, s)) consider(k, level, here, now);
            }
        }
    }

    /** Quét nhanh khối nhỏ quanh bot, dùng ngay sau khi vừa phá xong để chặt tiếp cây. */
    void refreshNear(BotPlayer bot, int radius, int now) {
        ServerLevel level = bot.serverLevel();
        BlockPos here = bot.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    m.set(here.getX() + dx, here.getY() + dy, here.getZ() + dz);
                    BlockState s = level.getBlockState(m);
                    if (s.isAir()) continue;
                    for (int k = 0; k < 3; k++) {
                        if (matches(k, s)) {
                            cursorPos.set(m);
                            consider(k, level, here, now);
                        }
                    }
                }
            }
        }
    }

    private void consider(int kind, ServerLevel level, BlockPos here, int now) {
        BlockPos pos = cursorPos;
        if (banned.containsKey(pos.asLong())) return;
        BlockPos cur = best[kind];
        if (cur != null && cur.distSqr(here) <= pos.distSqr(here)) return;
        if (!safeAndExposed(level, pos)) return;
        best[kind] = pos.immutable();
    }

    /** Có ít nhất một mặt hở (với tới được) và không kề chất lỏng (tránh dung nham/nước tràn). */
    static boolean safeAndExposed(ServerLevel level, BlockPos pos) {
        boolean open = false;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            BlockState s = level.getBlockState(n);
            if (!s.getFluidState().isEmpty()) return false;
            if (s.isAir()) open = true;
        }
        return open;
    }
}
