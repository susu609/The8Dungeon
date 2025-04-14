package net.ss.sudungeon.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GuiUtils extends GuiGraphics {

    public static final ResourceLocation BACKGROUND_TEXTURE = GetTextures.screen("background.png");
    // Kích thước của mỗi tile (mặc định là 32x32)
    private static final int TILE_SIZE = 32;

    private static final ResourceLocation[] STONE_BRICKS_TEXTURES = {
            new ResourceLocation("ss", "textures/block/stone_bricks.png"),
            new ResourceLocation("ss", "textures/block/cracked_stone_bricks.png"),
            new ResourceLocation("ss", "textures/block/mossy_stone_bricks.png")
    };

    // Texture fallback mặc định
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("minecraft", "textures/block/stone_bricks.png");

    // Bộ nhớ cache kiểm tra sự tồn tại của texture
    private static final Map<ResourceLocation, Boolean> TEXTURE_CACHE = new HashMap<>();

    private static final int[] TEXTURE_WEIGHTS = {50, 25, 25}; // Trọng số tương ứng

    public GuiUtils (Minecraft mc, MultiBufferSource.BufferSource buffer) {
        super(mc, buffer);
    }

    /**
     * Vẽ nền theo phong cách Slay the Spire với hiệu ứng mờ dần.
     */
    public static void drawBackground (GuiGraphics graphics, int width, int height, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F)); // Áp dụng alpha
        graphics.blit(BACKGROUND_TEXTURE, 0, 0, width, height, 0, 0, 16, 16, 16, 16); // Vẽ background
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset lại màu
    }

    /**
     * Vẽ nền mà không có hiệu ứng mờ dần.
     */
    public static void drawBackground (GuiGraphics graphics, int width, int height) {
        // Bật tính năng Blend để hỗ trợ hiệu ứng hiển thị (nếu cần)
        RenderSystem.enableBlend();

        // Đặt màu vẽ (không áp dụng alpha, tức không mờ)
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Alpha luôn là 1.0F (không trong suốt)

        // Vẽ texture nền lặp lại theo dạng tile (32x32 hoặc kích thước trong BACKGROUND_TEXTURE)
        graphics.blit(
                BACKGROUND_TEXTURE, // Đường dẫn đến texture (ảnh nền)
                0, 0,               // Tọa độ bắt đầu vẽ (x, y)
                width, height,      // Kích thước nền sẽ phủ (chiều rộng x chiều cao)
                0, 0,               // Góc trên bên trái của texture
                16, 16,             // Kích thước mặc định của tile
                16, 16              // Kích thước texture thực tế
        );

        // Tắt tính năng Blend sau khi vẽ xong
        RenderSystem.disableBlend();
    }

    /**
     * Vẽ viền cho UI theo phong cách card-based như Slay the Spire.
     */
    public static void drawBorder (GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 2, color); // Top
        graphics.fill(x, y, x + 2, y + height, color); // Left
        graphics.fill(x + width - 2, y, x + width, y + height, color); // Right
        graphics.fill(x, y + height - 2, x + width, y + height, color); // Bottom
    }

    /**
     * Hiển thị tooltip với hiệu ứng mờ dần.
     */
    public static void drawTooltip (GuiGraphics graphics, String text, int x, int y, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        int textWidth = mc.font.width(text);
        int backgroundColor = new Color(0, 0, 0, (int) (alpha * 255)).getRGB();
        graphics.fill(x - 2, y - 2, x + textWidth + 2, y + 10, backgroundColor);
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF);
    }

    public static void drawTexture (GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    public static void drawTopToBottomGradient (GuiGraphics guiGraphics, int width, int height, float alpha) {
        int gradientTopColor = 0xFF000000; // Màu đen đặc (100% alpha)
        int gradientBottomColor = 0x00000000; // Màu trong suốt (0% alpha)

        // Vẽ gradient phủ toàn màn hình
        guiGraphics.fillGradient(
                0,              // Vị trí X bắt đầu (bên trái màn hình)
                0,              // Vị trí Y bắt đầu (mép trên màn hình)
                width,          // Vị trí X kết thúc (bên phải màn hình)
                height,         // Vị trí Y kết thúc (mép dưới màn hình)
                adjustAlpha(gradientTopColor, alpha),  // Màu trên (đậm đặc với alpha hiệu ứng)
                gradientBottomColor                 // Màu dưới (trong suốt)
        );
    }

    // Điều chỉnh giá trị alpha cho dải màu gradient (hỗ trợ fade-in/mờ dần nếu cần)
    private static int adjustAlpha (int color, float alpha) {
        int a = (int) ((color >> 24 & 0xFF) * alpha); // Tính alpha mới dựa trên giá trị đầu vào
        return (color & 0x00FFFFFF) | (a << 24);      // Kết hợp màu RGB với alpha mới
    }

    private static ResourceLocation cachedBackgroundTexture;

    public static void generateCachedBackground (Minecraft mc, int width, int height) {
        BufferedImage backgroundImage = BackgroundRenderer.createBackground(width, height);

        // Chuyển "BufferedImage" sang một Texture dùng cho Minecraft
        cachedBackgroundTexture = MinecraftUtils.createTextureFromImage(mc, backgroundImage);
    }

    /**
     * Render nền "khối đá" (stone_bricks) với hiệu ứng random hóa.
     */
    public static void renderMovingBackground (GuiGraphics guiGraphics, ResourceLocation texture, int offsetY, float scale, int texW, int texH, int screenW, int screenH) {
        int scaledWidth = (int) (texW / scale);
        int scaledHeight = (int) (texH / scale);

        offsetY = offsetY % scaledHeight;
        int repeatsX = (int) Math.ceil((double) screenW / scaledWidth);
        int repeatsY = (int) Math.ceil((double) screenH / scaledHeight) + 1;

        RenderSystem.setShaderTexture(0, texture);
        for (int x = 0; x < repeatsX; x++) {
            for (int y = 0; y < repeatsY; y++) {
                int xPos = x * scaledWidth;
                int yPos = -offsetY + (y * scaledHeight);
                guiGraphics.blit(texture, xPos, yPos, 0, 0, scaledWidth, scaledHeight, texW, texH);
            }
        }
    }

    public static void drawProgressBar (GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, float alpha, float progress) {
        int width = Mth.ceil((float) (x1 - x0 - 2) * progress);
        int color = FastColor.ARGB32.color(Math.round(alpha * 255.0F), 255, 255, 255);

        // Filled progress bar
        guiGraphics.fill(x0 + 2, y0 + 2, x0 + width, y1 - 2, color);

        // Borders
        guiGraphics.fill(x0 + 1, y0, x1 - 1, y0 + 1, color); // Top
        guiGraphics.fill(x0 + 1, y1, x1 - 1, y1 - 1, color); // Bottom
        guiGraphics.fill(x0, y0, x0 + 1, y1, color); // Left
        guiGraphics.fill(x1, y0, x1 - 1, y1, color); // Right
    }

    /**
     * Kiểm tra texture có tồn tại trong hệ thống tài nguyên.
     *
     * @param texture Texture để kiểm tra.
     * @return true nếu tồn tại, ngược lại false.
     */
    private static boolean textureExists (ResourceLocation texture) {
        try {
            return Minecraft.getInstance().getResourceManager().getResource(texture).isPresent();
        } catch (Exception e) {
            Log.e("Error while checking texture existence: " + texture, e);
            return false;
        }
    }
}
