package net.ss.sudungeon.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin (Level level, BlockPos pos, float yRot, GameProfile profile) {
        super(level, pos, yRot, profile);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void preventDeath (DamageSource source, CallbackInfo ci) {
        if (this.level().dimension().location().equals(new ResourceLocation("ss", "dungeon_dimension"))) {
            this.setHealth(this.getMaxHealth()); // Hồi máu
            this.setPos(this.getX(), 77, this.getZ()); // Dịch lên lại
            this.setDeltaMovement(Vec3.ZERO);
            ci.cancel(); // Huỷ chết
        }
    }
}
