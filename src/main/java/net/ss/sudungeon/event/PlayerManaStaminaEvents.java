package net.ss.sudungeon.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.network.SsModVariables;

@Mod.EventBusSubscriber(value = {Dist.CLIENT, Dist.DEDICATED_SERVER})
public class PlayerManaStaminaEvents {

    // Đăng ký sự kiện để theo dõi quá trình hồi mana và stamina
    public PlayerManaStaminaEvents () {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onPlayerTick (TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            float currentStamina = (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).stamina;
            float maxStamina = getPlayerAttribute(player, SsModAttributes.MAX_STAMINA.get());

            // Giảm stamina khi chạy hoặc nhảy
            if (player.isSprinting() || player.fallDistance > 0) {
                if (currentStamina > 0) {
                    setPlayerCurrentStamina(player, Math.max(currentStamina - 0.1f, 0));
                } else {
                    // Ngăn chặn người chơi tiếp tục chạy hoặc nhảy khi stamina cạn và thêm hiệu ứng kiệt sức
                    player.setSprinting(false);
                    if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1)); // Thêm hiệu ứng Kiệt sức (Slowness) trong 5 giây
                    }
                }
            }

            // Hồi phục stamina
            if (currentStamina < maxStamina && player.getFoodData().getFoodLevel() > 0 && !player.isSprinting() && player.fallDistance == 0) {
                setPlayerCurrentStamina(player, Math.min(currentStamina + 0.05f, maxStamina));

                // Tiêu hao thức ăn khi hồi phục stamina
                player.getFoodData().addExhaustion(0.025f); // Tiêu hao thức ăn, tăng exhaustion
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            float currentStamina = getPlayerCurrentStamina(player);
            if (currentStamina > 0) {
                setPlayerCurrentStamina(player, Math.max(currentStamina - 0.2f, 0));
            } else {
                // Ngăn chặn nhảy khi stamina cạn và thêm hiệu ứng kiệt sức
                event.setCanceled(true);
                if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1)); // Thêm hiệu ứng Kiệt sức (Slowness) trong 5 giây
                }
            }
        }
    }

    public static float[] getCurrentManaAndStamina(Player player) {
        float currentMana = (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).mana;
        float currentStamina = (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).stamina;
        return new float[]{currentMana, currentStamina};
    }

    public static float getPlayerAttribute (Player player, Attribute attribute) {
        return player.getAttribute(attribute) != null ? (float) player.getAttribute(attribute).getValue() : 0.0f;
    }

    private static float getPlayerCurrentMana(Player player) {
        return (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).mana;
    }

    private static float getPlayerCurrentStamina(Player player) {
        return (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).stamina;
    }

    private static void setPlayerAttribute(Player player, Attribute attribute, float value) {
        if (player.getAttribute(attribute) != null) {
            player.getAttribute(attribute).setBaseValue(value);
        }
    }

    private static void setPlayerCurrentMana(Player player, float value) {
        if (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).isPresent()) {
            player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> vars.mana = value);
        }
    }

    private static void setPlayerCurrentStamina(Player player, float value) {
        if (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).isPresent()) {
            player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> vars.stamina = value);
        }
    }
}