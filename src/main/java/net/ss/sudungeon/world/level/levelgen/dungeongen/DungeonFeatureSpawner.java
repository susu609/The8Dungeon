package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.ss.sudungeon.init.SsModEntities;
import net.ss.sudungeon.world.entity.ModZombie;

import java.util.List;

public class DungeonFeatureSpawner {

    private final int minDecorations;
    private final int maxDecorations;
    private final double mushroomSpawnRate;
    private final double enemySpawnRate;
    private final double itemSpawnRate;

    public DungeonFeatureSpawner() {
        this.minDecorations = 3;
        this.maxDecorations = 12;
        this.mushroomSpawnRate = 0.25;
        this.enemySpawnRate = 0.2;
        this.itemSpawnRate = 0.1; // Tỷ lệ spawn item là 10%
    }

    public void spawnDecorations(ServerLevel world, BlockPos roomPos, RoomType roomType, RandomSource random) {
        int totalDecorations = minDecorations + random.nextInt(maxDecorations - minDecorations + 1);

        for (int i = 0; i < totalDecorations; i++) {
            BlockPos decorationPos = getRandomDecorationPos(world, roomPos, random);
            if (decorationPos != null) {
                Block decorationBlock = chooseDecorationBlock(random, roomType);
                world.setBlockAndUpdate(decorationPos, decorationBlock.defaultBlockState());

                // Spawn enemy nếu là loại phòng bình thường và tỷ lệ cho phép
                if (roomType == RoomType.NORMAL && random.nextDouble() < enemySpawnRate) {
                    spawnModZombie(world, decorationPos, random);
                }

                // Spawn random item với tỷ lệ xác định
                if (random.nextDouble() < itemSpawnRate) {
                    spawnCustomItemEntity(world, decorationPos, random);
                }
            }
        }
    }

    private void spawnModZombie(ServerLevel world, BlockPos spawnPos, RandomSource random) {
        // Sử dụng đúng thực thể ModZombie từ SsModEntities
        ModZombie modZombie = new ModZombie(SsModEntities.MOD_ZOMBIE.get(), world);
        modZombie.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        modZombie.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null, null);
        modZombie.setPersistenceRequired();
        world.addFreshEntity(modZombie);
    }


    private void spawnCustomItemEntity(ServerLevel world, BlockPos spawnPos, RandomSource random) {
        // Tạo ra một danh sách các vật phẩm có thể thả
        List<ItemStack> possibleItems = List.of(
                new ItemStack(Items.WOODEN_SWORD),
                new ItemStack(Items.BOOK),
                new ItemStack(Items.EMERALD)
        );

        // Chọn ngẫu nhiên một item từ danh sách
        ItemStack selectedItem = possibleItems.get(random.nextInt(possibleItems.size()));

        // Tạo thực thể ItemEntity và thả xuống thế giới
        ItemEntity itemEntity = new ItemEntity(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, selectedItem);
        world.addFreshEntity(itemEntity);
    }

    private Block chooseDecorationBlock(RandomSource random, RoomType roomType) {
        if (random.nextDouble() < mushroomSpawnRate) {
            return random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM;
        }

        return switch (roomType) {
            case TREASURE -> random.nextInt(10) > 5 ? Blocks.CHEST : Blocks.GOLD_BLOCK;
            case BOSS -> Blocks.DRAGON_EGG;
            default -> getRandomDecorationBlock(random);
        };
    }

    private BlockPos getRandomDecorationPos(ServerLevel world, BlockPos roomPos, RandomSource random) {
        int x = roomPos.getX() + 1 + random.nextInt(DrunkardWalk.ROOM_SIZE - 2);
        int z = roomPos.getZ() + 1 + random.nextInt(DrunkardWalk.ROOM_SIZE - 2);
        BlockPos pos = new BlockPos(x, roomPos.getY() + 1, z);
        return world.getBlockState(pos).isAir() ? pos : null;
    }

    private Block getRandomDecorationBlock(RandomSource random) {
        List<Block> decorationBlocks = List.of(Blocks.DECORATED_POT);
        return decorationBlocks.get(random.nextInt(decorationBlocks.size()));
    }
}
