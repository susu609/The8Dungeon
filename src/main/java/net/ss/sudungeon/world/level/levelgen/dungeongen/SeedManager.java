package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

public record SeedManager(long seed) {

    public Direction getRandomDirection (BlockPos pos) {
        long combinedSeed = this.seed ^ pos.asLong();
        RandomSource rand = RandomSource.create(combinedSeed); // Sử dụng RandomSource
        return Direction.Plane.HORIZONTAL.getRandomDirection(rand);
    }

    public RandomSource getRandom () { // Trả về RandomSource
        return RandomSource.create(seed);
    }
}