/*

package net.ss.sudungeon.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    // Inject into the 'setup' method of Camera
    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetup(BlockGetter blockGetter, Entity entity, boolean detached, boolean mirrored, float partialTicks, CallbackInfo ci) {
        // Override the 'detached' parameter to make the player model visible
        detached = false;  // This makes the camera behave as if it's in third-person view

        // Call the original method with the modified parameters
        ((Camera) (Object) this).setup(blockGetter, entity, detached, mirrored, partialTicks);

        // If needed, you can cancel further execution if this mixin alone is sufficient
        // ci.cancel(); // Uncomment this if you don't want the original method to run after this injection
    }
}

*/
