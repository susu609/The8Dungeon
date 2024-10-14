package net.ss.sudungeon.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModParticleTypes;

import java.util.Objects;

@Mod.EventBusSubscriber
public class onLivingHurtEventHandler {

    public static double latestDamageAmount = 0;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        double damageAmount = event.getAmount();

        if (damageAmount < 0) {
            damageAmount = 0;
        }

        // Lấy giá trị giáp và độ bền từ thực thể bị tấn công
        double armorPoints = target.getAttribute(Attributes.ARMOR) != null ? Objects.requireNonNull(target.getAttribute(Attributes.ARMOR)).getValue() : 0.0;
        double toughness = target.getAttribute(Attributes.ARMOR_TOUGHNESS) != null ? Objects.requireNonNull(target.getAttribute(Attributes.ARMOR_TOUGHNESS)).getValue() : 0.0;

        // Tính toán lượng sát thương sau khi áp dụng giáp và độ bền
        damageAmount = calculateDamageReduction(damageAmount, armorPoints, toughness);

        // Cập nhật giá trị sát thương mới
        event.setAmount((float) damageAmount);

        latestDamageAmount = damageAmount;

        if (event.getSource().getEntity() instanceof LivingEntity source) {
            if (source instanceof ServerPlayer player) {
                // Uncomment this if you want to show a message to the player
                // player.sendSystemMessage(Component.literal("Bạn đã gây " + damageAmount + " sát thương lên " + target.getName().getString()));
            }
        }

        Vec3 entityPosition = target.position();
        LevelAccessor world = target.level();

        // Truyền hướng bay lên trời (hướng Y dương)
        Vec3 direction = new Vec3(0, 1, 0);  // Hướng bay thẳng lên

        createParticles(world, entityPosition, target.getBbHeight(), target.getBbWidth(), direction);
    }

    private static double calculateDamageReduction(double baseDamage, double armorPoints, double toughness) {
        double toughnessFactor = toughness / 4.0;
        double reductionRatio = armorPoints / (armorPoints + 8.0);
        double damageAfterReduction = baseDamage * (1.0 - reductionRatio * (20 - toughnessFactor) / 20.0);
        return Math.max(0.0, damageAfterReduction);
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
