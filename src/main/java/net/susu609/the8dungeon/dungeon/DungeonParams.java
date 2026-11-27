package net.susu609.the8dungeon.dungeon;

/**
 * Thông số sinh một lớp dungeon.
 *
 * @param gridW         số ô lưới theo X (mỗi ô 16 block)
 * @param gridD         số ô lưới theo Z
 * @param minRooms      số phòng tối thiểu, luôn được đảm bảo
 * @param maxRooms      số phòng tối đa của pha chính (pha thêm phòng cụt có thể thêm vài phòng nữa)
 * @param minDeadEnds   số phòng cụt tối thiểu (không tính phòng vào)
 * @param presetWeights trọng số chọn kích thước phòng, theo THỨ TỰ của RoomPreset.values()
 */
public record DungeonParams(int gridW, int gridD, int minRooms, int maxRooms, int minDeadEnds, int[] presetWeights,
                            DungeonLayout.Role[] deadEndRoles,   // phòng cụt đặc biệt, KHÔNG tính BOSS
                            int maxMystery, int maxElite) {
    // Mặc định duy nhất: lưới 13x13 chunk (208x208 block), phòng vào ở ô giữa (6, 6),
    // tối thiểu 16 phòng (tối đa 24 ở pha chính), tối thiểu 4 phòng cụt.
    //                                                    16x16  16x32  32x32
    // TODO (tầng 1): phòng boss là phòng 1x2 (R16X32) DUY NHẤT của tầng 1; mọi phòng thường chỉ 1x1.
    //       presetWeights hiện {9,1,0} cho 10% phòng thường là 1x2, MÂU THUẪN với quy tắc này: tầng 1 phải là {1,0,0}
    //       và phòng boss được ép thành R16X32 ở DungeonLayout (xem TODO trong assignRoles).
    // TODO (tầng sau): trọng số R16X32 tăng dần theo số tầng (gợi ý: tầng 2 = {9,1,0}); cần chốt bảng số từng tầng.
    //       Hiện chỉ có một DEFAULT cho mọi tầng; cần thêm cách chọn bộ thông số theo số tầng.
    public static final int GRID = 13;   // CỐ ĐỊNH mọi tầng
    public static final DungeonParams FLOOR_1 = new DungeonParams(GRID, GRID, 8, 10,                                   // 8..10 phòng (cũ 16..24)
            3,                                       // đúng 3 cụt: boss + kho báu + shop
            new int[]{1, 0, 0},                      // chỉ phòng 1x1
            new DungeonLayout.Role[]{DungeonLayout.Role.TREASURE, DungeonLayout.Role.SHOP}, 1, 0);                                   // 1 phòng "?", chưa có elite
    public static final DungeonParams DEFAULT = FLOOR_1;   // chỗ khác vẫn gọi DEFAULT, sau này đổi thành chọn theo số tầng}

    // Compact constructor: chạy trước khi gán trường, dùng để kiểm tra đầu vào.
    // TODO: thêm trường tỉ lệ đuốc sáng (0.75) vào record. Đổi chữ ký record cần duyệt trước vì mọi nơi gọi new DungeonParams(...) phải sửa.
    public DungeonParams {
        if (gridW < 3 || gridD < 3) throw new IllegalArgumentException("lưới tối thiểu 3x3");
        if (minRooms < 2 || maxRooms < minRooms) throw new IllegalArgumentException("minRooms/maxRooms sai");
        // Cần 1 phòng vào + 4 phòng cụt đặc biệt + (6 - 4) phòng thường để thay bằng ? / elite.
        int deadSpecials = 1 + deadEndRoles.length;                          // boss + các phòng cụt đặc biệt
        if (minRooms < 1 + deadSpecials + maxMystery + maxElite)             // 1 = phòng vào
            throw new IllegalArgumentException("minRooms quá nhỏ so với số phòng đặc biệt");
        if (minDeadEnds < deadSpecials) throw new IllegalArgumentException("minDeadEnds phải >= " + deadSpecials);
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
}
