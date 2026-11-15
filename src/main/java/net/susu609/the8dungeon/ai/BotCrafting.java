package net.susu609.the8dungeon.ai;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.susu609.the8dungeon.ai.Blueprint.Mat;

/**
 * Chế đồ như người chơi:
 * - Lưới 2x2 (trong túi): ván, gậy, bàn chế tạo, đuốc.
 * - Lưới 3x3 (chỉ khi đứng nhìn vào bàn chế tạo): công cụ, lò, và đồ cho base (cửa, rương, giường, thang, rào).
 * need = số món base còn thiếu theo loại ô (có thể rỗng).
 */
final class BotCrafting {

    private static final Predicate<ItemStack> LOG = s -> s.is(ItemTags.LOGS);
    private static final Predicate<ItemStack> PLANK = s -> s.is(ItemTags.PLANKS);
    private static final Predicate<ItemStack> STICK = s -> s.is(Items.STICK);
    private static final Predicate<ItemStack> COBBLE = s -> s.is(Items.COBBLESTONE);
    private static final Predicate<ItemStack> TABLE = s -> s.is(Items.CRAFTING_TABLE);
    private static final Predicate<ItemStack> COAL = s -> s.is(Items.COAL) || s.is(Items.CHARCOAL);
    private static final Predicate<ItemStack> WOOL = s -> s.is(ItemTags.WOOL);

    private BotCrafting() {}

    /** Mong muốn thêm ngoài công cụ/base: cuốc xới (trồng trọt), bánh mì (từ lúa mì). */
    record Wants(boolean hoe, boolean bread) {
        static final Wants NONE = new Wants(false, false);
    }

    private static final Predicate<ItemStack> IRON = s -> s.is(Items.IRON_INGOT);
    private static final Predicate<ItemStack> WHEAT = s -> s.is(Items.WHEAT);

    private static int n(Map<Mat, Integer> need, Mat m) {
        return need == null ? 0 : need.getOrDefault(m, 0);
    }

    private static boolean wantTools(Inventory inv) {
        boolean stoneMissing = BotItems.rank(inv, PickaxeItem.class) < 2 || BotItems.rank(inv, SwordItem.class) < 2
                || BotItems.rank(inv, AxeItem.class) < 2;
        boolean ironUpgrade = BotItems.count(inv, IRON) >= 2 && (BotItems.rank(inv, PickaxeItem.class) < 3
                || BotItems.rank(inv, SwordItem.class) < 3);
        return stoneMissing || ironUpgrade;
    }

    /** Số ván cần thêm cho đồ base (ô ván + nguyên liệu cửa/rương/giường/rào). */
    private static int basePlanks(Map<Mat, Integer> need) {
        return n(need, Mat.PLANKS) + (n(need, Mat.DOOR) > 0 ? 6 : 0) + 8 * n(need, Mat.CHEST)
                + 3 * n(need, Mat.BED) + (n(need, Mat.FENCE) > 0 ? 4 * ((n(need, Mat.FENCE) + 2) / 3) : 0);
    }

    /** Số gậy cần thêm cho đồ base (thang 7 gậy/3 cái, rào 2 gậy/3 cái, đuốc 1 gậy/4 cái). */
    private static int baseSticks(Map<Mat, Integer> need) {
        return 7 * ((n(need, Mat.LADDER) + 2) / 3) + 2 * ((n(need, Mat.FENCE) + 2) / 3) + (n(need, Mat.TORCH) + 3) / 4;
    }

    // ===== 2x2: chế ngay trong túi =====

