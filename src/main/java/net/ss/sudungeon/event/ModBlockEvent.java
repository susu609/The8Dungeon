package net.ss.sudungeon.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = SsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModBlockEvent {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final HashMap<Integer, Integer> levelBreakCountMap = new HashMap<>();

    // Xác suất ban đầu cho từng loại vật phẩm
    private static final double INITIAL_RESOURCE_CHANCE = 0.3; // 30% xác suất tài nguyên
    private static final double INITIAL_FOOD_CHANCE = 0.2;     // 20% xác suất thức ăn
    private static final double INITIAL_WEAPON_CHANCE = 0.1;   // 40% xác suất vũ khí

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        int currentLevel = pos.getY() / 16;

        if (world.getBlockState(pos).getBlock() == Blocks.DECORATED_POT) {
            ServerLevel serverWorld = (ServerLevel) world;

            // Lấy số lần phá hủy khối ở tầng hiện tại
            int breakCount = levelBreakCountMap.getOrDefault(currentLevel, 0);

            // Tính toán xác suất cho từng loại vật phẩm
            double resourceChance = Math.max(0, INITIAL_RESOURCE_CHANCE);
            double foodChance = Math.max(0, INITIAL_FOOD_CHANCE);
            double weaponChance = Math.max(0, INITIAL_WEAPON_CHANCE - (breakCount * 0.01));// Giảm % mỗi lần

            // Xác định loại vật phẩm sẽ rơi ra
            if (RANDOM.nextDouble() < resourceChance) {
                dropResource(serverWorld, pos);
            } else if (RANDOM.nextDouble() < foodChance) {
                dropFood(serverWorld, pos);
            } else if (RANDOM.nextDouble() < weaponChance) {
                dropWeapon(serverWorld, pos);
                // Cập nhật số lần phá hủy khối
                if (breakCount < 6) {
                    levelBreakCountMap.put(currentLevel, breakCount + 1);
                }
            }


        }
    }

    // Hàm để drop tài nguyên
    private static void dropResource (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] resources = new ItemStack[]{
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT)
        };
        ItemStack resource = resources[RANDOM.nextInt(resources.length)];
        serverWorld.getServer().execute(() -> {
            ItemEntity entityToSpawn = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), resource);
            entityToSpawn.setPickUpDelay(10);
            serverWorld.addFreshEntity(entityToSpawn);

            // Drop kinh nghiệm khi rơi tài nguyên
            ExperienceOrb experienceOrb = new ExperienceOrb(serverWorld, pos.getX(), pos.getY(), pos.getZ(), Mth.nextInt(RANDOM, 3, 7));
            serverWorld.addFreshEntity(experienceOrb);
        });
    }

    // Hàm để drop thức ăn
    private static void dropFood (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] foods = new ItemStack[]{
                new ItemStack(Items.BREAD),
                new ItemStack(Items.APPLE),
                new ItemStack(Items.POTATO),
                new ItemStack(Items.POISONOUS_POTATO),
                new ItemStack(Items.CARROT)
                
        };
        ItemStack food = foods[RANDOM.nextInt(foods.length)];
        serverWorld.getServer().execute(() -> {
            ItemEntity entityToSpawn = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), food);
            entityToSpawn.setPickUpDelay(10);
            serverWorld.addFreshEntity(entityToSpawn);

            // Drop kinh nghiệm khi rơi thức ăn
            ExperienceOrb experienceOrb = new ExperienceOrb(serverWorld, pos.getX(), pos.getY(), pos.getZ(), Mth.nextInt(RANDOM, 1, 4));
            serverWorld.addFreshEntity(experienceOrb);
        });
    }

    // Hàm để drop vũ khí
    private static void dropWeapon (ServerLevel serverWorld, BlockPos pos) {
        ItemStack[] weapons = new ItemStack[]{
                new ItemStack(Items.WOODEN_SWORD),
                new ItemStack(Items.WOODEN_AXE),
                new ItemStack(Items.WOODEN_PICKAXE)
        };
        ItemStack weapon = weapons[RANDOM.nextInt(weapons.length)];
        serverWorld.getServer().execute(() -> {
            ItemEntity entityToSpawn = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), weapon);
            entityToSpawn.setPickUpDelay(10);
            serverWorld.addFreshEntity(entityToSpawn);

            // Drop kinh nghiệm khi rơi vũ khí
            ExperienceOrb experienceOrb = new ExperienceOrb(serverWorld, pos.getX(), pos.getY(), pos.getZ(), Mth.nextInt(RANDOM, 5, 10));
            serverWorld.addFreshEntity(experienceOrb);
        });
    }
}
