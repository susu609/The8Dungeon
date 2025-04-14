package net.ss.sudungeon.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class BackgroundRenderer {
    private static final int TILE_SIZE = 50;
    private static final String[] TEXTURES = {"stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks"};
    private static final int[] TEXTURE_WEIGHTS = {50, 25, 25};

    /**
     * Tạo nền bằng BufferedImage (dành cho môi trường desktop/demo).
     */
    public static BufferedImage createBackground(int width, int height) {
        int columns = (width + TILE_SIZE - 1) / TILE_SIZE;
        int rows = (height + TILE_SIZE - 1) / TILE_SIZE;
        BufferedImage image = new BufferedImage(columns * TILE_SIZE, rows * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);

        Random random = new Random();
        Graphics2D g2d = image.createGraphics();

        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {
                String texture = getRandomTexture(random);
                Color debugColor = getDebugColor(texture);

                g2d.setColor(debugColor);
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        g2d.dispose();
        return image;
    }

    private static String getRandomTexture(Random random) {
        int totalWeight = 0;
        for (int weight : TEXTURE_WEIGHTS) totalWeight += weight;

        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (int i = 0; i < TEXTURE_WEIGHTS.length; i++) {
            currentWeight += TEXTURE_WEIGHTS[i];
            if (randomValue < currentWeight) {
                return TEXTURES[i];
            }
        }
        return TEXTURES[0];
    }

    private static Color getDebugColor(String textureKey) {
        return switch (textureKey) {
            case "stone_bricks" -> Color.GRAY;
            case "cracked_stone_bricks" -> Color.DARK_GRAY;
            case "mossy_stone_bricks" -> Color.GREEN;
            default -> Color.WHITE;
        };
    }
}