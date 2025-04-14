package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.ss.sudungeon.init.SsModEntities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DungeonFeatureSpawner {

    private static final List<ItemStack> POSSIBLE_ITEMS = List.of(
            new ItemStack(Items.WOODEN_SWORD),
            new ItemStack(Items.BOOK),
            new ItemStack(Items.EMERALD)
    );

    private static final List<Block> MOD_DECORATED_POT = List.of(Blocks.DECORATED_POT);
    private static final double DEFAULT_MUSHROOM_SPAWN_RATE = 0.25;
    private static final double DEFAULT_ENEMY_SPAWN_RATE = 0.2;
    private static final double DEFAULT_ITEM_SPAWN_RATE = 0.1;

    private final int minDecorations;
    private final int maxDecorations;
    private final double mushroomSpawnRate;
    private final double itemSpawnRate;

    // Tập hợp để lưu trữ các vị trí trang trí
    private final Set<BlockPos> decorationPositions = new HashSet<>();

    public DungeonFeatureSpawner () {
        this(3, 12, DEFAULT_MUSHROOM_SPAWN_RATE, DEFAULT_ENEMY_SPAWN_RATE, DEFAULT_ITEM_SPAWN_RATE);
    }

    public DungeonFeatureSpawner (int minDecorations, int maxDecorations, double mushroomSpawnRate, double enemySpawnRate, double itemSpawnRate) {
        this.minDecorations = minDecorations;
        this.maxDecorations = maxDecorations;
        this.mushroomSpawnRate = mushroomSpawnRate;
        this.itemSpawnRate = itemSpawnRate;
    }

    public void spawnDecorations (ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        int totalDecorations = minDecorations + random.nextInt(maxDecorations - minDecorations + 1);

        for (int i = 0; i < totalDecorations; i++) {
            BlockPos decorationPos = getRandomDecorationPos(world, roomPos, random);
            if (decorationPos != null) {
                spawnDecorationBlock(world, decorationPos, roomType, random);
                maybeSpawnItem(world, decorationPos, random);

                // Thêm vị trí trang trí vào tập hợp
                decorationPositions.add(decorationPos);
            }
        }

        // Gọi spawnModZombie tại một vị trí phù hợp
        maybeSpawnModZombie(world, roomPos, random);
    }

    private void spawnDecorationBlock (ServerLevel world, BlockPos pos, RoomType roomType, RandomSource random) {
        Block decorationBlock = chooseDecorationBlock(random, roomType);
        world.setBlockAndUpdate(pos, decorationBlock.defaultBlockState());
    }

    private void maybeSpawnItem (ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextDouble() < itemSpawnRate) {
            ItemStack item = selectRandomItem(random);
            spawnItemEntity(world, pos, item);
        }
    }

    private void spawnItemEntity (ServerLevel world, BlockPos pos, ItemStack item) {
        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
        world.addFreshEntity(itemEntity);
    }

    private ItemStack selectRandomItem (RandomSource random) {
        return POSSIBLE_ITEMS.get(random.nextInt(POSSIBLE_ITEMS.size()));
    }

    private Block chooseDecorationBlock (RandomSource random, RoomType roomType) {
        if (random.nextDouble() < mushroomSpawnRate) {
            return random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM;
        }
        return getRoomSpecificDecoration(roomType, random);
    }

    private Block getRoomSpecificDecoration (RoomType roomType, RandomSource random) {
        return switch (roomType) {
            case TREASURE -> random.nextInt(10) > 5 ? Blocks.CHEST : Blocks.GOLD_BLOCK;
            case BOSS -> Blocks.DRAGON_EGG;
            default -> getRandomDecorationBlock(random);
        };
    }

    private Block getRandomDecorationBlock (RandomSource random) {
        return MOD_DECORATED_POT.get(random.nextInt(MOD_DECORATED_POT.size()));
    }

    private BlockPos getRandomDecorationPos (ServerLevel world, BlockPos roomPos, RandomSource random) {
        int x = roomPos.getX() + 1 + random.nextInt(DrunkardWalk.ROOM_SIZE - 2);
        int z = roomPos.getZ() + 1 + random.nextInt(DrunkardWalk.ROOM_SIZE - 2);
        BlockPos pos = new BlockPos(x, roomPos.getY() + 1, z);
        return world.getBlockState(pos).isAir() ? pos : null;
    }

    private void maybeSpawnModZombie(ServerLevel world, BlockPos roomPos, RandomSource random) {
        // Tăng xác suất spawn
        double enhancedSpawnRate = DEFAULT_ENEMY_SPAWN_RATE * 1.5; // Tăng thêm 50% xác suất spawn

        // Tăng số lần thử spawn zombie trong mỗi phòng
        int spawnAttempts = 4; // Số lần spawn zombie

        for (int i = 0; i < spawnAttempts; i++) {
            BlockPos spawnPos = getRandomDecorationPos(world, roomPos, random);
            if (spawnPos != null && !decorationPositions.contains(spawnPos) && random.nextDouble() < enhancedSpawnRate) {
                spawnModZombie(world, spawnPos);
            }
        }
    }


    private void spawnModZombie (ServerLevel world, BlockPos spawnPos) {

    }

    private boolean isSpawnPositionValid (ServerLevel world, BlockPos spawnPos) {
        for (int y = 0; y <= 1; y++) {
            BlockPos checkPos = spawnPos.above(y);
            if (!world.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        return true;
    }
    // Phương thức tính toán maxChallengePoints mới (ví dụ)
    public static int calculateNewMaxChallengePoints (int currentMaxChallengePoints) {
        return currentMaxChallengePoints + 5; // Tăng 5 điểm mỗi lần vào dungeon
    }

    // Phương thức spawn quái vật dựa trên maxChallengePoints (bạn cần tự triển khai)
    public static void spawnMobsInRoom (ServerLevel world, BlockPos roomPos, int maxChallengePoints) {
        int maxMobs = 8; // Giới hạn tối đa số quái vật trong phòng
        int numMobs = world.random.nextInt(maxMobs) + 1; // Sinh số lượng quái vật ngẫu nhiên từ 1 đến maxMobs
        // ... (phần còn lại của phương thức giữ nguyên)
    }
}
