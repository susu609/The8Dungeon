package net.susu609.the8dungeons.dungeon;

/**
 * Thông số sinh một lớp dungeon.
 *
 * @param gridW          số ô lưới theo X (mỗi ô 16 block)
 * @param gridD          số ô lưới theo Z
 * @param minRooms       số phòng tối thiểu, luôn được đảm bảo
 * @param maxRooms       số phòng tối đa của pha chính (pha thêm phòng cụt có thể thêm vài phòng nữa)
 * @param minDeadEnds    số phòng cụt tối thiểu (không tính phòng vào)
 * @param presetWeights  trọng số chọn kích thước phòng, theo THỨ TỰ của RoomPreset.values()
 */
public record DungeonParams(int gridW, int gridD, int minRooms, int maxRooms,
                            int minDeadEnds, int[] presetWeights) {

    // Compact constructor: chạy trước khi gán trường, dùng để kiểm tra đầu vào.
    public DungeonParams {
        if (gridW < 3 || gridD < 3) throw new IllegalArgumentException("lưới tối thiểu 3x3");
        if (minRooms < 2 || maxRooms < minRooms) throw new IllegalArgumentException("minRooms/maxRooms sai");
        // Cần 1 phòng vào + 4 phòng cụt đặc biệt + (6 - 4) phòng thường để thay bằng ? / elite.
        int minNeeded = 1 + DungeonLayout.DEAD_END_SPECIALS
                + (DungeonLayout.TOTAL_SPECIALS - DungeonLayout.DEAD_END_SPECIALS);
        if (minRooms < minNeeded) throw new IllegalArgumentException("minRooms phải >= " + minNeeded);
        if (minDeadEnds < DungeonLayout.DEAD_END_SPECIALS)
            throw new IllegalArgumentException("minDeadEnds phải >= " + DungeonLayout.DEAD_END_SPECIALS);
        if (presetWeights.length != RoomPreset.values().length)
            throw new IllegalArgumentException("presetWeights phải có " + RoomPreset.values().length + " phần tử");
        int total = 0;
        for (int w : presetWeights) {
            if (w < 0) throw new IllegalArgumentException("trọng số âm");
            total += w;
        }
        if (total <= 0) throw new IllegalArgumentException("tổng trọng số phải > 0");
        presetWeights = presetWeights.clone();   // tránh bị sửa từ bên ngoài
    }

    // Mặc định duy nhất: lưới 13x13 chunk (208x208 block), phòng vào ở ô giữa (6, 6),
    // tối thiểu 16 phòng (tối đa 24 ở pha chính), tối thiểu 4 phòng cụt.
    //                                                    16x16  16x32  32x32
    public static final DungeonParams DEFAULT =
            new DungeonParams(13, 13, 16, 24, 4, new int[]{6, 3, 1});
}