    static boolean stepInventory(BotPlayer bot, boolean wantFurnace, boolean tablePlaced, Map<Mat, Integer> need, Wants w) {
        Inventory inv = bot.getInventory();
        int logs = BotItems.count(inv, LOG);
        int planks = BotItems.count(inv, PLANK);
        int sticks = BotItems.count(inv, STICK);
        boolean tools = wantTools(inv);
        boolean hasTable = tablePlaced || BotItems.count(inv, TABLE) > 0; // bàn đang đặt dưới đất cũng tính
        boolean baseCraft = n(need, Mat.DOOR) + n(need, Mat.CHEST) + n(need, Mat.BED) + n(need, Mat.LADDER)
                + n(need, Mat.FENCE) > 0 || w.hoe() || w.bread();
        if ((tools || wantFurnace || baseCraft) && !hasTable && planks >= 4) {
            BotItems.take(inv, PLANK, 4);
            BotItems.give(bot, new ItemStack(Items.CRAFTING_TABLE));
            BotLog.log(bot, "CRAFT", () -> "bàn chế tạo (từ 4 ván, trong túi)");
            return true;
        }
        int needSticks = (tools ? 4 : 0) + baseSticks(need) + (w.hoe() ? 2 : 0);
        if (sticks < needSticks && planks >= 2 && (hasTable || planks >= 6)) {
            BotItems.take(inv, PLANK, 2);
            BotItems.give(bot, new ItemStack(Items.STICK, 4));
            BotLog.log(bot, "CRAFT", () -> "4 gậy (từ 2 ván)");
            return true;
        }
        if (n(need, Mat.TORCH) > 0 && sticks >= 1 && BotItems.count(inv, COAL) >= 1) {
            BotItems.take(inv, COAL, 1);
            BotItems.take(inv, STICK, 1);
            BotItems.give(bot, new ItemStack(Items.TORCH, 4));
            BotLog.log(bot, "CRAFT", () -> "4 đuốc (than + gậy)");
            return true;
        }
        int wantPlanks = Math.max((tools || !hasTable || w.hoe()) ? 12 : 0, basePlanks(need) + 4);
        int keepLogs = n(need, Mat.LOG); // giữ gỗ nguyên khúc làm cột nhà
        if (planks < wantPlanks && logs > keepLogs) {
            BotItems.take(inv, LOG, 1);
            BotItems.give(bot, new ItemStack(Items.OAK_PLANKS, 4));
            BotLog.log(bot, "CRAFT", () -> "4 ván (từ 1 gỗ)");
            return true;
        }
        return false;
    }

    // ===== 3x3: chỉ ở bàn chế tạo =====

    /** Có món 3x3 nào chế được ngay bây giờ không (cần ra bàn chế tạo). */
    static boolean needsTable(BotPlayer bot, boolean wantFurnace, Map<Mat, Integer> need, Wants w) {
        return tableStep(bot, wantFurnace, need, w, true);
    }

    /** Chế một món 3x3. Chỉ gọi khi bot đang đứng cạnh và nhìn vào bàn chế tạo. */
    static boolean stepTable(BotPlayer bot, boolean wantFurnace, Map<Mat, Integer> need, Wants w) {
        return tableStep(bot, wantFurnace, need, w, false);
    }

