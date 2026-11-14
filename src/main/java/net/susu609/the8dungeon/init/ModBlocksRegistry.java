package net.susu609.the8dungeon.init;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.torch.ModUnlitTorchBlock;
import net.susu609.the8dungeon.world.torch.ModUnlitWallTorchBlock;

/**
 * Toàn bộ đăng ký khối của mod nằm ở đây.
 * Đuốc vanilla (torch, wall_torch) cháy hết thì đổi thành torch_off / wall_torch_off.
 * Logic cháy nằm trong mixin ModBlockBehaviour; ở đây chỉ đăng ký hai khối đã tắt.
 */
public final class ModBlocksRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Susu609Mod.MODID);

    // Đuốc đã tắt: không sáng, không hạt
    public static final RegistryObject<Block> TORCH_OFF = BLOCKS.register("torch_off",
            () -> new ModUnlitTorchBlock(torchProperties(), ParticleTypes.SMOKE));
    public static final RegistryObject<Block> WALL_TORCH_OFF = BLOCKS.register("wall_torch_off",
            () -> new ModUnlitWallTorchBlock(torchProperties(), ParticleTypes.SMOKE));

    private ModBlocksRegistry() {}

    /** Thuộc tính giống đuốc vanilla, chỉ khác là không phát sáng. */
    private static BlockBehaviour.Properties torchProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 0)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
