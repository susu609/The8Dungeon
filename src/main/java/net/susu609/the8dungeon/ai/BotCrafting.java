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

    static boolean step(BotPlayer bot) {
        Inventory inv = bot.getInventory();
        int logs = BotItems.count(inv, LOG);
        int planks = BotItems.count(inv, PLANK);
        int sticks = BotItems.count(inv, STICK);
        int cobble = BotItems.count(inv, COBBLE);
        int pick = BotItems.rank(inv, PickaxeItem.class);
        int sword = BotItems.rank(inv, SwordItem.class);
        int axe = BotItems.rank(inv, AxeItem.class);

        // Cuốc trước tiên vì cần để đào đá
        if (pick == 0 && planks >= 3 && sticks >= 2) return tool(bot, PLANK, 3, Items.WOODEN_PICKAXE);
        if (pick < 2 && cobble >= 3 && sticks >= 2 && pick >= 1) return tool(bot, COBBLE, 3, Items.STONE_PICKAXE);
        if (sword == 0 && planks >= 2 && sticks >= 1) return tool(bot, PLANK, 2, Items.WOODEN_SWORD, 1);
        if (axe == 0 && planks >= 3 && sticks >= 2) return tool(bot, PLANK, 3, Items.WOODEN_AXE);
        if (sword < 2 && cobble >= 2 && sticks >= 1 && sword >= 1) return tool(bot, COBBLE, 2, Items.STONE_SWORD, 1);
        if (axe < 2 && cobble >= 3 && sticks >= 2 && axe >= 1) return tool(bot, COBBLE, 3, Items.STONE_AXE);

        boolean wantTools = pick < 2 || sword < 2 || axe < 2;
        if (wantTools && sticks < 4 && planks >= 2) {
            BotItems.take(inv, PLANK, 2);
            BotItems.give(bot, new ItemStack(Items.STICK, 4));
            return true;
        }
        if (wantTools && planks < 8 && logs > 0) {
            BotItems.take(inv, LOG, 1);
            BotItems.give(bot, new ItemStack(Items.OAK_PLANKS, 4));
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
        return true;
    }
}
