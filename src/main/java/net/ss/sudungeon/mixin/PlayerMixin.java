/*
package net.ss.sudungeon.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ss.sudungeon.network.SsModVariables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    public void modifyCriticalHit(Entity target, CallbackInfo ci) {
        // Cast `this` to `Player` to access player-specific methods
        Player player = (Player) (Object) this;

        // Get player variables capability
        SsModVariables.PlayerVariables playerVars = player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables());

        // Custom critical hit logic
        boolean flag2 = Math.random() < playerVars.critRate; // Critical chance
        if (flag2) {
            // Apply critical damage multiplier
            float f = (float) (1.0F * playerVars.critDamageMultiple);

            // Play critical hit sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
        }
    }
}*/
