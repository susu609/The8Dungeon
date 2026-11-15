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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Giác quan của bot, thiết kế để rẻ:
 * - quét khối theo ngân sách (theo luật scanBudget mỗi lần suy nghĩ), không bao giờ quét cả vùng trong một tick;
 * - kết quả được kiểm tra lại khi dùng, nên dữ liệu hơi cũ cũng không sai;
 * - truy vấn thực thể trong hộp nhỏ, chỉ mỗi vài tick.
 */
final class BotSenses {

    static final int LOG = 0, STONE = 1, DIRT = 2, BED = 3, TABLE = 4;
    private static final int KINDS = 5;

    private static final int R = 16, RY = 6;
    private static final int W = 2 * R + 1, H = 2 * RY + 1, TOTAL = W * W * H;

    private final BlockPos[] best = new BlockPos[KINDS];
    private final Long2IntOpenHashMap banned = new Long2IntOpenHashMap(); // vị trí -> tick hết hạn cấm
    private final Int2IntOpenHashMap ignoredItems = new Int2IntOpenHashMap();
    private final BlockPos.MutableBlockPos cursorPos = new BlockPos.MutableBlockPos();
    private int cursor = TOTAL;
    private BlockPos center = BlockPos.ZERO;

    // ===== Tầm nhìn: nón 120° trước mặt + không bị khối che; phía sau là mù =====
    private static final float HALF_FOV = 60.0F;
    private static final int MEMORY_TICKS = 100;   // thấy rồi thì nhớ 5 giây
    private final Int2IntOpenHashMap seenUntil = new Int2IntOpenHashMap();

    // ===== Thính giác =====
    static final int SOUND_ALERT = 1;   // tiếng quái: quay đầu nhìn
    static final int SOUND_DANGER = 2;  // creeper xì, TNT, nổ: chạy
    net.minecraft.world.phys.Vec3 heardPos;
    int heardKind;
    int heardAt = -100000;

    LivingEntity hostile;
    double hostileDist = Double.MAX_VALUE;
    LivingEntity prey;
    LivingEntity sheep; // cừu gần nhất (giết lấy len làm giường)
    LivingEntity creeper; // creeper gần nhất, tách riêng: không đánh, chỉ tránh
    double creeperDist = Double.MAX_VALUE;
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
        if (e == item) item = null; // bỏ ngay, không chờ lần quét kế tiếp
    }

    /** Cập nhật giác quan. blocks=false khi đang trú ẩn để tiết kiệm CPU. */
    void update(BotPlayer bot, int now, boolean blocks, int budget) {
        ServerLevel level = bot.serverLevel();
        entities(bot, level, now);
        if (blocks) {
            validate(level, now);
            scan(bot, level, now, budget);
        }
    }

    /** Bot có nhận biết được thực thể này không: sát bên, còn trong trí nhớ, hoặc trong nón nhìn và không bị che. */
    boolean perceives(BotPlayer bot, net.minecraft.world.entity.Entity e, double distSqr, int now) {
        if (distSqr < 1.5D * 1.5D) return true;                 // sát bên: cảm nhận được dù không nhìn
        if (seenUntil.get(e.getId()) > now) return true;       // vừa thấy gần đây
        double dx = e.getX() - bot.getX();
        double dz = e.getZ() - bot.getZ();
        float toward = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        if (Math.abs(net.minecraft.util.Mth.wrapDegrees(toward - bot.getYHeadRot())) > HALF_FOV) return false;
        if (!bot.hasLineOfSight(e)) return false;               // raycast đắt nhất nên để cuối
        seenUntil.put(e.getId(), now + MEMORY_TICKS);
        return true;
    }

    /** Ghi nhớ một thực thể dù không nhìn thấy (bị nó đánh, nghe tiếng nó). */
    void remember(net.minecraft.world.entity.Entity e, int now) {
        seenUntil.put(e.getId(), now + MEMORY_TICKS);
    }

    void hear(net.minecraft.world.phys.Vec3 pos, int kind, int now) {
        // tiếng nguy hiểm không bị tiếng thường đè lên trong lúc còn mới
        if (kind < heardKind && now - heardAt < 40) return;
        heardPos = pos;
        heardKind = kind;
        heardAt = now;
    }

    // ===== Thực thể =====

    private void entities(BotPlayer bot, ServerLevel level, int now) {
        hostile = null;
        prey = null;
        hostileDist = Double.MAX_VALUE;
        creeper = null;
        creeperDist = Double.MAX_VALUE;
        double preyD = Double.MAX_VALUE;
        sheep = null;
        double sheepD = Double.MAX_VALUE;
        seenUntil.int2IntEntrySet().removeIf(en -> en.getIntValue() <= now);
        AABB box = bot.getBoundingBox().inflate(18.0D, 8.0D, 18.0D);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != bot && x.isAlive())) {
            double d = e.distanceToSqr(bot);
            if (!perceives(bot, e, d, now)) continue; // phía sau / bị che: không biết
            if (e instanceof Creeper) {
                if (d < creeperDist) {
                    creeperDist = d;
                    creeper = e;
                }
            } else if (e instanceof Enemy && !(e instanceof EnderMan) && !(e instanceof Player)) {
                if (d < hostileDist) {
                    hostileDist = d;
                    hostile = e;
                }
            } else if (e instanceof Cow || e instanceof Pig || e instanceof Sheep || e instanceof Chicken) {
                if (!e.isBaby() && d < preyD) {
                    preyD = d;
                    prey = e;
                }
                if (e instanceof Sheep && !e.isBaby() && d < sheepD) {
                    sheepD = d;
                    sheep = e;
                }
            }
        }
        if (hostile != null) hostileDist = Math.sqrt(hostileDist);
        if (creeper != null) creeperDist = Math.sqrt(creeperDist);

        item = null;
        double itemD = Double.MAX_VALUE;
        ignoredItems.values().removeIf((int until) -> until <= now);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, bot.getBoundingBox().inflate(12.0D, 5.0D, 12.0D),
                x -> x.isAlive() && !ignoredItems.containsKey(x.getId()))) {
            double d = ie.distanceToSqr(bot);
            if (d < itemD && perceives(bot, ie, d, now)) {
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
            case DIRT -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT);
            case TABLE -> s.is(Blocks.CRAFTING_TABLE);
            // giường trống: chỉ lấy phần đầu giường (vị trí dùng để nằm)
            default -> s.is(BlockTags.BEDS) && s.getValue(net.minecraft.world.level.block.BedBlock.PART)
                    == net.minecraft.world.level.block.state.properties.BedPart.HEAD
                    && !s.getValue(net.minecraft.world.level.block.BedBlock.OCCUPIED);
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
    private void scan(BotPlayer bot, ServerLevel level, int now, int budget) {
        if (cursor >= TOTAL) {
            cursor = 0;
            center = bot.blockPosition();
        }
        int end = Math.min(TOTAL, cursor + budget);
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
            for (int k = 0; k < KINDS; k++) {
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
                    for (int k = 0; k < KINDS; k++) {
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
        if (kind != BED && kind != TABLE && !safeAndExposed(level, pos)) return;
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
