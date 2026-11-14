package net.susu609.the8dungeon.ai;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;

/** Tiện ích kho đồ cho bot. */
final class BotItems {

    private BotItems() {}

    static int count(Inventory inv, Predicate<ItemStack> test) {
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && test.test(s)) n += s.getCount();
        }
        return n;
    }

    /** Lấy đúng n món nếu đủ; không đủ thì không lấy gì. */
    static boolean take(Inventory inv, Predicate<ItemStack> test, int n) {
        if (count(inv, test) < n) return false;
        for (int i = 0; i < inv.getContainerSize() && n > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !test.test(s)) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
        }
        return true;
    }

    static void give(BotPlayer bot, ItemStack stack) {
        if (!bot.getInventory().add(stack)) bot.drop(stack, false);
    }

    /** 0 = chưa có, 1 = gỗ/vàng, 2 = đá, 3 = sắt, 4 = kim cương... */
    static int rank(Inventory inv, Class<? extends TieredItem> type) {
        int best = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && type.isInstance(s.getItem())) {
                best = Math.max(best, ((TieredItem) s.getItem()).getTier().getLevel() + 1);
            }
        }
        return best;
    }

    /** Khối bot được phép dùng để xây chỗ trú (không dùng ván, ván để chế đồ). */
    static boolean isPlaceable(ItemStack s) {
        return s.is(Items.DIRT) || s.is(Items.COBBLESTONE) || s.is(Items.COARSE_DIRT);
    }

    static ItemStack findPlaceable(Inventory inv) {
        // Ưu tiên đất, để dành đá cuội chế công cụ
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty() || !isPlaceable(s)) continue;
                boolean dirt = !s.is(Items.COBBLESTONE);
                if (dirt == (pass == 0)) return s;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Thức ăn tốt nhất; khi đói nặng mới ăn đồ có hại. */
    static ItemStack bestFood(BotPlayer bot, boolean desperate) {
        Inventory inv = bot.getInventory();
        ItemStack best = ItemStack.EMPTY;
        int bestValue = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !s.isEdible()) continue;
            if (s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO) || s.is(Items.PUFFERFISH)
                    || s.is(Items.CHORUS_FRUIT) || s.is(Items.SUSPICIOUS_STEW)) continue;
            if (!desperate && (s.is(Items.ROTTEN_FLESH) || s.is(Items.CHICKEN))) continue;
            FoodProperties fp = s.getFoodProperties(bot);
            if (fp == null) continue;
            if (fp.getNutrition() > bestValue) {
                bestValue = fp.getNutrition();
                best = s;
            }
        }
        return best;
    }

    /** Chọn dụng cụ đào nhanh nhất cho khối này và cầm lên tay. */
    static void equipBest(BotPlayer bot, BlockState state) {
        Inventory inv = bot.getInventory();
        int bestSlot = -1;
        float bestSpeed = 1.0F;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            float speed = s.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot >= 0) select(inv, bestSlot);
    }

    static void equipWeapon(BotPlayer bot) {
        Inventory inv = bot.getInventory();
        int bestSlot = -1;
        float best = 0.0F;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            float score = 0.0F;
            if (s.getItem() instanceof SwordItem sw) score = 10.0F + sw.getDamage();
            else if (s.getItem() instanceof AxeItem ax) score = 5.0F + ax.getAttackDamage();
            if (score > best) {
                best = score;
                bestSlot = i;
            }
        }
        if (bestSlot >= 0) select(inv, bestSlot);
    }

    private static void select(Inventory inv, int slot) {
        if (slot < 9) {
            inv.selected = slot;
        } else {
            ItemStack held = inv.getItem(inv.selected);
            inv.setItem(inv.selected, inv.getItem(slot));
            inv.setItem(slot, held);
        }
    }
}
