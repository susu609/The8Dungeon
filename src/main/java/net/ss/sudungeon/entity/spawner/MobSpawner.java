package net.ss.sudungeon.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public interface MobSpawner {
     void spawnMobs(ServerLevel world, BlockPos roomPos, RandomSource random, int dungeonLevel);
}