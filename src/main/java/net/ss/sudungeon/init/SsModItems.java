package net.ss.sudungeon.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;

public class SsModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, SsMod.MODID);

    public static final RegistryObject<Item> RANDOM_STONE = block(SsModBlocks.RANDOM_STONE);
    public static final RegistryObject<Item> RANDOM_STONE_BRICKS = block(SsModBlocks.RANDOM_STONE_BRICKS);
/*    public static final RegistryObject<Item> ROOM_MARKER_ENTITY_SPAWN_EGG = REGISTRY.register("room_marker_spawn_egg", () -> {
        return new ForgeSpawnEggItem(SsModEntities.ROOM_MARKER, -1, -1, new Item.Properties());
    });*/


    // Start of user code block custom items
    // End of user code block custom items
    private static RegistryObject<Item> block (RegistryObject<Block> block) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }


}
