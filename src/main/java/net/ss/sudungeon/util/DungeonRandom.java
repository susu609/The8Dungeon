package net.ss.sudungeon.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonRandom {
    private static final ConcurrentHashMap<String, Random> rngMap = new ConcurrentHashMap<>();
    private static Random globalRandom;
    private static RandomSource globalRandomSource;
    private static long seed = 0L;

    // Cập nhật seed cho toàn bộ hệ thống DungeonRandom
    public static void setSeed(long dungeonSeed) {
        seed = dungeonSeed;
        globalRandom = new Random(seed);
        globalRandomSource = RandomSource.create(seed);
        rngMap.clear();
        Log.d("🔄 Dungeon Seed cập nhật: " + seed);
    }

    // Lấy seed hiện tại
    public static long getSeed() {
        return seed;
    }

    // Lấy một Random toàn cục cho hệ thống dungeon
    public static Random getRandom() {
        if (globalRandom == null) {
            globalRandom = new Random(seed);
        }
        return globalRandom;
    }

    // Lấy một RandomSource toàn cục cho hệ thống
    public static RandomSource getRandomSource() {
        if (globalRandomSource == null) {
            globalRandomSource = RandomSource.create(seed);
        }
        return globalRandomSource;
    }

    // Lấy một Random cụ thể dựa trên key (ví dụ liên quan đến phòng, vật phẩm, v.v.)
    public static Random getRNG(String key) {
        return rngMap.computeIfAbsent(key, k -> new Random(seed + k.hashCode()));
    }

    // Sinh một hướng ngẫu nhiên dựa trên vị trí BlockPos
    public static Direction getRandomDirection(BlockPos pos) {
        long combinedSeed = seed ^ pos.asLong();
        RandomSource rand = RandomSource.create(combinedSeed);
        return Direction.Plane.HORIZONTAL.getRandomDirection(rand);
    }

    // Quyết định có một sự kiện nào đó xảy ra dựa trên xác suất
    public static boolean decideWithProbability(double probability) {
        return getRandomSource().nextDouble() < probability;
    }
}