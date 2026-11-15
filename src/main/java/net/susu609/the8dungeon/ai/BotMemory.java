package net.susu609.the8dungeon.ai;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Trí nhớ của từng bot, lưu theo thế giới:
 * - chunk đã đi qua và lần cuối ghé (để khám phá chỗ mới, bớt quay lại chỗ cũ);
 * - địa điểm đáng chú ý: nguồn nước, chỗ hợp làm base, hang, quặng.
 */
public final class BotMemory extends SavedData {

    private static final String NAME = "susu609_bot_memory";
    private static final int MAX_CHUNKS = 4096;
    private static final int MAX_POIS_PER_TYPE = 48;

    enum PoiType { WATER, SITE, CAVE, ORE }

    record Poi(PoiType type, BlockPos pos, long time) {}

    /** Trí nhớ của một bot. */
    static final class Mind {
        final Long2LongOpenHashMap visited = new Long2LongOpenHashMap(); // chunk -> lần cuối ghé (game time)
        final List<Poi> pois = new ArrayList<>();

        /** Lần cuối ghé chunk chứa pos, -1 nếu chưa từng. */
        long lastVisit(BlockPos pos) {
            return visited.getOrDefault(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), -1L);
        }

        /** Ghi nhớ một địa điểm; trả true nếu là địa điểm mới (không trùng chỗ đã biết trong 24 khối). */
        boolean remember(PoiType type, BlockPos pos, long time) {
            int same = 0;
            for (int i = 0; i < pois.size(); i++) {
                Poi p = pois.get(i);
                if (p.type() != type) continue;
                same++;
                if (p.pos().distSqr(pos) < 24 * 24) {
                    pois.set(i, new Poi(type, p.pos(), time)); // đã biết: chỉ cập nhật thời gian
                    return false;
                }
            }
            if (same >= MAX_POIS_PER_TYPE) { // đầy: bỏ địa điểm cũ nhất cùng loại
                Poi oldest = null;
                for (Poi p : pois) if (p.type() == type && (oldest == null || p.time() < oldest.time())) oldest = p;
                pois.remove(oldest);
            }
            pois.add(new Poi(type, pos.immutable(), time));
            return true;
        }

        void forget(PoiType type, BlockPos pos) {
            pois.removeIf(p -> p.type() == type && p.pos().distSqr(pos) < 4 * 4);
        }

        /** Địa điểm loại type gần pos nhất (trong maxDist), hoặc null. */
        Poi nearest(PoiType type, BlockPos pos, double maxDist) {
            Poi best = null;
            double bestD = maxDist * maxDist;
            for (Poi p : pois) {
                if (p.type() != type) continue;
                double d = p.pos().distSqr(pos);
                if (d < bestD) {
                    bestD = d;
                    best = p;
                }
            }
            return best;
        }

        void markVisited(BlockPos pos, long time) {
            visited.put(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), time);
            if (visited.size() > MAX_CHUNKS) { // quên bớt chunk ghé lâu nhất
                long oldestKey = 0, oldest = Long.MAX_VALUE;
                for (Long2LongOpenHashMap.Entry e : visited.long2LongEntrySet()) {
                    if (e.getLongValue() < oldest) {
                        oldest = e.getLongValue();
                        oldestKey = e.getLongKey();
                    }
                }
                visited.remove(oldestKey);
            }
        }
    }

    private final java.util.Map<String, Mind> minds = new java.util.HashMap<>();

    public static BotMemory get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BotMemory::load, BotMemory::new, NAME);
    }

    Mind of(String bot) {
        setDirty(); // trí nhớ thay đổi liên tục: lưu cùng lượt lưu thế giới
        return minds.computeIfAbsent(bot, k -> new Mind());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        minds.forEach((bot, m) -> {
            CompoundTag t = new CompoundTag();
            long[] keys = new long[m.visited.size()], times = new long[m.visited.size()];
            int i = 0;
            for (Long2LongOpenHashMap.Entry e : m.visited.long2LongEntrySet()) {
                keys[i] = e.getLongKey();
                times[i++] = e.getLongValue();
            }
            t.putLongArray("chunks", keys);
            t.putLongArray("times", times);
            ListTag pl = new ListTag();
            for (Poi p : m.pois) {
                CompoundTag pt = new CompoundTag();
                pt.putString("type", p.type().name());
                pt.putLong("pos", p.pos().asLong());
                pt.putLong("time", p.time());
                pl.add(pt);
            }
            t.put("pois", pl);
            tag.put(bot, t);
        });
        return tag;
    }

    private static BotMemory load(CompoundTag tag) {
        BotMemory s = new BotMemory();
        for (String bot : tag.getAllKeys()) {
            CompoundTag t = tag.getCompound(bot);
            Mind m = new Mind();
            long[] keys = t.getLongArray("chunks"), times = t.getLongArray("times");
            for (int i = 0; i < Math.min(keys.length, times.length); i++) m.visited.put(keys[i], times[i]);
            for (Tag x : t.getList("pois", Tag.TAG_COMPOUND)) {
                CompoundTag pt = (CompoundTag) x;
                try {
                    m.pois.add(new Poi(PoiType.valueOf(pt.getString("type")), BlockPos.of(pt.getLong("pos")), pt.getLong("time")));
                } catch (IllegalArgumentException ignored) {
                }
            }
            s.minds.put(bot, m);
        }
        return s;
    }
}
