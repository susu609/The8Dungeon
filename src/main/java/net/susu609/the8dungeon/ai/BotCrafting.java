package net.susu609.the8dungeon.ai;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;

/**
 * Chế đồ tức thời, không cần bàn chế tạo: bot "biết" công thức cơ bản.
 * Mỗi lần gọi làm tối đa một bước; trả true nếu đã chế được gì đó.
 */
final class BotCrafting {

    private static final Predicate<ItemStack> LOG = s -> s.is(ItemTags.LOGS);
    private static final Predicate<ItemStack> PLANK = s -> s.is(ItemTags.PLANKS);
    private static final Predicate<ItemStack> STICK = s -> s.is(Items.STICK);
    private static final Predicate<ItemStack> COBBLE = s -> s.is(Items.COBBLESTONE);

    private BotCrafting() {}

    private static final Predicate<ItemStack> TABLE = st -> st.is(Items.CRAFTING_TABLE);

    private static boolean wantTools(Inventory inv) {
        return BotItems.rank(inv, PickaxeItem.class) < 2 || BotItems.rank(inv, SwordItem.class) < 2
                || BotItems.rank(inv, AxeItem.class) < 2;
    }

    /**
     * Công thức lưới 2x2 (chế ngay trong túi như người chơi): ván, gậy, bàn chế tạo.
     * Luôn giữ một bàn trong túi để chế đồ 3x3 ở bất cứ đâu.
     */
    static boolean stepInventory(BotPlayer bot, boolean wantFurnace) {
        Inventory inv = bot.getInventory();
        int logs = BotItems.count(inv, LOG);
        int planks = BotItems.count(inv, PLANK);
        int sticks = BotItems.count(inv, STICK);
        boolean tools = wantTools(inv);
        boolean hasTable = BotItems.count(inv, TABLE) > 0;
        if ((tools || wantFurnace) && !hasTable && planks >= 4) {
            BotItems.take(inv, PLANK, 4);
            BotItems.give(bot, new ItemStack(Items.CRAFTING_TABLE));
            BotLog.log(bot, "CRAFT", () -> "bàn chế tạo (từ 4 ván, trong túi)");
            return true;
        }
        if (tools && sticks < 4 && planks >= 2 && (hasTable || planks >= 6)) {
            BotItems.take(inv, PLANK, 2);
            BotItems.give(bot, new ItemStack(Items.STICK, 4));
            BotLog.log(bot, "CRAFT", () -> "4 gậy (từ 2 ván)");
            return true;
        }
        if ((tools || !hasTable) && planks < 12 && logs > 0) {
            BotItems.take(inv, LOG, 1);
            BotItems.give(bot, new ItemStack(Items.OAK_PLANKS, 4));
            BotLog.log(bot, "CRAFT", () -> "4 ván (từ 1 gỗ)");
            return true;
        }
        return false;
    }

    /** Có món 3x3 nào chế được ngay bây giờ không (cần ra bàn chế tạo). */
    static boolean needsTable(BotPlayer bot, boolean wantFurnace) {
        return tableStep(bot, wantFurnace, true);
    }

    /** Chế một món 3x3. Chỉ gọi khi bot đang đứng cạnh và nhìn vào bàn chế tạo. */
    static boolean stepTable(BotPlayer bot, boolean wantFurnace) {
        return tableStep(bot, wantFurnace, false);
    }

    private static boolean tableStep(BotPlayer bot, boolean wantFurnace, boolean dryRun) {
        Inventory inv = bot.getInventory();
        int planks = BotItems.count(inv, PLANK);
        int sticks = BotItems.count(inv, STICK);
        int cobble = BotItems.count(inv, COBBLE);
        int pick = BotItems.rank(inv, PickaxeItem.class);
        int sword = BotItems.rank(inv, SwordItem.class);
        int axe = BotItems.rank(inv, AxeItem.class);

        // Cuốc trước tiên vì cần để đào đá
        if (pick == 0 && planks >= 3 && sticks >= 2) return dryRun || tool(bot, PLANK, 3, Items.WOODEN_PICKAXE);
        if (pick == 1 && cobble >= 3 && sticks >= 2) return dryRun || tool(bot, COBBLE, 3, Items.STONE_PICKAXE);
        if (sword == 0 && planks >= 2 && sticks >= 1) return dryRun || tool(bot, PLANK, 2, Items.WOODEN_SWORD, 1);
        if (axe == 0 && planks >= 3 && sticks >= 2) return dryRun || tool(bot, PLANK, 3, Items.WOODEN_AXE);
        if (sword == 1 && cobble >= 2 && sticks >= 1) return dryRun || tool(bot, COBBLE, 2, Items.STONE_SWORD, 1);
        if (axe == 1 && cobble >= 3 && sticks >= 2) return dryRun || tool(bot, COBBLE, 3, Items.STONE_AXE);
        if (wantFurnace && cobble >= 8 && BotItems.count(inv, st -> st.is(Items.FURNACE)) == 0) {
            if (dryRun) return true;
            BotItems.take(inv, COBBLE, 8);
            BotItems.give(bot, new ItemStack(Items.FURNACE));
            BotLog.log(bot, "CRAFT", () -> "lò (từ 8 đá cuội, ở bàn chế tạo)");
            return true;
        }
        return false;
    }

    private static boolean tool(BotPlayer bot, Predicate<ItemStack> material, int amount, Item result) {
        return tool(bot, material, amount, result, 2);
    }

    private static boolean tool(BotPlayer bot, Predicate<ItemStack> material, int amount, Item result, int sticks) {
        Inventory inv = bot.getInventory();
        if (BotItems.count(inv, STICK) < sticks || !BotItems.take(inv, material, amount)) return false;
        BotItems.take(inv, STICK, sticks);
        BotItems.give(bot, new ItemStack(result));
        BotLog.log(bot, "CRAFT", () -> result.getDescriptionId());
        return true;
    }
}
