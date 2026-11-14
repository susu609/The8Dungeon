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

/** Toàn bộ đăng ký khối của mod nằm ở đây. */
public final class ModBlocksRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Susu609Mod.MODID);

    // Đuốc tường có nhiên liệu, ID "susu609:torch" (tái dùng model torch, tắt thì dùng texture torch_off)
    public static final RegistryObject<Block> TORCH = BLOCKS.register("torch",
            () -> new ModWallTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.WALL_TORCH),
                    ParticleTypes.FLAME));

    private ModBlocksRegistry() {}

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
