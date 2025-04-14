package app;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class StoneBricksBackgroundApp extends JPanel {
    private static final int TILE_SIZE = 96;

    private static final String[] STONE_BRICKS_TEXTURES = {
            "stone_bricks",           // 50% tỉ lệ
            "cracked_stone_bricks",   // 25% tỉ lệ
            "mossy_stone_bricks"      // 25% tỉ lệ
    };

    private static final int[] TEXTURE_WEIGHTS = {50, 25, 25};

    private final java.util.Map<String, BufferedImage> textures = new java.util.HashMap<>(); // Lưu trữ kết cấu
    private Random random;
    private long currentSeed;

    public StoneBricksBackgroundApp(long seed) {
        setSeed(seed);
        loadTextures(); // Tải các kết cấu khi khởi tạo
    }

    private void loadTextures() {
        loadTexture("stone_bricks", "/assets/ss/textures/block/stone_bricks.png");
        loadTexture("cracked_stone_bricks", "/assets/ss/textures/block/cracked_stone_bricks.png");
        loadTexture("mossy_stone_bricks", "/assets/ss/textures/block/mossy_stone_bricks.png");
    }

    private void loadTexture(String key, String path) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream(path));
            textures.put(key, image);
        } catch (IOException | NullPointerException e) {
            System.err.println("Không thể tải kết cấu: " + path + " - " + e.getMessage());
        }
    }

    public void setSeed(long seed) {
        this.currentSeed = seed;
        this.random = new Random(seed);
        repaint();
    }

    private String getRandomTexture() {
        java.util.List<String> weightedTextures = new java.util.ArrayList<>();
        for (int i = 0; i < STONE_BRICKS_TEXTURES.length; i++) {
            for (int j = 0; j < TEXTURE_WEIGHTS[i]; j++) {
                weightedTextures.add(STONE_BRICKS_TEXTURES[i]);
            }
        }
        return weightedTextures.get(random.nextInt(weightedTextures.size()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int columns = (width + TILE_SIZE - 1) / TILE_SIZE;
        int rows = (height + TILE_SIZE - 1) / TILE_SIZE;

        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                String textureKey = getRandomTexture();
                BufferedImage texture = textures.get(textureKey);

                if (texture != null) {
                    g2d.drawImage(texture, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g2d.setColor(getDebugColor(textureKey));
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private Color getDebugColor(String textureKey) {
        return switch (textureKey) {
            case "stone_bricks" -> Color.GRAY;
            case "cracked_stone_bricks" -> Color.DARK_GRAY;
            case "mossy_stone_bricks" -> Color.GREEN;
            default -> Color.WHITE;
        };
    }

    /**
     * Lưu toàn bộ nền hiển thị ra file PNG.
     *
     * @param fileName Tên file PNG
     */
    public void saveAsPng(String fileName) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Vẽ toàn bộ giao diện lên BufferedImage
        paint(g2d);

        g2d.dispose(); // Giải phóng tài nguyên đồ họa

        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println("Nền đã được lưu vào file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể lưu file: " + fileName);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Stone Bricks Background (Random Textures with Seed)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        StoneBricksBackgroundApp content = new StoneBricksBackgroundApp(42); // Seed mặc định

        // Nút đặt Seed
        JButton seedButton = new JButton("Đặt Seed Mới");
        seedButton.addActionListener(e -> {
            String inputSeed = JOptionPane.showInputDialog(frame, "Nhập seed (số nguyên):", content.currentSeed);
            try {
                long newSeed = Long.parseLong(inputSeed); // Chuyển đổi sang số nguyên
                content.setSeed(newSeed); // Cập nhật seed
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Seed không hợp lệ! Vui lòng nhập một số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Nút lưu thành PNG
        JButton saveButton = new JButton("Xuất PNG");
        saveButton.addActionListener(e -> {
            String fileName = "background.png"; // Đặt tên file mặc định
            content.saveAsPng(fileName); // Lưu file
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(seedButton);
        buttonPanel.add(saveButton);

        frame.setLayout(new BorderLayout());
        frame.add(content, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}