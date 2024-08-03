package net.ss.sudungeon.world.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;

public class ModItems extends Items {
    public static final Item WOODEN_AXE = registerItem("wooden_axe", new AxeItem(Tiers.WOOD, 6.0F, -3.2F, new Item.Properties()));

}
