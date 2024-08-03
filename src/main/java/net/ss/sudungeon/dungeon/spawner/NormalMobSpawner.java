package net.ss.sudungeon.dungeon.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.NaturalSpawner;
import net.ss.sudungeon.SsMod;

import java.util.Optional;

public class NormalMobSpawner implements MobSpawner {
    @Override
    public void spawnMobs(ServerLevel world, BlockPos roomPos, RandomSource random, int dungeonLevel) {
        int maxMobsPerRoom = 8;
        int numMobs = random.nextInt(maxMobsPerRoom) + 1; // Sinh số lượng quái vật ngẫu nhiên từ 1 đến maxMobsPerRoom

        for (int i = 0; i < numMobs; i++) {
            BlockPos spawnPos = roomPos.offset(random.nextInt(14) + 1, 1, random.nextInt(14) + 1);
            // Lấy loại quái vật dựa trên roomLevel, cần truyền thêm vào giá trị roomLevel ở đây
            EntityType<? extends Monster> mobType = EntityType.ZOMBIE; // Giả định chỉ có 1 loại là zombie trong normal

            if (canSpawnMob(world, mobType, spawnPos)) {
                Optional.ofNullable(mobType.create(world)).ifPresent(monster -> {
                    monster.moveTo(spawnPos, 0.0F, 0.0F);
                    monster.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
                    world.addFreshEntity(monster);
                });
            } else {
                // Xử lý nếu không thể spawn mob ở vị trí này (ví dụ: in log hoặc thử lại với vị trí khác)
                SsMod.LOGGER.warn("Không thể spawn mob tại vị trí: {}", spawnPos);
            }
        }
    }

    private boolean canSpawnMob(ServerLevel world, EntityType<?> type, BlockPos pos) {
        return NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, pos, type);
    }


}