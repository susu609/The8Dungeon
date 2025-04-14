package net.ss.sudungeon.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModItem extends Item {
    protected final int emeraldCost;

    public ModItem(Properties properties, int emeraldCost) {
        super(properties.stacksTo(1));
        this.emeraldCost = emeraldCost;
    }

    public int getEmeraldCost() {
        return emeraldCost;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }
}
