package net.ss.sudungeon.dungeon.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.NaturalSpawner;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;

import java.util.List;

import static net.ss.sudungeon.dungeon.spawner.MobInfo.MOB_TYPES;

public class EnemySpawner {
    private final RandomSource random = RandomSource.create();
    private static final int SPAWN_DELAY_TICKS = 100; // 5 giây * 20 ticks/giây
    private int tickCounter = 0;


    public void spawnEnemies(ServerLevel world, BlockPos roomPos, RoomType roomType, int dungeonLevel) {
        if (tickCounter < SPAWN_DELAY_TICKS) {
            tickCounter++;
            return;
        }
        tickCounter = 0;

        // Xác định bậc của phòng dựa trên roomType và dungeonLevel
        int roomLevel = getRoomLevel(roomType, dungeonLevel);
        if (roomLevel == 0) {
            return; // Không spawn quái vật nếu phòng không có bậc
        }

        MobSpawner spawner = switch (roomType) {
            case NORMAL -> new NormalMobSpawner();
            case ELITE -> new EliteMobSpawner(); // Tạo lớp EliteMobSpawner tương ứng
            case BOSS -> new BossMobSpawner();   // Tạo lớp BossMobSpawner tương ứng
            default -> null;
        };

        // Gọi MobSpawner để spawn quái vật
        if (spawner != null) {
            spawner.spawnMobs(world, roomPos, world.getRandom(), dungeonLevel); // Truyền vào dungeonLevel
        }
    }

    private int getRoomLevel(RoomType roomType, int dungeonLevel) {
        return switch (roomType) {
            case NORMAL -> 1; // Phòng NORMAL luôn có roomLevel là 1
            case ELITE -> 2;
            case BOSS -> 3;
            default -> 0;
        };
    }



    private EntityType<? extends Monster> getMobType (ServerLevel world, int roomLevel) {

        // Lọc danh sách quái vật dựa trên roomLevel
        List<MobInfo> eligibleMobs = MOB_TYPES.stream()
                .filter(mob -> mob.level() == roomLevel)
                .toList();

        // Tính tổng trọng số của các quái vật đủ điều kiện
        float totalWeight = eligibleMobs.stream()
                .map(MobInfo::spawnProbability)
                .reduce(0f, Float::sum);

        // Chọn ngẫu nhiên một quái vật dựa trên trọng số
        float randomValue = random.nextFloat() * totalWeight; // Sửa thành dùng random thay vì world.getRandom()


        for (MobInfo mob : eligibleMobs) {
            if (randomValue < mob.spawnProbability()) {
                return mob.type();
            }
            randomValue -= mob.spawnProbability();
        }

        return switch (roomLevel) {
            case 1 -> EntityType.ZOMBIE;
            case 2 -> EntityType.ENDERMAN;
            case 3 -> EntityType.WITHER;
            default -> throw new IllegalArgumentException("Invalid room level: " + roomLevel);
        };
    }

    private boolean canSpawnMob (ServerLevel world, EntityType<?> type, BlockPos pos) {
        return NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, pos, type);
    }

    // Trong lớp EnemySpawner
    private int getChallengeCost (EntityType<? extends Monster> entityType, ServerLevel world) {
        Monster tempEntity = entityType.create(world);
        if (tempEntity == null) {
            return 0;
        }

        double maxHealth = tempEntity.getMaxHealth();
        double attackDamage = tempEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double movementSpeed = tempEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
        tempEntity.discard();

        // Tính chi phí triệu hồi dựa trên các thuộc tính
        return (int) Math.round(maxHealth * 2 + attackDamage * 5 + movementSpeed * 3); // Ví dụ: trọng số khác nhau cho từng thuộc tính
    }
}