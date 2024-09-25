/*
package net.ss.sudungeon.mixin;


import io.socol.betterthirdperson.BetterThirdPerson;
import io.socol.betterthirdperson.api.TickPhase;
import io.socol.betterthirdperson.api.adapter.IMovementInputAdapter;
import io.socol.betterthirdperson.api.adapter.IPlayerAdapter;
import io.socol.betterthirdperson.impl.MovementInputAdapter;
import io.socol.betterthirdperson.impl.PlayerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class KeyboardInputMixin {
    public KeyboardInputMixin () {
    }

    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    public void tickHook (boolean moveSlowly, float sneakingSpeedBonus, CallbackInfo ci) {
        if (BetterThirdPerson.getCameraManager().hasCustomCamera()) {
            IPlayerAdapter player = new PlayerAdapter(Minecraft.getInstance().player);
            IMovementInputAdapter inputs = new MovementInputAdapter((Input) this);
            BetterThirdPerson.getCameraManager().handleMovementInputs(player, inputs, TickPhase.START);
        }

    }
}
*/
