package net.susu609.the8dungeons.dungeon;

/**
 * Thông số sinh một lớp dungeon.
 *
 * @param gridW         số ô lưới theo X (mỗi ô 16 block)
 * @param gridD         số ô lưới theo Z
 * @param minRooms      số phòng tối thiểu, luôn được đảm bảo
 * @param maxRooms      số phòng tối đa của pha chính (pha thêm phòng cụt có thể
 *                      thêm vài phòng nữa)
 * @param minDeadEnds   số phòng cụt tối thiểu (không tính phòng vào)
 * @param presetWeights trọng số chọn kích thước phòng, theo THỨ TỰ của
 *                      RoomPreset.values()
 */
public record DungeonParams(int gridW, int gridD, int minRooms, int maxRooms,
                            int minDeadEnds, int[] presetWeights)
{

    //                                                       16x16  16x32  32x32
    public static final DungeonParams SMALL =
            new DungeonParams(7, 7, 6, 8, 3, new int[]{7, 3, 0});
    public static final DungeonParams STANDARD =
            new DungeonParams(9, 9, 10, 14, 4, new int[]{6, 3, 1});

    // Compact constructor: chạy trước khi gán trường, dùng để kiểm tra đầu vào.
    public DungeonParams {
        if (gridW < 3 || gridD < 3)
            throw new IllegalArgumentException("lưới tối thiểu 3x3");
        if (minRooms < 2 || maxRooms < minRooms)
            throw new IllegalArgumentException("minRooms/maxRooms sai");
        if (minDeadEnds < 0)
            throw new IllegalArgumentException("minDeadEnds < 0");
        if (presetWeights.length != RoomPreset.values().length)
            throw new IllegalArgumentException(
                    "presetWeights phải có " + RoomPreset.values().length +
                            " phần tử");
        int total = 0;
        for (int w : presetWeights) {
            if (w < 0)
                throw new IllegalArgumentException("trọng số âm");
            total += w;
        }
        if (total <= 0)
            throw new IllegalArgumentException("tổng trọng số phải > 0");
        presetWeights = presetWeights.clone();   // tránh bị sửa từ bên ngoài
    }
}
