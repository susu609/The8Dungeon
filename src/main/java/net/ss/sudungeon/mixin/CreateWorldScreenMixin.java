package net.ss.sudungeon.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.ss.sudungeon.util.Log;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Final
    @Shadow
    WorldCreationUiState uiState;

    @Shadow
    protected abstract void init ();

    @Unique
    private static final String WORLD_NAME = "RougeDungeon"; // Tên thế giới tạo mới (chỉ để debug)

    protected CreateWorldScreenMixin (Component title) {
        super(title);
        // Log constructor
        Log.i("[Mixin Log] Constructor của CreateWorldScreenMixin đã được gọi.");
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void beforeOnCreate (CallbackInfo ci) {
        Log.i("[Mixin Log] onCreate() bắt đầu.");
    }

    @Inject(method = "onCreate", at = @At("TAIL"))
    private void afterOnCreate (CallbackInfo ci) {
        Log.i("[Mixin Log] onCreate() đã chạy xong.");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectInit (CallbackInfo ci) {
        Log.i("[Mixin Log] Inject vào init() thành công.");
        uiState.setName(WORLD_NAME);
        uiState.setAllowCheats(true);

        // Kiểm tra minecraft không phải null trước khi sử dụng
        if (minecraft != null && minecraft.screen instanceof CreateWorldScreen createWorldScreen) {
            // Gọi onCreate() thông qua Accessor
            ((CreateWorldScreenAccessor) createWorldScreen).callOnCreate();
        } else {
            Log.w("[Mixin Log] Minecraft hoặc screen không hợp lệ, không thể gọi onCreate().");
        }
    }

}
