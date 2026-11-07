package net.susu609.the8dungeons.dungeon;

import net.susu609.the8dungeons.world.WorldRandom;

import java.util.*;

/**
 * Sơ đồ MỘT lớp dungeon: phòng nào ở ô lưới nào, cửa nối phòng nào với phòng
 * nào. Lớp này chỉ là dữ liệu thuần (không import gì của Minecraft) nên test
 * được bằng main().
 * <p>
 * Quy trình (cùng seed thì cùng kết quả): Pha 1: mọc cây phòng từ phòng vào,
 * cho đến khi đủ số phòng. Pha 2: ensureDeadEnds, thêm phòng cụt (có thể kèm
 * phòng cầu nối) cho đủ số tối thiểu. Pha 3: gán vai trò (phòng vào, phòng
 * boss). Nếu một bước không làm được thì bỏ cả lần thử và sinh lại với
 * "attempt" kế tiếp.
 */
public final class DungeonLayout {

    private static final int MAX_ATTEMPTS = 200;
    private final DungeonParams params;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Door> doors = new ArrayList<>();
    private final int[][] grid;// grid[z][x] = id phòng chiếm ô đó, -1 nếu trống
    private final Set<Integer> tagged = new HashSet<>();
    // phòng đã nhận nhánh cụt hoặc là cầu nối
    private int bossId = -1;
    private DungeonLayout(DungeonParams params) {
        this.params = params;
        this.grid = new int[params.gridD()][params.gridW()];
        for (int[] row : grid)
            Arrays.fill(row, -1);
    }
    public static DungeonLayout generate(WorldRandom world,
            DungeonParams params)
    {
        WorldRandom base = world.fork("dungeon-layout");
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            DungeonLayout layout = attemptGenerate(base.at(attempt, 0), params);
            if (layout != null)
                return layout;
        }
        throw new IllegalStateException(
                "Không sinh được dungeon sau " + MAX_ATTEMPTS +
                        " lần thử, thử tăng kích thước lưới hoặc giảm " +
                        "minRooms/minDeadEnds.");
    }
    private static DungeonLayout attemptGenerate(WorldRandom r, DungeonParams p)
    {
        DungeonLayout l = new DungeonLayout(p);
        l.addRoom(
                RoomPreset.R16X16, false, p.gridW() / 2,
                p.gridD() / 2);   // phòng vào ở giữa
        // Pha 1: số phòng đích nằm trong [minRooms, maxRooms]. Chọn một
        // phòng bất kỳ, một hướng bất kỳ,
        // thử gắn phòng mới; lượt nào không vừa thì bỏ và thử lượt khác.
        int target = r.nextRange(p.minRooms(), p.maxRooms());
        for (int guard = 0; l.rooms.size() < target && guard < 3000; guard++) {
            Room parent = l.rooms.get(r.nextInt(l.rooms.size()));
            Side side = Side.values()[r.nextInt(4)];
            RoomPreset preset = l.pickPreset(r);
            boolean rotated = preset.canRotate() && r.chance(0.5);
            l.tryAttach(r, parent, side, preset, rotated);
        }
        if (l.rooms.size() < p.minRooms())
            return null;   // không đủ phòng tối thiểu thì bỏ lần thử này
        if (!l.ensureDeadEnds(r))
            return null;
        l.assignRoles();
        return l;
    }
    // Chọn ngẫu nhiên một ô trong đoạn giao nhau của [aStart, aStart+aLen)
    // và [bStart, bStart+bLen).
    private static int overlapPick(WorldRandom r, int aStart, int aLen,
            int bStart, int bLen)
    {
        int from = Math.max(aStart, bStart);
        int to = Math.min(aStart + aLen, bStart + bLen);   // loại trừ
        return from + r.nextInt(to - from);
    }
    // Trộn ngẫu nhiên theo WorldRandom (Fisher-Yates), để kết quả vẫn quyết
    // định theo seed.
    private static <T> void shuffle(WorldRandom r, List<T> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
    }
    // ---------- API công khai ----------
    // Phòng cụt = phòng chỉ có 1 cửa, không tính phòng vào.
    public int deadEndCount() {
        int n = 0;
        for (Room r : rooms)
            if (r.id() != 0 && degree(r.id()) == 1)
                n++;
        return n;
    }
    // Số cửa của phòng.
    public int degree(int roomId) {
        int n = 0;
        for (Door d : doors)
            if (d.a() == roomId || d.b() == roomId)
                n++;
        return n;
    }
    public List<Door> doors() {
        return Collections.unmodifiableList(doors);
    }
    public DungeonParams params() {
        return params;
    }
    public Role roleOf(Room room) {
        if (room.id() == 0)
            return Role.START;
        if (room.id() == bossId)
            return Role.BOSS;
        return Role.NORMAL;
    }
    public List<Room> rooms() {
        return Collections.unmodifiableList(rooms);
    }
    public Room start() {
        return rooms.get(0);
    }
    // Pha 2: bù phòng cụt cho đủ minDeadEnds.
    private boolean ensureDeadEnds(WorldRandom r) {
        while (deadEndCount() < params.minDeadEnds()) {
            // Cha hợp lệ: có từ 2 cửa (gắn vào phòng cụt thì số cụt không
            // tăng), chưa có nhãn, không phải phòng vào.
            List<Room> candidates = new ArrayList<>();
            for (Room room : rooms) {
                if (room.id() != 0 && degree(room.id()) >= 2 &&
                        !tagged.contains(room.id()))
                {
                    candidates.add(room);
                }
            }
            shuffle(r, candidates);
            boolean ok = false;
            for (Room c : candidates) {
                if (tryBranch(r, c)) {
                    ok = true;
                    break;
                }
            }
            if (!ok)
                return false;   // hết chỗ, sinh lại
        }
        return true;
    }
    // ---------- Sinh ----------
    // Thử gắn từ phòng cha một nhánh: [cầu nối 16x16] + phòng cụt. Thất bại
    // thì hoàn tác sạch.
    private boolean tryBranch(WorldRandom r, Room parent) {
        List<Side> sides = new ArrayList<>(List.of(Side.values()));
        shuffle(r, sides);
        for (Side side : sides) {
            int len = 1 + r.nextInt(
                    2);          // 1 = phòng cụt luôn, 2 = có một cầu nối
            List<Room> chain = new ArrayList<>();
            Room cur = parent;
            for (int i = 0; i < len; i++) {
                boolean last = (i == len - 1);
                RoomPreset preset = last ? pickPreset(r) :
                        RoomPreset.R16X16;   // cầu nối luôn 16x16
                boolean rotated = preset.canRotate() && r.chance(0.5);
                Room child = tryAttach(r, cur, side, preset, rotated);
                if (child == null)
                    break;
                chain.add(child);
                cur = child;
            }
            if (chain.size() == len) {
                tagged.add(parent.id());
                for (int i = 0; i < len - 1; i++)
                    tagged.add(chain.get(i)
                            .id());   // cầu nối không mọc thêm nhánh
                return true;
            }
            for (int i = 0; i < chain.size(); i++)
                undoLast();   // hoàn tác phần đã gắn
        }
        return false;
    }
    /**
     * Thử gắn một phòng mới vào cạnh `side` của phòng cha. Điều kiện: nằm trong
     * lưới, các ô còn trống, và phòng mới CHỈ chạm đúng phòng cha (không tạo
     * vòng). Thử lần lượt mọi vị trí lệch dọc theo cạnh (bắt đầu từ một vị trí
     * ngẫu nhiên). Thành công thì trả về phòng mới (đã thêm phòng + cửa), thất
     * bại trả null và không đổi gì.
     */
    private Room tryAttach(WorldRandom r, Room parent, Side side,
            RoomPreset preset, boolean rotated)
    {
        int w = preset.cellsX(rotated);
        int d = preset.cellsZ(rotated);
        boolean horizontal = (side == Side.EAST || side == Side.WEST);
        // Với EAST/WEST, cx cố định còn cz chạy trong [lo, hi]; SOUTH/NORTH
        // thì ngược lại.
        int fixed;
        int lo;
        int hi;
        switch (side) {
            case EAST -> fixed = parent.cx() + parent.cellsX();
            case WEST -> fixed = parent.cx() - w;
            case SOUTH -> fixed = parent.cz() + parent.cellsZ();
            default -> fixed = parent.cz() - d;
        }
        if (horizontal) {
            lo = parent.cz() - (d - 1);
            hi = parent.cz() + parent.cellsZ() - 1;
        } else {
            lo = parent.cx() - (w - 1);
            hi = parent.cx() + parent.cellsX() - 1;
        }
        int count = hi - lo + 1;
        int startAt = r.nextInt(count);
        for (int k = 0; k < count; k++) {
            int offset = lo + (startAt + k) % count;
            int cx = horizontal ? fixed : offset;
            int cz = horizontal ? offset : fixed;
            if (!fits(parent.id(), cx, cz, w, d))
                continue;
            Room child = addRoom(preset, rotated, cx, cz);
            // Cửa đặt ở một ô ngẫu nhiên trên đoạn ranh giới chung giữa hai
            // phòng.
            switch (side) {
                case EAST -> doors.add(new Door(
                        parent.id(), child.id(),
                        parent.cx() + parent.cellsX() - 1,
                        overlapPick(r, parent.cz(), parent.cellsZ(), cz, d),
                        true));
                case WEST -> doors.add(new Door(
                        parent.id(), child.id(), cx + w - 1,
                        overlapPick(r, parent.cz(), parent.cellsZ(), cz, d),
                        true));
                case SOUTH -> doors.add(new Door(
                        parent.id(), child.id(),
                        overlapPick(r, parent.cx(), parent.cellsX(), cx, w),
                        parent.cz() + parent.cellsZ() - 1, false));
                default -> doors.add(new Door(
                        parent.id(), child.id(),
                        overlapPick(r, parent.cx(), parent.cellsX(), cx, w),
                        cz + d - 1, false));
            }
            return child;
        }
        return null;
    }
    // Hình chữ nhật (cx, cz, w, d) có đặt được không?
    private boolean fits(int parentId, int cx, int cz, int w, int d) {
        if (cx < 0 || cz < 0 || cx + w > params.gridW() ||
                cz + d > params.gridD())
            return false;
        for (int z = cz; z < cz + d; z++) {
            for (int x = cx; x < cx + w; x++) {
                if (grid[z][x] != -1)
                    return false;
            }
        }
        // Chỉ được chạm đúng một phòng khác, và đó phải là phòng cha.
        Set<Integer> touching = new HashSet<>();
        for (int z = cz; z < cz + d; z++) {
            for (int x = cx; x < cx + w; x++) {
                touch(touching, x + 1, z);
                touch(touching, x - 1, z);
                touch(touching, x, z + 1);
                touch(touching, x, z - 1);
            }
        }
        return touching.size() == 1 && touching.contains(parentId);
    }
    private void touch(Set<Integer> out, int x, int z) {
        if (x < 0 || z < 0 || x >= params.gridW() || z >= params.gridD())
            return;
        if (grid[z][x] != -1)
            out.add(grid[z][x]);
    }
    private Room addRoom(RoomPreset preset, boolean rotated, int cx, int cz) {
        Room room = new Room(rooms.size(), preset, rotated, cx, cz);
        rooms.add(room);
        for (int z = cz; z < cz + room.cellsZ(); z++) {
            for (int x = cx; x < cx + room.cellsX(); x++)
                grid[z][x] = room.id();
        }
        return room;
    }
    // Bỏ phòng cuối cùng và cửa cuối cùng (mỗi lần gắn thêm đúng 1 phòng + 1
    // cửa).
    private void undoLast() {
        Room room = rooms.remove(rooms.size() - 1);
        doors.remove(doors.size() - 1);
        for (int z = room.cz(); z < room.cz() + room.cellsZ(); z++) {
            for (int x = room.cx(); x < room.cx() + room.cellsX(); x++)
                grid[z][x] = -1;
        }
    }
    // Pha 3: boss ở phòng cụt xa phòng vào nhất (đếm số cửa phải đi qua).
    private void assignRoles() {
        int[] dist = new int[rooms.size()];
        Arrays.fill(dist, -1);
        dist[0] = 0;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            for (Door d : doors) {
                int other =
                        (d.a() == cur) ? d.b() : (d.b() == cur) ? d.a() : -1;
                if (other != -1 && dist[other] == -1) {
                    dist[other] = dist[cur] + 1;
                    queue.add(other);
                }
            }
        }
        bossId = -1;
        for (Room room : rooms) {
            if (room.id() != 0 && degree(room.id()) == 1 &&
                    (bossId == -1 || dist[room.id()] > dist[bossId]))
            {
                bossId = room.id();
            }
        }
    }
    private RoomPreset pickPreset(WorldRandom r) {
        int[] w = params.presetWeights();
        int total = 0;
        for (int x : w)
            total += x;
        int roll = r.nextInt(total);
        for (int i = 0; i < w.length; i++) {
            roll -= w[i];
            if (roll < 0)
                return RoomPreset.values()[i];
        }
        return RoomPreset.R16X16;   // không bao giờ tới đây, chỉ để compiler
        // yên tâm
    }

    public enum Role {START, NORMAL, BOSS}

    private enum Side {EAST, WEST, SOUTH, NORTH}
    // id trùng với vị trí trong danh sách rooms (chỉ xóa ở cuối nên không bị
    // lệch).
    public record Room(int id, RoomPreset preset, boolean rotated, int cx,
                       int cz)
    {
        public int cellsX() {
            return preset.cellsX(rotated);
        }
        public int cellsZ() {
            return preset.cellsZ(rotated);
        }
    }
    /**
     * Cửa nằm trên ranh giới giữa hai ô lưới. east = true : giữa ô (cx, cz) và
     * ô (cx + 1, cz) east = false: giữa ô (cx, cz) và ô (cx, cz + 1)
     */
    public record Door(int a, int b, int cx, int cz, boolean east) {
    }
}
