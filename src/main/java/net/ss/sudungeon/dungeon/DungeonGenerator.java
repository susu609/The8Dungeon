package net.ss.sudungeon.dungeon;

import java.util.Random;

public class DungeonGenerator {

    private final int roomSize = 4; // Kích thước 1 phòng (4x4 pixels)
    private final int pathSize = 2; // Kích thước hành lang nối (2 pixels)
    private final int spaceBetweenRooms = 1; // Khoảng cách giữa các phòng
    private final int width;
    private final int height;
    private final int[][] grid;
    private final int dungeonWidth;
    private final int dungeonHeight;
    private final int offsetX;
    private final int offsetY;
    private final Random random;

    public DungeonGenerator(int numRoomsX, int numRoomsY, Long seed) {
        this.dungeonWidth = numRoomsX * (roomSize + spaceBetweenRooms); // Tổng độ rộng hầm ngục
        this.dungeonHeight = numRoomsY * (roomSize + spaceBetweenRooms); // Tổng chiều cao hầm ngục

        // Kích thước tổng thể lưới grid (gấp đôi để căn giữa)
        this.width = dungeonWidth + 20;
        this.height = dungeonHeight + 20;

        // Tạo mảng lưới
        this.grid = new int[this.height][this.width];

        // Offset để căn giữa hầm ngục
        this.offsetX = (this.width - this.dungeonWidth) / 2;
        this.offsetY = (this.height - this.dungeonHeight) / 2;

        // Random seed
        this.random = (seed != null) ? new Random(seed) : new Random();
    }

    public void generate() {
        // Bắt đầu với lưới toàn tường
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 1; // 1 = tường
            }
        }

        // Đưa starting room vào ngẫu nhiên
        int currentRoomX = random.nextInt(dungeonWidth / (roomSize + spaceBetweenRooms));
        int currentRoomY = random.nextInt(dungeonHeight / (roomSize + spaceBetweenRooms));

        createRoom(currentRoomX, currentRoomY);
        grid[translateToPixel(currentRoomY, offsetY)][translateToPixel(currentRoomX, offsetX)] = 2; // Start room

        // Sinh các phòng khác dựa trên Drunkard's Walk
        int pathLength = random.nextInt(20) + 10; // Giới hạn số bước

        for (int i = 0; i < pathLength; i++) {
            // Random hướng di chuyển (0 = trên, 1 = dưới, 2 = trái, 3 = phải)
            int direction = random.nextInt(4);
            switch (direction) {
                case 0 -> currentRoomY = Math.max(0, currentRoomY - 1); // Đi lên
                case 1 -> currentRoomY = Math.min((dungeonHeight / (roomSize + spaceBetweenRooms)) - 1, currentRoomY + 1); // Đi xuống
                case 2 -> currentRoomX = Math.max(0, currentRoomX - 1); // Đi trái
                case 3 -> currentRoomX = Math.min((dungeonWidth / (roomSize + spaceBetweenRooms)) - 1, currentRoomX + 1); // Đi phải
            }

            createRoom(currentRoomX, currentRoomY);
            connectRooms(currentRoomX, currentRoomY, direction);
        }

        // Đặt phòng cuối cùng là điểm kết thúc
        grid[translateToPixel(currentRoomY, offsetY)][translateToPixel(currentRoomX, offsetX)] = 3; // End room
    }

    private int translateToPixel(int roomIndex, int offset) {
        return offset + roomIndex * (roomSize + spaceBetweenRooms) + roomSize / 2;
    }

    private void createRoom(int roomX, int roomY) {
        int startX = offsetX + roomX * (roomSize + spaceBetweenRooms);
        int startY = offsetY + roomY * (roomSize + spaceBetweenRooms);

        for (int y = startY; y < startY + roomSize; y++) {
            for (int x = startX; x < startX + roomSize; x++) {
                grid[y][x] = 0; // Tạo phòng (0 = lối đi)
            }
        }
    }

    private void connectRooms(int roomX, int roomY, int direction) {
        int startX = offsetX + roomX * (roomSize + spaceBetweenRooms);
        int startY = offsetY + roomY * (roomSize + spaceBetweenRooms);

        switch (direction) {
            case 0 -> {
                for (int x = startX + roomSize / 2 - pathSize / 2; x < startX + roomSize / 2 + pathSize / 2; x++) {
                    for (int y = startY - spaceBetweenRooms; y < startY; y++) {
                        if (y >= offsetY) grid[y][x] = 0;
                    }
                }
            }
            case 1 -> {
                for (int x = startX + roomSize / 2 - pathSize / 2; x < startX + roomSize / 2 + pathSize / 2; x++) {
                    for (int y = startY + roomSize; y < startY + roomSize + spaceBetweenRooms; y++) {
                        if (y < height - offsetY) grid[y][x] = 0;
                    }
                }
            }
            case 2 -> {
                for (int y = startY + roomSize / 2 - pathSize / 2; y < startY + roomSize / 2 + pathSize / 2; y++) {
                    for (int x = startX - spaceBetweenRooms; x < startX; x++) {
                        if (x >= offsetX) grid[y][x] = 0;
                    }
                }
            }
            case 3 -> {
                for (int y = startY + roomSize / 2 - pathSize / 2; y < startY + roomSize / 2 + pathSize / 2; y++) {
                    for (int x = startX + roomSize; x < startX + roomSize + spaceBetweenRooms; x++) {
                        if (x < width - offsetX) grid[y][x] = 0;
                    }
                }
            }
        }
    }

    public int[][] getDungeon() {
        return grid;
    }
}