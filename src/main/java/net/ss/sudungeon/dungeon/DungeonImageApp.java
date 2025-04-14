package net.ss.sudungeon.dungeon;

import java.util.Scanner;

public class DungeonImageApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Cho phép người dùng nhập seed
        System.out.print("Nhập seed (bấm Enter để dùng ngẫu nhiên): ");
        String seedInput = scanner.nextLine().trim();
        Long seed = (seedInput.isEmpty()) ? null : Long.parseLong(seedInput);

        // Tạo và sinh dungeon
        DungeonGenerator generator = new DungeonGenerator(20, 20, seed);
        generator.generate();
        int[][] dungeon = generator.getDungeon();

        // Xuất dungeon thành ảnh
        DungeonImageRenderer.renderDungeon(dungeon, "dungeon.png");
    }
}