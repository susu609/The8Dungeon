package net.ss.sudungeon.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModParticleTypes;
import net.ss.sudungeon.util.Log;

@Mod.EventBusSubscriber
public class HurtEventHandler {

    public static double latestDamageAmount = 0;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        float amount = event.getAmount();
        boolean isCritical = false;

        // Nếu nguồn sát thương có biến critChance
        if (attacker instanceof LivingEntity livingAttacker) {
            byte critChance = 0;
            // Roll ngẫu nhiên từ 0–99
            int roll = target.level().random.nextInt(100);
            if (roll < critChance) {
                isCritical = true;
                amount *= 1.5f; // tăng sát thương
            }
        }

        // Cập nhật biến global để render particle
        latestDamageAmount = isCritical ? amount * 2 : amount;

        // Áp dụng lại sát thương nếu đã thay đổi
        if (amount != event.getAmount()) {
            event.setAmount(amount);
        }

        // Gửi hiệu ứng "hurt" cho thực thể đặc biệt
        if (!target.level().isClientSide && target.getId() == 33) {
            target.level().broadcastEntityEvent(target, (byte)33);
        }

        // Tạo particle
        Vec3 pos = target.position();
        createParticles(target.level(), pos, target.getBbHeight(), target.getBbWidth(), new Vec3(0, 1, 0));
    }


    private static void createParticles(LevelAccessor world, Vec3 entityPosition, double entityHeight, double entityWidth, Vec3 sourcePosition) {
        double minSpeed = 0.0025; // Tốc độ tối thiểu
        double maxSpeed = 0.0050; // Tốc độ tối đa

        // Tính toán vector hướng từ thực thể đến người chơi
        Vec3 directionToSource = sourcePosition.subtract(entityPosition).normalize();

        // Đảm bảo hạt không bay về phía sau, chỉ bay sang hai bên hoặc phía trước
        double vx, vz;
        if (directionToSource.z > 0) {
            vx = (Math.random() - 0.5) * entityWidth + (Math.random() * (maxSpeed - minSpeed) + minSpeed);
            vz = (Math.random() - 0.5) * (maxSpeed - minSpeed) + minSpeed;
        } else {
            vx = (Math.random() - 0.5) * entityWidth + (Math.random() * (maxSpeed - minSpeed) + minSpeed);
            vz = (Math.random() - 0.5) * (maxSpeed - minSpeed) + minSpeed;
        }

        double vy = (Math.random() * (maxSpeed - minSpeed)) + minSpeed;

        double x = entityPosition.x + (Math.random() - 0.5) * entityWidth;
        double y = entityPosition.y + entityHeight;
        double z = entityPosition.z + (Math.random() - 0.5) * entityWidth;

        if (world.isClientSide()) {
            assert Minecraft.getInstance().level != null;
            Minecraft.getInstance().level.addParticle(SsModParticleTypes.NUMERIC_PARTICLE.get(), x, y, z, vx, vy, vz);
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(SsModParticleTypes.NUMERIC_PARTICLE.get(), x, y, z, 1, vx, vy, vz, 0.1);
        }
    }
}
