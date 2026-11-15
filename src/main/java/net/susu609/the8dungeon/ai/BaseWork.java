package net.susu609.the8dungeon.ai;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.susu609.the8dungeon.ai.Blueprint.Entry;
import net.susu609.the8dungeon.ai.Blueprint.Mat;

/** Tính toán xây base: ô nào còn thiếu, cần vật liệu gì, đặt khối đúng loại và hướng. */
final class BaseWork {

    private BaseWork() {}

    /** Kết quả kiểm tra base cho một bot. */
    static final class Status {
        Entry next;                       // việc bot này làm được ngay (đào dọn hoặc đặt), hoặc null
        boolean nextIsMine;               // true: phải phá khối đang chiếm chỗ; false: đặt khối
        final Map<Mat, Integer> missing = new EnumMap<>(Mat.class); // ô bắt buộc còn thiếu theo loại
        boolean structureDone;            // tường, mái, cửa đã đủ (có thể trú đêm)
        boolean tierDone;                 // mọi ô bắt buộc đã xong
    }

    // ===== Khối hiện tại có khớp bản vẽ không =====

    static boolean satisfied(ServerLevel level, BlockPos p, Mat m) {
        BlockState s = level.getBlockState(p);
        return switch (m) {
            case AIR -> s.canBeReplaced() && s.getFluidState().isEmpty();
            case SOLID -> s.isFaceSturdy(level, p, Direction.UP);
            case PLANKS -> s.is(BlockTags.PLANKS);
            case LOG -> s.is(BlockTags.LOGS);
            case COBBLE -> s.is(Blocks.COBBLESTONE);
            case DOOR -> s.is(BlockTags.WOODEN_DOORS);
            case BED -> s.is(BlockTags.BEDS);
            case TABLE -> s.is(Blocks.CRAFTING_TABLE);
            case FURNACE -> s.is(Blocks.FURNACE);
            case CHEST -> s.is(Blocks.CHEST);
            case TORCH -> s.is(Blocks.WALL_TORCH) || s.is(Blocks.TORCH);
            case LADDER -> s.is(Blocks.LADDER);
            case FENCE -> s.is(BlockTags.WOODEN_FENCES);
        };
    }

    /** Món trong túi dùng để đặt loại ô này. */
    static Predicate<ItemStack> itemFor(Mat m) {
        return switch (m) {
            case SOLID -> st -> st.is(Items.DIRT) || st.is(Items.COBBLESTONE);
            case PLANKS -> st -> st.is(ItemTags.PLANKS);
            case LOG -> st -> st.is(ItemTags.LOGS);
            case COBBLE -> st -> st.is(Items.COBBLESTONE);
            case DOOR -> st -> st.is(ItemTags.WOODEN_DOORS);
            case BED -> st -> st.is(ItemTags.BEDS);
            case TABLE -> st -> st.is(Items.CRAFTING_TABLE);
            case FURNACE -> st -> st.is(Items.FURNACE);
            case CHEST -> st -> st.is(Items.CHEST);
            case TORCH -> st -> st.is(Items.TORCH);
            case LADDER -> st -> st.is(Items.LADDER);
            case FENCE -> st -> st.is(ItemTags.WOODEN_FENCES);
            default -> st -> false;
        };
    }

    // ===== Kiểm tra base =====

    static Status status(ServerLevel level, BaseStore.Base base, BotPlayer bot, Predicate<BlockPos> skip) {
        Status st = new Status();
        Inventory inv = bot.getInventory();
        List<Entry> plan = Blueprint.tier(base.tier);
        boolean structure = true, all = true;
        double bestD = Double.MAX_VALUE;
        // Thứ tự ưu tiên: dọn chỗ -> nền -> tường/cột -> mái/cửa -> nội thất
        for (int pass = 0; pass < 4; pass++) {
            for (Entry e : plan) {
                if (pass != phase(e.mat())) continue;
                BlockPos p = base.origin.offset(e.dx(), e.dy(), e.dz());
                if (!level.isLoaded(p) || satisfied(level, p, e.mat())) continue;
                boolean skipped = skip.test(p);
                BlockState s = level.getBlockState(p);
                boolean blocked = !s.canBeReplaced() || !s.getFluidState().isEmpty();
                if (e.mat() == Mat.FENCE && blocked && !s.is(BlockTags.LEAVES)) continue; // rào không đào đất
                if (!e.optional()) {
                    st.missing.merge(e.mat(), 1, Integer::sum);
                    all = false;
                    if (e.mat() != Mat.BED && e.mat() != Mat.TABLE && e.mat() != Mat.FURNACE
                            && e.mat() != Mat.CHEST && e.mat() != Mat.LADDER) structure = false;
                }
                // việc làm được ngay: phá khối chiếm chỗ (luôn làm được), hoặc đặt nếu có món
                boolean mine = blocked && e.mat() != Mat.SOLID;
                boolean can = mine || (e.mat() != Mat.AIR && BotItems.count(inv, itemFor(e.mat())) > 0);
                if (!can || skipped) continue;
                double d = p.distSqr(bot.blockPosition()) + pass * 400.0D; // ưu tiên pha trước, rồi gần
                if (d < bestD) {
                    bestD = d;
                    st.next = e;
                    st.nextIsMine = mine;
                }
            }
        }
        st.structureDone = structure;
        st.tierDone = all;
        return st;
    }

