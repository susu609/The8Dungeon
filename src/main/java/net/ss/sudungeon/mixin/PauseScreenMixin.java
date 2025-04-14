package net.ss.sudungeon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.ss.sudungeon.client.gui.screens.MainMenuScreen;
import net.ss.sudungeon.client.gui.screens.RougeLikeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void ss$replaceTitleScreen(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // Xử lý thoát level (giống như gốc)
        assert mc.level != null;
        mc.level.disconnect();
        mc.clearLevel();

        // Mở màn hình tuỳ chỉnh thay vì TitleScreen
        mc.setScreen(new RougeLikeScreen(true));

        ci.cancel(); // huỷ logic gốc
    }
}
