/*
package net.ss.sudungeon.mixin;

import io.socol.betterthirdperson.BetterThirdPerson;
import io.socol.betterthirdperson.api.TickPhase;
import io.socol.betterthirdperson.impl.PlayerAdapter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin({Entity.class})
public class EntityMixin {
    public EntityMixin () {
    }

    @Inject(
            method = {"turn"},
            at = {@At("HEAD")}
    )
    public void preTurnHook (CallbackInfo ci) {
        Entity entity = (Entity) this;
        if (entity instanceof LocalPlayer) {
            BetterThirdPerson.getCameraManager().onPlayerTurn(TickPhase.START, new PlayerAdapter((LocalPlayer) entity));
        }

    }

    @Inject(
            method = {"turn"},
            at = {@At("TAIL")}
    )
    public void postTurnHook (CallbackInfo ci) {
        Entity entity = (Entity) this;
        if (entity instanceof LocalPlayer player) {
            if (BetterThirdPerson.getCameraManager().onPlayerTurn(TickPhase.END, new PlayerAdapter(player)) && player.isPassenger()) {
                Objects.requireNonNull(player.getVehicle()).onPassengerTurned(player);
            }
        }

    }
}
*/
