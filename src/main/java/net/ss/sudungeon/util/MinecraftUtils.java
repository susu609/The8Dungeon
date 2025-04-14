package net.ss.sudungeon.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class MinecraftUtils {

    @SuppressWarnings("ConstantConditions")
    public static @NotNull Minecraft getMinecraft() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null)
            throw new RuntimeException("Minecraft instance is null.");
        return mc;
    }
    public static void assertMinecraftReady () {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        assert mc.level != null;
    }

    /**
     * Tạo một ResourceLocation từ BufferedImage
     *
     * @param mc            Minecraft instance để đăng ký (register) texture
     * @param bufferedImage Bức ảnh BufferedImage cần convert
     * @return ResourceLocation chỉ định texture đã tạo
     */
    public static ResourceLocation createTextureFromImage (Minecraft mc, BufferedImage bufferedImage) {
        try {
            NativeImage nativeImage = bufferedImageToNativeImage(bufferedImage);

            // Tạo DynamicTexture từ NativeImage
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            // Đăng ký DynamicTexture vào Minecraft và lấy ResourceLocation
            return mc.getTextureManager().register("custom_background", dynamicTexture);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert buffered image to native image", e);
        }
    }

    /**
     * Chuyển đổi BufferedImage thành NativeImage (Minecraft sử dụng NativeImage để render texture)
     */
    private static NativeImage bufferedImageToNativeImage (BufferedImage bufferedImage) throws IOException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        NativeImage nativeImage = new NativeImage(width, height, true); // True: hỗ trợ kênh alpha
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Lấy ARGB (Alpha, Red, Green, Blue) từ BufferedImage
                int argb = bufferedImage.getRGB(x, y);
                nativeImage.setPixelRGBA(x, y, argb);
            }
        }
        return nativeImage;
    }

    /**
     * Thực hiện hành động chỉ khi player tồn tại.
     *
     * @param action Hành động cần thực hiện với player
     */
    public static void withLocalPlayer(Consumer<LocalPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            action.accept(mc.player);
        }
    }
}