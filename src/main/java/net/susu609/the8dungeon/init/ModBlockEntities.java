package net.susu609.the8dungeon.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.level.block.entity.ModTorchBlockEntity;

public final class ModBlockEntities {

    // Sổ đăng ký: mọi BlockEntityType của mod đi qua đây
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Susu609Mod.MODID);

    // Loại BlockEntity của đuốc: ID "susu609:dungeon_torch"
    // Cần khối ModBlocks.DUNGEON_TORCH đã đăng ký trước (xem ghi chú)
    public static final RegistryObject<BlockEntityType<ModTorchBlockEntity>> TORCH =
            REGISTRY.register("dungeon_torch", () -> BlockEntityType.Builder
                    .of(ModTorchBlockEntity::new, ModBlocks.DUNGEON_TORCH.get())
                    .build(null));

    private ModBlockEntities() {
    }
}