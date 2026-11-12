package net.susu609.the8dungeon.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.entity.ClawdEntity;

/**
 * Toàn bộ đăng ký entity của mod nằm ở đây.
 */
public final class ModEntitiesRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Susu609Mod.MODID);

    // Kích thước 12x10 px: rộng 0.75 block, cao 0.625 block
    public static final RegistryObject<EntityType<ClawdEntity>> CLAWD_AGENT =
            ENTITIES.register("clawd_agent", () -> EntityType.Builder
                    .of(ClawdEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.625F)
                    .build("clawd_agent"));

    private ModEntitiesRegistry() {
    }

    /**
     * Gọi một lần trong constructor của mod, với mod event bus.
     */
    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(ModEntitiesRegistry::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(CLAWD_AGENT.get(), ClawdEntity.createAttributes().build());
    }
}