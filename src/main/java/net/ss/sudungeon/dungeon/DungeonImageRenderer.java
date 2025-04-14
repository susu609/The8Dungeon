package net.ss.sudungeon.dungeon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class DungeonImageRenderer {

    public static void renderDungeon(int[][] dungeon, String outputFile) {
        int cellSize = 2; // Giảm kích thước mỗi ô để ảnh dễ nhìn hơn
        int width = dungeon[0].length * cellSize;
        int height = dungeon.length * cellSize;

        // Tạo hình ảnh
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();

        // Duyệt qua dungeon và vẽ từng ô
        for (int y = 0; y < dungeon.length; y++) {
            for (int x = 0; x < dungeon[0].length; x++) {
                switch (dungeon[y][x]) {
                    case 0 -> g.setColor(Color.WHITE); // Lối đi/trống
                    case 1 -> g.setColor(Color.BLACK); // Tường
                    case 2 -> g.setColor(Color.GREEN); // Phòng bắt đầu
                    case 3 -> g.setColor(Color.RED);   // Phòng kết thúc
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        g.dispose();

        // Lưu hình ảnh
        try {
            ImageIO.write(img, "png", new File(outputFile));
            System.out.println("Ảnh dungeon đã được tạo: " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}