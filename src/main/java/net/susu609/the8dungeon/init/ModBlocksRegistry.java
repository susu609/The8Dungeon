package net.susu609.the8dungeon.init;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.torch.ModWallTorchBlock;

public class ModBlocksRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Susu609Mod.MODID);

    // ID "susu609:torch"
    public static final RegistryObject<Block> TORCH = BLOCKS.register("torch",
            () -> new ModWallTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.WALL_TORCH),
                    ParticleTypes.FLAME));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}