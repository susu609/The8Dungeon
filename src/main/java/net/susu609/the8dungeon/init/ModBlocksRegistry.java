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
 * Đuốc cháy theo chuỗi: torch (vanilla, sáng 14) -> torch_dim (sáng 7) -> torch_off (sáng 0).
 * Mỗi bậc có bản đứng và bản gắn tường, như đuốc vanilla.
 */
public final class ModBlocksRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Susu609Mod.MODID);

    // Đuốc đã tắt: không sáng, không hạt
    public static final RegistryObject<Block> TORCH_OFF = BLOCKS.register("torch_off",
            () -> new ModUnlitTorchBlock(torchProperties(0), ParticleTypes.SMOKE));
    public static final RegistryObject<Block> WALL_TORCH_OFF = BLOCKS.register("wall_torch_off",
            () -> new ModUnlitWallTorchBlock(torchProperties(0), ParticleTypes.SMOKE));

    private ModBlocksRegistry() {}

    /** Thuộc tính giống đuốc vanilla, chỉ khác mức sáng. */
    private static BlockBehaviour.Properties torchProperties(int lightLevel) {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> lightLevel)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
