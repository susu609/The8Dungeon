package net.susu609.the8dungeon.combat;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CombatEvent {
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity().getLastHurtByMob() instanceof Player) {
            event.setStrength(event.getStrength() * 0.1F);    // quái chỉ lùi 10%
        }
    }
}
