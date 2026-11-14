package net.susu609.the8dungeon.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;

public final class ModBlockEntitiesRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Susu609Mod.MODID);

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }    // ID "susu609:torch", phải đăng ký sau ModBlocksRegistry
    public static final RegistryObject<BlockEntityType<ModTorchBlockEntity>> TORCH =
            BLOCK_ENTITIES.register("torch", () -> BlockEntityType.Builder
                    .of(ModTorchBlockEntity::new, ModBlocksRegistry.TORCH.get())
                    .build(null));



}