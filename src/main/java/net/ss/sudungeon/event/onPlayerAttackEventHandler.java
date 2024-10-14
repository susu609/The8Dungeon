package net.ss.sudungeon.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.network.SsModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class onPlayerAttackEventHandler {
    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent event) throws CommandSyntaxException {
        if (event == null || event.getEntity() == null) {
            return;
        }

        Entity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        LevelAccessor world = target.level();

        if (attacker instanceof Player player) {
            // Kiểm tra stamina trước khi tiếp tục tấn công
            float currentStamina = getPlayerCurrentStamina(player);
            if (currentStamina <= 0) {
                return; // Ngừng tấn công nếu stamina âm hoặc bằng 0
            }

            // Hủy sự kiện mặc định để thay thế bằng xử lý tùy chỉnh
            cancelEvent(event);

            // Gọi phương thức xử lý tấn công
            handlePlayerAttack(player, target, event.getAmount(), world);
        }
    }

    private static void cancelEvent(@Nullable Event event) {
        if (event != null && event.isCancelable()) {
            event.setCanceled(true);
        } else if (event != null && event.hasResult()) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static void handlePlayerAttack(Player player, Entity target, double baseDamage, LevelAccessor world) {
        // Lấy thuộc tính từ player
        double strength = getAttributeValue(player, Attributes.ATTACK_DAMAGE);
        double critRate = getAttributeValue(player, SsModAttributes.CRITICAL_HIT_CHANCE.get());
        double critMultiplier = getAttributeValue(player, SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get());

        // Điều chỉnh lượng sát thương dựa trên chỉ số strength
        double adjustedDamage = baseDamage + strength;

        // Kiểm tra có chí mạng hay không
        boolean isCritical = Math.random() < critRate;

        if (isCritical) {
            // Tính toán sát thương chí mạng
            double critDamage = adjustedDamage * critMultiplier;
            dealDamage(target, critDamage, world);

            playCritEffect(player, target, world);
        } else {
            // Gây sát thương thường nếu không chí mạng
            dealDamage(target, adjustedDamage, world);
        }

        // Giảm độ bền của vật phẩm khi người chơi tấn công
        decreaseItemDurability(player);
    }

    private static void decreaseItemDurability(Player player) {
        // Kiểm tra vật phẩm hiện tại của người chơi trên tay chính
        if (!player.getMainHandItem().isEmpty()) {
            // Giảm độ bền của vật phẩm, mặc định là 1
            player.getMainHandItem().hurtAndBreak(1, player, p -> {
                // Xử lý khi vật phẩm bị phá hủy hoàn toàn, ví dụ như chơi âm thanh vỡ
                p.broadcastBreakEvent(p.getUsedItemHand());
            });
        }
    }

    private static double getAttributeValue(Player player, Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }

    private static float getPlayerCurrentStamina(Player player) {
        return (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).stamina;
    }

    private static void setPlayerCurrentStamina(Player player, float value) {
        if (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).isPresent()) {
            player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> vars.stamina = value);
        }
    }

    private static void dealDamage(Entity target, double damage, LevelAccessor world) {
        target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.GENERIC)), (float) damage);
    }

    private static void playCritEffect(Player player, Entity target, LevelAccessor world) {
        // Phát âm thanh chí mạng
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);

        // Tạo hiệu ứng hạt cho đòn đánh chí mạng
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.2D);
        }
    }
}
