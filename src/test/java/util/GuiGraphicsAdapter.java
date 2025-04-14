package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GuiGraphicsAdapter {
    private final Graphics2D delegate;
    private final Map<String, BufferedImage> textures = new HashMap<>();

    public GuiGraphicsAdapter(Graphics2D delegate) {
        this.delegate = delegate;
        loadTexture("stone_bricks", "/assets/ss/textures/block/stone_bricks.png");
        loadTexture("cracked_stone_bricks", "/assets/ss/textures/block/cracked_stone_bricks.png");
        loadTexture("mossy_stone_bricks", "/assets/ss/textures/block/mossy_stone_bricks.png");
    }

    private void loadTexture(String key, String path) {
        try {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            textures.put(key, image);
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load texture: " + path + " - " + e.getMessage());
        }
    }

    public void blit(String textureKey, int x, int y, int width, int height) {
        BufferedImage texture = textures.get(textureKey);
        if (texture != null) {
            delegate.drawImage(texture, x, y, width, height, null);
        } else {
            Color debugColor = new Color(textureKey.hashCode(), true);
            delegate.setColor(debugColor);
            delegate.fillRect(x, y, width, height);
        }
    }

    // Thêm chức năng lưu hình ảnh ra file PNG
    public void saveToFile(BufferedImage image, String fileName) {
        try {
            File outputFile = new File(fileName);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}