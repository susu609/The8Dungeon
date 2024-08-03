package net.ss.sudungeon.init;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.level.block.RandomStone;
import net.ss.sudungeon.world.level.block.RandomStoneBricks;


public class SsModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, SsMod.MODID);
    public static final RegistryObject<Block> RANDOM_STONE = REGISTRY.register("random_stone", RandomStone::new);
    public static final RegistryObject<Block> RANDOM_STONE_BRICKS = REGISTRY.register("random_stone_bricks", RandomStoneBricks::new);

    // Start of user code block custom blocks
    // End of user code block custom blocks
}
