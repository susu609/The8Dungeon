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

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModBlockEvent {
    private static final RandomSource RANDOM = RandomSource.create(); // Tạo một đối tượng RandomSource duy nhất

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel(); // Sử dụng Level thay vì LevelAccessor
        BlockPos pos = event.getPos();

        if (world.getBlockState(pos).getBlock() == Blocks.DECORATED_POT) {
            ServerLevel serverWorld = (ServerLevel) world;

            // Xác suất phần trăm để tạo ra các ItemEntity
            double emeraldChance = 0.4; // 40% xác suất
            double breadChance = 0.2;   // 20% xác suất

            // Tạo ItemEntity cho Emerald nếu xác suất thành công
            if (RANDOM.nextDouble() < emeraldChance) {
                int emeraldCount = Mth.nextInt(RANDOM, 0, 4); // Số lượng tối đa
                for (int index = 0; index < emeraldCount; index++) {
                    serverWorld.getServer().execute(() -> {
                        ItemEntity entityToSpawn = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.EMERALD));
                        entityToSpawn.setPickUpDelay(10);
                        serverWorld.addFreshEntity(entityToSpawn);
                    });
                }
            }

            // Tạo ItemEntity cho Bread nếu xác suất thành công
            if (RANDOM.nextDouble() < breadChance) {
                int breadCount = Mth.nextInt(RANDOM, 0, 2); // Số lượng tối đa
                for (int index = 0; index < breadCount; index++) {
                    serverWorld.getServer().execute(() -> {
                        ItemEntity entityToSpawn = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BREAD));
                        entityToSpawn.setPickUpDelay(10);
                        serverWorld.addFreshEntity(entityToSpawn);
                    });
                }
            }

            // Luôn tạo ExperienceOrb
            serverWorld.getServer().execute(() -> {
                ExperienceOrb experienceOrb = new ExperienceOrb(serverWorld, pos.getX(), pos.getY(), pos.getZ(), Mth.nextInt(RANDOM, 1, 16));
                serverWorld.addFreshEntity(experienceOrb);
            });
        }
    }
}