package net.susu609.the8dungeon.client;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Tập các khối đang bị "cắt" khỏi hình chunk (mixin RenderChunkRegionMixin trả về không khí cho chúng
 * khi dựng mesh), rồi được vẽ lại mờ bởi SpectatorCam. Chỉ ảnh hưởng hình ảnh, không đụng tới thế giới.
 * Đọc từ luồng dựng chunk nên dùng tham chiếu volatile tới tập không bao giờ bị sửa sau khi công bố.
 */
public final class CutawayState {

    private static final long[] NONE = new long[0];
    private static volatile LongOpenHashSet hidden = new LongOpenHashSet();
    private static volatile long[] positions = NONE;

    private CutawayState() {}

    /** Gọi từ luồng dựng chunk: rất rẻ khi không có gì bị ẩn. */
    public static boolean isHidden(BlockPos pos) {
        return positions.length != 0 && hidden.contains(pos.asLong());
    }

    public static long[] positions() {
        return positions;
    }

    public static void update(LongOpenHashSet next) {
        LongOpenHashSet old = hidden;
        if (old.equals(next)) return;
        hidden = next;
        positions = next.isEmpty() ? NONE : next.toLongArray();
        markDirty(old, next);
    }

    public static void clear() {
        if (positions.length != 0) update(new LongOpenHashSet());
    }

    /** Dựng lại các section chứa khối thay đổi (cộng 1 khối viền để mặt của khối bên cạnh hiện ra). */
    private static void markDirty(LongOpenHashSet a, LongOpenHashSet b) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (LongOpenHashSet set : new LongOpenHashSet[] {a, b}) {
            for (long l : set) {
                int x = BlockPos.getX(l), y = BlockPos.getY(l), z = BlockPos.getZ(l);
                minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
            }
        }
        if (minX == Integer.MAX_VALUE) return;
        Minecraft.getInstance().levelRenderer.setBlocksDirty(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
    }
}
