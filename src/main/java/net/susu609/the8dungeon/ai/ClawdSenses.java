package net.susu609.the8dungeon.ai;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.susu609.the8dungeon.world.entity.ClawdEntity;

/** Những gì Clawd "thấy" quanh chỗ nó đứng. Chỉ gọi trên luồng server. */
public final class ClawdSenses {

    private static final int NEAR = 8;
    private static final int ENTITY_RANGE = 16;
    private static final List<String> NOTABLE = List.of(
            "chest", "barrel", "door", "trapdoor", "lever", "button", "pressure_plate",
            "spawner", "lava", "water", "ore", "tnt", "redstone", "torch", "lantern");
    private static final String GRID_CHARS =
            "#ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private ClawdSenses() {}

    /** Bối cảnh gọn (~100 token) cho model giao tiếp. */
    public static String brief(ClawdEntity clawd, Player owner) {
        ServerLevel level = (ServerLevel) clawd.level();
        BlockPos pos = clawd.blockPosition();
        StringBuilder sb = new StringBuilder();
        sb.append("dim:").append(level.dimension().location().getPath())
          .append(" biome:").append(level.getBiome(pos).unwrapKey().map(k -> k.location().getPath()).orElse("?"))
          .append(" light:").append(level.getMaxLocalRawBrightness(pos))
          .append(" time:").append(level.isDay() ? "day" : "night")
          .append(" weather:").append(level.isThundering() ? "storm" : level.isRaining() ? "rain" : "clear")
          .append(" on:").append(id(level.getBlockState(pos.below())))
          .append(" clawd:").append(clawd.isOrderedToSit() ? "stay" : "follow");
        if (owner != null) {
            sb.append(" owner_hp:").append(Math.round(owner.getHealth()))
              .append('/').append(Math.round(owner.getMaxHealth()))
              .append(" owner_mode:").append(owner.isCreative() ? "creative" : "survival")
              .append(" owner_at:").append(rel(pos, owner.blockPosition()));
        }
        String room = roomInfo(level, pos);
        if (!room.isEmpty()) sb.append(" room:").append(room);
        sb.append("\nnear:").append(notableBlocks(level, pos));
        sb.append("\nmobs:").append(mobs(level, clawd));
        return sb.toString();
    }

    /** Bối cảnh chi tiết cho model lập kế hoạch: thêm lưới khối 13x13 ở 5 tầng (y -1..+3). */
    public static String detailed(ClawdEntity clawd, Player owner) {
        ServerLevel level = (ServerLevel) clawd.level();
        BlockPos o = clawd.blockPosition();
        Map<String, Character> palette = new LinkedHashMap<>();
        palette.put("air", '.');
        StringBuilder grid = new StringBuilder();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 3; dy++) {
            grid.append("y").append(dy >= 0 ? "+" : "").append(dy)
                .append(" (rows z-6..+6, cols x-6..+6)\n");
            for (int dz = -6; dz <= 6; dz++) {
                for (int dx = -6; dx <= 6; dx++) {
                    String id = id(level.getBlockState(p.set(o.getX() + dx, o.getY() + dy, o.getZ() + dz)));
                    Character c = palette.get(id);
                    if (c == null) {
                        int i = palette.size() - 1;
                        c = i < GRID_CHARS.length() ? GRID_CHARS.charAt(i) : '?';
                        palette.put(id, c);
                    }
                    grid.append(c);
                }
                grid.append('\n');
            }
        }
        StringBuilder legend = new StringBuilder("legend:");
        palette.forEach((id, c) -> legend.append(' ').append(c).append('=').append(id));
        return brief(clawd, owner) + "\n" + legend + "\n" + grid;
    }

    private static String notableBlocks(ServerLevel level, BlockPos o) {
        Map<String, Integer> count = new HashMap<>();
        Map<String, BlockPos> nearest = new HashMap<>();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -NEAR; dx <= NEAR; dx++) {
            for (int dy = -NEAR / 2; dy <= NEAR / 2; dy++) {
                for (int dz = -NEAR; dz <= NEAR; dz++) {
                    p.set(o.getX() + dx, o.getY() + dy, o.getZ() + dz);
                    String id = id(level.getBlockState(p));
                    if (NOTABLE.stream().noneMatch(id::contains)) continue;
                    count.merge(id, 1, Integer::sum);
                    BlockPos best = nearest.get(id);
                    if (best == null || o.distSqr(p) < o.distSqr(best)) nearest.put(id, p.immutable());
                }
            }
        }
        if (count.isEmpty()) return " none";
        StringBuilder sb = new StringBuilder();
        count.entrySet().stream()
                .sorted((a, b) -> Double.compare(o.distSqr(nearest.get(a.getKey())), o.distSqr(nearest.get(b.getKey()))))
                .limit(10)
                .forEach(e -> sb.append(' ').append(e.getKey()).append('x').append(e.getValue())
                        .append('@').append(rel(o, nearest.get(e.getKey()))));
        return sb.toString();
    }

    private static String mobs(ServerLevel level, ClawdEntity clawd) {
        AABB box = clawd.getBoundingBox().inflate(ENTITY_RANGE);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != clawd && e.isAlive() && !(e instanceof Player));
        if (list.isEmpty()) return " none";
        Map<String, Integer> count = new HashMap<>();
        Map<String, Double> dist = new HashMap<>();
        for (LivingEntity e : list) {
            // "!" đánh dấu quái thù địch
            String name = (e instanceof Enemy ? "!" : "") + BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
            count.merge(name, 1, Integer::sum);
            dist.merge(name, (double) e.distanceTo(clawd), Math::min);
        }
        StringBuilder sb = new StringBuilder();
        count.forEach((n, c) -> sb.append(' ').append(n).append('x').append(c)
                .append('~').append(Math.round(dist.get(n))).append('m'));
        return sb.toString();
    }

    /** TODO: nối với DungeonLayout.roomAt/roleOf khi biết cách đổi tọa độ khối sang ô phòng. */
    private static String roomInfo(ServerLevel level, BlockPos pos) {
        return "";
    }

    private static String id(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }

    private static String rel(BlockPos o, BlockPos p) {
        return sign(p.getX() - o.getX()) + "," + sign(p.getY() - o.getY()) + "," + sign(p.getZ() - o.getZ());
    }

    private static String sign(int v) {
        return v > 0 ? "+" + v : String.valueOf(v);
    }
}
