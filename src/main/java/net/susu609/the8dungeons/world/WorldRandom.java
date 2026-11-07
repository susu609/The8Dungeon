package net.susu609.the8dungeons.world;

public class WorldRandom {

    private final long seed;  // seed duy nhất của cả thế giới, không bao giờ đổi
    private long state;       // trạng thái chạy, đổi sau mỗi lần sinh số

    public WorldRandom(long seed) {
        this.seed = seed;
        this.state = seed;
    }

    // Biến một số long thành số long khác nhìn như ngẫu nhiên.
    // Hai đầu vào gần nhau (1 và 2) cho đầu ra khác hẳn nhau.
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    // Số nguyên 0..bound-1
    public int nextInt(int bound) {
        long r = nextLong() >>> 33;           // lấy 31 bit (0 .. 2^31-1)
        return (int) ((r * bound) >>> 31);    // co vào [0, bound)
    }

    // Số nguyên min..max, GỒM CẢ max
    public int nextRange(int min, int max) {
        return min + nextInt(max - min + 1);
    }

    // Số thực 0.0 đến dưới 1.0
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    // true với xác suất p. Ví dụ chance(0.02) = 2%
    public boolean chance(double p) {
        return nextDouble() < p;
    }

    // Mọi hàm bên dưới đều gọi hàm này.
    public long nextLong() {
        state += 0x9E3779B97F4A7C15L;  // mỗi lần tiến lên một bước cố định
        return mix(state);             // rồi trộn ra kết quả
    }

    // Tạo bộ sinh con từ seed gốc + một số định danh.
    // Cùng seed gốc + cùng id => luôn cùng dãy, bất kể bạn đã gọi bao nhiêu số ở bộ cha.
    private WorldRandom derive(long id) {
        return new WorldRandom(mix(seed ^ mix(id)));
    }

    // Theo TÊN việc: world.fork("loot"), world.fork("layout")...
    public WorldRandom fork(String name) {
        long h = 0xCBF29CE484222325L;                 // băm chuỗi thành long
        for (int i = 0; i < name.length(); i++) {
            h = (h ^ name.charAt(i)) * 0x100000001B3L;
        }
        return derive(h);
    }

    // Theo VỊ TRÍ: world.at(chunkX, chunkZ)
    public WorldRandom at(int x, int z) {
        return derive(((long) x << 32) ^ (z & 0xFFFFFFFFL));  // ghép x và z vào một long
    }
}