    private static int phase(Mat m) {
        return switch (m) {
            case AIR -> 0;
            case SOLID -> 1;
            case LOG, PLANKS, COBBLE -> 2;
            default -> 3;
        };
    }

    // ===== Đặt khối =====

    /** Đặt món cho ô e tại p (trừ món trong túi). Trả false nếu không đặt được. */
    static boolean place(ServerLevel level, BotPlayer bot, Entry e, BlockPos p) {
        if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(p)).isEmpty()) return false;
        Inventory inv = bot.getInventory();
        ItemStack stack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && itemFor(e.mat()).test(s)) {
                stack = s;
                break;
            }
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return false;
        Block block = bi.getBlock();
        Direction f = e.facing();
        BlockState state;
        switch (e.mat()) {
            case LOG -> state = block.defaultBlockState().hasProperty(RotatedPillarBlock.AXIS)
                    ? block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : block.defaultBlockState();
            case DOOR -> {
                BlockPos up = p.above();
                if (!level.getBlockState(up).canBeReplaced()) return false;
                state = block.defaultBlockState().setValue(DoorBlock.FACING, f).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                level.setBlock(up, state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
            }
            case BED -> {
                BlockPos head = p.relative(f);
                if (!level.getBlockState(head).canBeReplaced()) return false;
                state = block.defaultBlockState().setValue(BedBlock.FACING, f).setValue(BedBlock.PART, BedPart.FOOT);
                level.setBlock(head, state.setValue(BedBlock.PART, BedPart.HEAD), 3);
            }
            case TORCH -> state = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, f);
            case FURNACE, CHEST, LADDER -> state = block.defaultBlockState().hasProperty(HorizontalDirectionalBlock.FACING)
                    ? block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, f) : block.defaultBlockState();
            default -> state = block.defaultBlockState();
        }
        if (e.mat() == Mat.FENCE) state = Block.updateFromNeighbourShapes(state, level, p); // nối rào với nhau
        level.setBlock(p, state, 3);
        level.playSound(null, p, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
        stack.shrink(1);
        return true;
    }

    // ===== Vị trí và bảo vệ base =====

    /** Khối này thuộc vùng một base (bot không tự ý phá, trừ khi bản vẽ yêu cầu). */
    static boolean protects(BaseStore store, ServerLevel level, BlockPos p) {
        String dim = level.dimension().location().toString();
        for (BaseStore.Base b : store.all()) {
            if (!b.dim.equals(dim)) continue;
            int x = p.getX() - b.origin.getX(), y = p.getY() - b.origin.getY(), z = p.getZ() - b.origin.getZ();
            if (x >= -2 && x <= 12 && z >= -2 && z <= 8 && y >= -1 && y <= 4) return true;
        }
        return false;
    }

    /** Tìm chỗ tương đối phẳng (chênh tối đa 2 khối, không nước) cho khung 7x7 quanh center. */
    static BlockPos findSite(ServerLevel level, BlockPos center) {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int dx = -16; dx <= 16; dx += 2) {
            for (int dz = -16; dz <= 16; dz += 2) {
                int ox = center.getX() + dx, oz = center.getZ() + dz;
                int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, sum = 0;
                boolean ok = true;
                for (int x = 0; x <= 6 && ok; x++) {
                    for (int z = 0; z <= 6 && ok; z++) {
                        if (!level.hasChunk((ox + x) >> 4, (oz + z) >> 4)) {
                            ok = false;
                            break;
                        }
                        int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox + x, oz + z);
                        if (!level.getFluidState(new BlockPos(ox + x, h - 1, oz + z)).isEmpty()) ok = false;
                        min = Math.min(min, h);
                        max = Math.max(max, h);
                        sum += h;
                    }
                }
                if (!ok || max - min > 2) continue;
                int score = (max - min) * 100 + dx * dx + dz * dz;
                if (score < bestScore) {
                    bestScore = score;
                    best = new BlockPos(ox, Math.round(sum / 49.0F), oz);
                }
            }
        }
        return best;
    }

    // ===== Vật liệu còn thiếu (để đi kiếm và chế) =====

    /** Số món còn thiếu so với những gì bot đang có, theo từng loại ô (chỉ tính ô bắt buộc). */
    static Map<Mat, Integer> deficit(Status st, Inventory inv) {
        Map<Mat, Integer> d = new EnumMap<>(Mat.class);
        st.missing.forEach((m, n) -> {
            if (m == Mat.AIR || m == Mat.SOLID) return;
            int have = BotItems.count(inv, itemFor(m));
            if (n > have) d.put(m, n - have);
        });
        return d;
    }

    /** Giường cùng màu với len: white_wool -> white_bed. */
    static Item bedForWool(Item wool) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(wool);
        Item bed = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("_wool", "_bed")));
        return bed == Items.AIR ? Items.WHITE_BED : bed;
    }
}
