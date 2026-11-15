package net.susu609.the8dungeon.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;

/**
 * Bản vẽ nhà làng plains 3 cấp (1: chòi gỗ 5x5; 2: nhà đá 7x7; 3: thêm phòng phụ phía đông và rào sân), tọa độ tương đối với góc tây-bắc (min x, min z) của khung 7x7, y=0 là tầng nền.
 * Mỗi cấp là một bản vẽ đầy đủ; nâng cấp thì khối của cấp cũ không còn trong bản vẽ mới sẽ bị dọn (ô AIR).
 * Cửa luôn ở tường bắc.
 */
final class Blueprint {

    /** Loại vật liệu của một ô. Khối thật lấy từ món bot có (gỗ sồi hay vân sam đều được). */
    enum Mat { AIR, SOLID, PLANKS, LOG, COBBLE, DOOR, BED, TABLE, FURNACE, CHEST, TORCH, LADDER, FENCE }

    /** facing: hướng của cửa/giường (chân -> đầu)/lò/rương/đuốc/thang; optional: không bắt buộc để hoàn thành cấp. */
    record Entry(int dx, int dy, int dz, Mat mat, Direction facing, boolean optional) {}

    private static final List<List<Entry>> TIERS = List.of(tier1(), tier2(), tier3());

    static final int MAX_TIER = 3;

    /** Bản vẽ của cấp t (1..3). */
    static List<Entry> tier(int t) {
        return TIERS.get(Math.max(1, Math.min(MAX_TIER, t)) - 1);
    }

    /** Ô đứng bên trong nhà (ban đêm vào đứng đây). */
    static int[] insideCell(int t) {
        return new int[] {3, 0, 3};
    }

    private static void add(List<Entry> l, int x, int y, int z, Mat m) {
        l.add(new Entry(x, y, z, m, Direction.NORTH, false));
    }

    private static void add(List<Entry> l, int x, int y, int z, Mat m, Direction f, boolean optional) {
        l.add(new Entry(x, y, z, m, f, optional));
    }

    /** Hộp nhà: nền, tường (góc là cột gỗ), mái; bên trong để trống. */
    private static void box(List<Entry> l, int x0, int z0, int x1, int z1, int y0, int y1, Mat wall,
                            boolean floor, Mat roof, int doorX) {
        if (floor) {
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) add(l, x, y0 - 1, z, Mat.SOLID);
        }
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edgeX = x == x0 || x == x1, edgeZ = z == z0 || z == z1;
                    if (edgeX && edgeZ) add(l, x, y, z, Mat.LOG);
                    else if (edgeX || edgeZ) {
                        if (z == z0 && x == doorX && y <= y0 + 1) {
                            if (y == y0) add(l, x, y, z, Mat.DOOR, Direction.NORTH, false); // cửa 2 khối, ghi ở nửa dưới
                        } else {
                            add(l, x, y, z, wall);
                        }
                    } else {
                        add(l, x, y, z, Mat.AIR);
                    }
                }
            }
        }
        if (roof != null) {
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) add(l, x, y1 + 1, z, roof);
        }
    }

    /** Cấp 1: chòi gỗ 5x5, cửa, 1 giường, bàn chế tạo, đuốc. */
    private static List<Entry> tier1() {
        List<Entry> l = new ArrayList<>();
        box(l, 1, 1, 5, 5, 0, 2, Mat.PLANKS, true, Mat.PLANKS, 3);
        furnish(l, Mat.BED, 2, 0, 3, Direction.SOUTH, false); // chân (2,0,3) -> đầu (2,0,4)
        furnish(l, Mat.TABLE, 4, 0, 4, Direction.NORTH, false);
        furnish(l, Mat.TORCH, 4, 1, 2, Direction.SOUTH, true);
        return l;
    }

    /** Cấp 2: nhà 7x7 tường đá cuội, 2 giường, bàn, lò, rương, 2 đuốc. */
    private static List<Entry> tier2() {
        List<Entry> l = new ArrayList<>();
        box(l, 0, 0, 6, 6, 0, 2, Mat.COBBLE, true, Mat.PLANKS, 3);
        furnishTier2(l);
        return l;
    }

    private static void furnishTier2(List<Entry> l) {
        furnish(l, Mat.BED, 1, 0, 4, Direction.SOUTH, false);
        furnish(l, Mat.BED, 2, 0, 4, Direction.SOUTH, false);
        furnish(l, Mat.TABLE, 5, 0, 5, Direction.NORTH, false);
        furnish(l, Mat.FURNACE, 5, 0, 4, Direction.WEST, false);
        furnish(l, Mat.CHEST, 5, 0, 3, Direction.WEST, false);
        furnish(l, Mat.TORCH, 1, 1, 1, Direction.SOUTH, true);
        furnish(l, Mat.TORCH, 4, 1, 1, Direction.SOUTH, true);
    }

    /** Cấp 3: cấp 2 + phòng phụ 5x5 phía đông (thông cửa trong), thêm 2 giường, hàng rào sân bao quanh. */
    private static List<Entry> tier3() {
        List<Entry> l = new ArrayList<>();
        box(l, 0, 0, 6, 6, 0, 2, Mat.COBBLE, true, Mat.PLANKS, 3);
        // phòng phụ x 6..10, z 1..5 dùng chung tường x=6 với nhà chính
        List<Entry> annex = new ArrayList<>();
        box(annex, 6, 1, 10, 5, 0, 2, Mat.COBBLE, true, Mat.PLANKS, -1);
        for (Entry e : annex) {
            boolean shared = e.dx() == 6 && e.dy() >= -1; // tường chung đã có trong nhà chính
            if (!shared) l.add(e);
        }
        add(l, 6, 0, 3, Mat.AIR); // lối thông giữa hai phòng
        add(l, 6, 1, 3, Mat.AIR);
        furnishTier2(l);
        furnish(l, Mat.BED, 8, 0, 3, Direction.SOUTH, false);
        furnish(l, Mat.BED, 9, 0, 3, Direction.SOUTH, false);
        furnish(l, Mat.TORCH, 8, 1, 2, Direction.SOUTH, true);
        // hàng rào sân, chừa lối vào trước cửa
        for (int x = -2; x <= 12; x++) {
            for (int z = -2; z <= 8; z++) {
                boolean ring = x == -2 || x == 12 || z == -2 || z == 8;
                if (ring && !(z == -2 && x == 3)) add(l, x, 0, z, Mat.FENCE, Direction.NORTH, true);
            }
        }
        return l;
    }

    /** Đặt đồ nội thất: thay ô AIR đã có ở vị trí đó (và ô đầu giường) bằng món đồ. */
    private static void furnish(List<Entry> l, Mat m, int x, int y, int z, Direction f, boolean optional) {
        l.removeIf(e -> e.dx() == x && e.dy() == y && e.dz() == z);
        if (m == Mat.BED) {
            int hx = x + f.getStepX(), hz = z + f.getStepZ();
            l.removeIf(e -> e.dx() == hx && e.dy() == y && e.dz() == hz); // ô đầu giường do giường tự đặt
        }
        l.add(new Entry(x, y, z, m, f, optional));
    }
}
