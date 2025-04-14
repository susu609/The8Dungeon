package net.ss.sudungeon.mixin;

import com.mojang.realmsclient.client.RealmsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.ss.sudungeon.client.gui.screens.RougeLikeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setInitialScreen", at = @At("HEAD"), cancellable = true)
    private void mixinSetInitialScreen (RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig.QuickPlayData quickPlayData, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;

        if (quickPlayData.isEnabled()) {
            // QuickPlay logic
            net.minecraft.client.quickplay.QuickPlay.connect(minecraft, quickPlayData, reloadInstance, realmsClient);
        } else if (minecraft.options.onboardAccessibility) {
            // Show onboarding accessibility screen
            minecraft.setScreen(new AccessibilityOnboardingScreen(minecraft.options));
        } else {
            minecraft.setScreen(new RougeLikeScreen(true));
        }

        // Cancel further execution of the original method
        ci.cancel();
    }

}