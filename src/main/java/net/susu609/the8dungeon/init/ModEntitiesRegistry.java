package net.susu609.the8dungeon.init;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.susu609.the8dungeon.Susu609Mod;

/**
 * Toàn bộ đăng ký entity của mod nằm ở đây.
 */
public final class ModEntitiesRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Susu609Mod.MODID);


    private ModEntitiesRegistry() {
    }

    /**
     * Gọi một lần trong constructor của mod, với mod event bus.
     */
    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntitiesRegistry::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
    }
}