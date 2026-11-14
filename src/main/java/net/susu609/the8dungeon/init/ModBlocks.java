package net.susu609.the8dungeon.init;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.torch.ModWallTorchBlock;

public class ModBlocks {

    // Sổ đăng ký khối của mod
    public static final DeferredRegister<Block> REGISTRY =
    DeferredRegister.create(ForgeRegistries.BLOCKS, Susu609Mod.MODID);

    // Đuốc tường có nhiên liệu, ID "susu609:dungeon_torch"
    // Copy thuộc tính từ đuốc tường vanilla, hạt lửa là FLAME
    public static final RegistryObject<Block> DUNGEON_TORCH =
    REGISTRY.register("dungeon_torch", () -> new ModWallTorchBlock(
            BlockBehaviour.Properties.copy(Blocks.WALL_TORCH),
    ParticleTypes.FLAME));

    public ModBlocks() {
    }
}