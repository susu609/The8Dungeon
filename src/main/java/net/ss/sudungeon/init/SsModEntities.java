package net.ss.sudungeon.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SsMod.MODID);
    // ...
/*
    public static final RegistryObject<EntityType<RoomMarkerEntity>> ROOM_MARKER = register("room_marker_entity",
            EntityType.Builder.<RoomMarkerEntity>of(RoomMarkerEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(RoomMarkerEntity::new)

                    .sized(1f, 1f));
*/

    private static <T extends Entity> RegistryObject<EntityType<T>> register (String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> entityTypeBuilder.build(registryname));
    }

    @SubscribeEvent
    public static void init (FMLCommonSetupEvent event) {
        // Không cần init cho RoomMarkerEntity
    }
    @SubscribeEvent
    public static void registerAttributes (EntityAttributeCreationEvent event) {
    }
}
