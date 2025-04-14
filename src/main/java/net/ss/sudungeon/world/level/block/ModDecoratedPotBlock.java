package net.ss.sudungeon.world.level.block;/*
package net.ss.sudungeon.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.ss.sudungeon.init.SsModItems;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class ModDecoratedPotBlock extends DecoratedPotBlock {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final HashMap<Integer, Integer> levelBreakCountMap = new HashMap<>();

    // Xác suất ban đầu cho từng loại vật phẩm
    private static final double INITIAL_RESOURCE_CHANCE = 0.3; // 30% xác suất tài nguyên
    private static final double INITIAL_FOOD_CHANCE = 0.2;     // 20% xác suất thức ăn
    private static final double INITIAL_WEAPON_CHANCE = 0.05;  // 5% xác suất vũ khí

    public ModDecoratedPotBlock (Properties p_273064_) {
        super(p_273064_);
    }

    @Override
    public void playerWillDestroy (@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        super.playerWillDestroy(world, pos, state, player);

        if (world instanceof ServerLevel serverWorld) {
            int currentLevel = pos.getY() / 16;
            int breakCount = levelBreakCountMap.getOrDefault(currentLevel, 0);

            double resourceChance = Math.max(0, INITIAL_RESOURCE_CHANCE);
            double foodChance = Math.max(0, INITIAL_FOOD_CHANCE);
            double weaponChance = Math.max(0, INITIAL_WEAPON_CHANCE - (breakCount * 0.01)); // Giảm % mỗi lần

            if (RANDOM.nextDouble() < resourceChance) {
                dropResource(serverWorld, pos);
            } else if (RANDOM.nextDouble() < foodChance) {
                dropFood(serverWorld, pos);
            } else if (RANDOM.nextDouble() < weaponChance) {
                dropWeapon(serverWorld, pos);
                if (breakCount < 6) {
                    levelBreakCountMap.put(currentLevel, breakCount + 1);
                }
            }
        }
    }

    private void dropResource (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] resources = new ItemStack[]{
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT)
        };
        ItemStack resource = resources[RANDOM.nextInt(resources.length)];
        spawnItemEntity(serverWorld, pos, resource, Mth.nextInt(RANDOM, 3, 7));
    }

    private void dropFood (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] foods = new ItemStack[]{
                new ItemStack(Items.BREAD),
                new ItemStack(Items.APPLE),
                new ItemStack(Items.POTATO),
                new ItemStack(Items.POISONOUS_POTATO),
                new ItemStack(Items.CARROT)
        };
        ItemStack food = foods[RANDOM.nextInt(foods.length)];
        spawnItemEntity(serverWorld, pos, food, Mth.nextInt(RANDOM, 1, 4));
    }

    private void dropWeapon (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] weapons = new ItemStack[]{
                new ItemStack(SsModItems.WOODEN_SWORD.get())
        };
        ItemStack weapon = weapons[RANDOM.nextInt(weapons.length)];
        spawnItemEntity(serverWorld, pos, weapon, Mth.nextInt(RANDOM, 5, 10));
    }

    private void spawnItemEntity (ServerLevel serverWorld, BlockPos pos, ItemStack itemStack, int xpAmount) {
        serverWorld.getServer().execute(() -> {
            ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), itemStack);
            itemEntity.setPickUpDelay(10);
            serverWorld.addFreshEntity(itemEntity);

            ExperienceOrb experienceOrb = new ExperienceOrb(serverWorld, pos.getX(), pos.getY(), pos.getZ(), xpAmount);
            serverWorld.addFreshEntity(experienceOrb);
        });
    }

    @Override
    public List<ItemStack> getDrops ( BlockState state, LootParams.Builder builder) {
        return null;
    }
}
*/