    private static boolean tableStep(BotPlayer bot, boolean wantFurnace, Map<Mat, Integer> need, Wants w, boolean dry) {
        Inventory inv = bot.getInventory();
        int planks = BotItems.count(inv, PLANK);
        int sticks = BotItems.count(inv, STICK);
        int cobble = BotItems.count(inv, COBBLE);
        int pick = BotItems.rank(inv, PickaxeItem.class);
        int sword = BotItems.rank(inv, SwordItem.class);
        int axe = BotItems.rank(inv, AxeItem.class);

        // Công cụ: cuốc trước tiên vì cần để đào đá
        if (pick == 0 && planks >= 3 && sticks >= 2) return dry || tool(bot, PLANK, 3, Items.WOODEN_PICKAXE, 2);
        if (pick == 1 && cobble >= 3 && sticks >= 2) return dry || tool(bot, COBBLE, 3, Items.STONE_PICKAXE, 2);
        if (sword == 0 && planks >= 2 && sticks >= 1) return dry || tool(bot, PLANK, 2, Items.WOODEN_SWORD, 1);
        if (axe == 0 && planks >= 3 && sticks >= 2) return dry || tool(bot, PLANK, 3, Items.WOODEN_AXE, 2);
        if (sword == 1 && cobble >= 2 && sticks >= 1) return dry || tool(bot, COBBLE, 2, Items.STONE_SWORD, 1);
        if (axe == 1 && cobble >= 3 && sticks >= 2) return dry || tool(bot, COBBLE, 3, Items.STONE_AXE, 2);
        // Đồ sắt (sau khi nung quặng sắt): cuốc trước rồi kiếm, rìu
        int iron = BotItems.count(inv, IRON);
        if (pick == 2 && iron >= 3 && sticks >= 2) return dry || tool(bot, IRON, 3, Items.IRON_PICKAXE, 2);
        if (sword == 2 && iron >= 2 && sticks >= 1) return dry || tool(bot, IRON, 2, Items.IRON_SWORD, 1);
        if (axe == 2 && iron >= 3 && sticks >= 2 && BotItems.rank(inv, PickaxeItem.class) >= 3) return dry || tool(bot, IRON, 3, Items.IRON_AXE, 2);
        // Cuốc xới để trồng trọt
        boolean hasHoe = BotItems.count(inv, s -> s.getItem() instanceof net.minecraft.world.item.HoeItem) > 0;
        if (w.hoe() && !hasHoe) {
            if (cobble >= 2 && sticks >= 2) return dry || tool(bot, COBBLE, 2, Items.STONE_HOE, 2);
            if (planks >= 2 && sticks >= 2) return dry || tool(bot, PLANK, 2, Items.WOODEN_HOE, 2);
        }
        // Bánh mì: 3 lúa mì
        if (w.bread() && BotItems.count(inv, WHEAT) >= 3) return dry || make(bot, WHEAT, 3, null, 0, Items.BREAD, 1);
        // Lò: để nấu ăn, hoặc base cần
        boolean furnaceNeeded = wantFurnace || n(need, Mat.FURNACE) > 0;
        if (furnaceNeeded && cobble >= 8 && BotItems.count(inv, s -> s.is(Items.FURNACE)) == 0) {
            return dry || make(bot, COBBLE, 8, null, 0, Items.FURNACE, 1);
        }
        // Đồ cho base
        if (n(need, Mat.DOOR) > 0 && planks >= 6) return dry || make(bot, PLANK, 6, null, 0, Items.OAK_DOOR, 3);
        if (n(need, Mat.CHEST) > 0 && planks >= 8) return dry || make(bot, PLANK, 8, null, 0, Items.CHEST, 1);
        if (n(need, Mat.BED) > 0 && planks >= 3) {
            Item wool = woolWithAtLeast(inv, 3);
            if (wool != null) {
                if (dry) return true;
                final Item woolItem = wool;
                return make(bot, PLANK, 3, s -> s.is(woolItem), 3, BaseWork.bedForWool(wool), 1);
            }
        }
        if (n(need, Mat.LADDER) > 0 && sticks >= 7) return dry || make(bot, STICK, 7, null, 0, Items.LADDER, 3);
        if (n(need, Mat.FENCE) > 0 && planks >= 4 && sticks >= 2) return dry || make(bot, PLANK, 4, STICK, 2, Items.OAK_FENCE, 3);
        return false;
    }

    /** Màu len có ít nhất n khối (giường cần 3 len cùng màu). */
    private static Item woolWithAtLeast(Inventory inv, int n) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && WOOL.test(s)) {
                Item w = s.getItem();
                if (BotItems.count(inv, x -> x.is(w)) >= n) return w;
            }
        }
        return null;
    }

    private static boolean tool(BotPlayer bot, Predicate<ItemStack> material, int amount, Item result, int sticks) {
        return make(bot, material, amount, STICK, sticks, result, 1);
    }

    /** Lấy nguyên liệu (a x na, b x nb) và cho ra result x count. */
    private static boolean make(BotPlayer bot, Predicate<ItemStack> a, int na, Predicate<ItemStack> b, int nb,
                                Item result, int count) {
        Inventory inv = bot.getInventory();
        if (BotItems.count(inv, a) < na || (b != null && BotItems.count(inv, b) < nb)) return false;
        BotItems.take(inv, a, na);
        if (b != null) BotItems.take(inv, b, nb);
        BotItems.give(bot, new ItemStack(result, count));
        BotLog.log(bot, "CRAFT", () -> count + " " + result.getDescriptionId() + " (ở bàn chế tạo)");
        return true;
    }
}
