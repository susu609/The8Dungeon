package net.susu609.the8dungeon.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.susu609.the8dungeon.Susu609Mod;

public final class ModBlockEntitiesRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Susu609Mod.MODID);

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}