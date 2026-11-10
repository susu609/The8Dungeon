package net.susu609.the8dungeon.dungeon;

/**
 * Các kích thước phòng có sẵn. Kích thước tính theo block, GỒM cả tường. Mỗi ô
 * lưới (cell) rộng đúng 16 block = 1 chunk, nên phòng luôn là bội của 16.
 * <p>
 * Phòng 16x16: nội thất 14x14. Phòng 16x32: nội thất 14x30 (chiếm 2 ô lưới).
 * Chiều cao nội thất luôn là 16 block (xem INTERIOR_HEIGHT).
 */
public enum RoomPreset {
    // THỨ TỰ enum này khớp với mảng trọng số presetWeights trong DungeonParams.
    R16X16(16, 16), R16X32(16, 32), R32X32(32, 32);

    public static final int CELL = 16;              // cạnh một ô lưới, block
    public static final int INTERIOR_HEIGHT = 16;
    // chiều cao rỗng bên trong, block

    private final int sizeX;
    private final int sizeZ;

    RoomPreset(int sizeX, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    // Phòng không vuông thì có thể xoay 90 độ (16x32 thành 32x16).
    public boolean canRotate() {
        return sizeX != sizeZ;
    }

    // Số ô lưới theo trục X; rotated = true thì đổi chỗ X và Z.
    public int cellsX(boolean rotated) {
        return (rotated ? sizeZ : sizeX) / CELL;
    }

    public int cellsZ(boolean rotated) {
        return (rotated ? sizeX : sizeZ) / CELL;
    }
}
