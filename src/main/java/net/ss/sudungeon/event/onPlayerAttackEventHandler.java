package net.ss.sudungeon.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.world.entity.ModZombie;

import javax.annotation.Nullable;

import static net.ss.sudungeon.SsMod.LOGGER;

@Mod.EventBusSubscriber
public class onPlayerAttackEventHandler {
    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent event) throws CommandSyntaxException {
        if (event != null && event.getEntity() != null) {
            execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity(), event.getSource().getEntity(), event.getAmount());
        }
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) throws CommandSyntaxException {
        if (entity == null || sourceentity == null)
            return;

        if (sourceentity instanceof Player || sourceentity instanceof ServerPlayer) {
            if (event != null && event.isCancelable()) {
                event.setCanceled(true);
            } else if (event != null && event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }

            // Lấy chỉ số strength và crit từ người chơi
            double strength = sourceentity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new SsModVariables.PlayerVariables()).strength;
            double critRate = sourceentity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new SsModVariables.PlayerVariables()).critRate;
            double critMultiplier = sourceentity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new SsModVariables.PlayerVariables()).critDamageMultiple;

            // Điều chỉnh lượng sát thương dựa trên chỉ số strength
            double adjustedDamage = amount + strength;

            // Kiểm tra có chí mạng hay không
            boolean isCritical = Math.random() < critRate;

            // Nếu chí mạng, nhân sát thương lên và tạo hiệu ứng
            if (isCritical) {
                // Ghi log sát thương chí mạng
                double critDamage = (adjustedDamage * critMultiplier);
                entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), (float) critDamage);
                LOGGER.info("Critical hit! Damage dealt: " + critDamage);

                // Thêm âm thanh và hiệu ứng cho cú đánh chí mạng
                if (sourceentity instanceof Player player) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY(0.5D), entity.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.2D);
                    }
                }
            } else {
                // Gây sát thương thường nếu không chí mạng
                entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), (float) adjustedDamage);
            }
        }
/*        if (sourceentity instanceof ModZombie) {
            if (event != null && event.isCancelable()) {
                event.setCanceled(true);
            } else if (event != null && event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
        }*/

    }
}