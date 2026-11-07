package net.susu609.the8dungeons;

import net.susu609.the8dungeons.world.WorldRandom;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        WorldRandom a = new WorldRandom(2026L);
        WorldRandom b = new WorldRandom(2026L);
        System.out.println(a.nextLong() + "  " + b.nextLong());   // hai số phải giống nhau

        WorldRandom r = new WorldRandom(1L);
        int[] count = new int[6];
        for (int i = 0; i < 600_000; i++) count[r.nextInt(6)]++;
        System.out.println(Arrays.toString(count));   // 6 số quanh 100.000 (lệch vài trăm là bình thường)

        WorldRandom world = new WorldRandom(2026L);

// 1) Theo tên việc
        int roomWidth = world.fork("layout").nextRange(4, 10);

// 2) Theo vị trí, kết hợp với tên việc
        boolean hasLoot = world.fork("loot").at(3, 7).chance(0.5);

// 3) Gọi lại lần nữa phải cho cùng kết quả (kể cả khi gọi theo thứ tự khác)
        System.out.println(world.fork("loot").at(3, 7).chance(0.5) == hasLoot);   // true
    }
}
