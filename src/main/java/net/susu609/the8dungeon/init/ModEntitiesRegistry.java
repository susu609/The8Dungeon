package net.susu609.the8dungeon.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;
import net.susu609.the8dungeon.world.level.block.entity.PedestalEntity;

import static net.minecraft.world.entity.MobCategory.MISC;

/**
 * Toàn bộ đăng ký entity của mod nằm ở đây.
 */
public final class ModEntitiesRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Susu609Mod.MODID);

    public static final RegistryObject<EntityType<PedestalEntity>> PEDESTAL = ENTITIES.register("pedestal",
            () -> Builder.of(PedestalEntity::new, MISC)
                    .sized(1.0F, 1.0F)
                    .build("pedestal"));

    // Cửa hầm ngục 2x3x1 block. sized(rộng, cao): hitbox mặc định vuông 2x2x3;
    // GateEntity.size() + PropEntity.makeBoundingBox() thu lại đúng 2x3x1 và xoay theo hướng cửa.
    public static final RegistryObject<EntityType<GateEntity>> GATE = ENTITIES.register("gate",
            () -> Builder.of(GateEntity::new, MISC)
                    .sized(2.0F, 3.0F)
                    .build("gate"));


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