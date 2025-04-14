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
//import net.ss.sudungeon.world.entity.JennyEntity;
import net.ss.sudungeon.world.entity.ModPlayerEntity;
import net.ss.sudungeon.world.entity.ModZombieEntity;
import net.ss.sudungeon.world.entity.TargetDummyEntity;
import net.ss.sudungeon.world.entity.ZombieBossEntity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SsMod.MODID);

    // Đăng ký thực thể ModZombieEntity
    public static final RegistryObject<EntityType<ModZombieEntity>> MOD_ZOMBIE = register("mod_zombie",
            EntityType.Builder.<ModZombieEntity>of(ModZombieEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(ModZombieEntity::new)
                    .sized(0.6f, 1.95f));  // Kích thước của zombie thường

    public static final RegistryObject<EntityType<ZombieBossEntity>> ZOMBIE_BOSS = register("zombie_boss",
            EntityType.Builder.<ZombieBossEntity>of(ZombieBossEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.2f) // cao hơn 1 chút
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .setCustomClientFactory(ZombieBossEntity::new));


    public static final RegistryObject<EntityType<TargetDummyEntity>> TARGET_DUMMY = register("target_dummy",
            EntityType.Builder.<TargetDummyEntity>of(TargetDummyEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(TargetDummyEntity::new)
                    .sized(0.6f, 1.8f)
    );


    public static final RegistryObject<EntityType<ModPlayerEntity>> MOD_PLAYER = register("mod_player",
            EntityType.Builder.<ModPlayerEntity>of(ModPlayerEntity::new, MobCategory.MISC) // hoặc MobCategory.MONSTER tuỳ theo logic bạn muốn
                    .sized(0.6f, 1.8f)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
    );


/*
    public static final RegistryObject<EntityType<JennyEntity>> JENNY = register("mod_zombie",
            EntityType.Builder.<JennyEntity>of(JennyEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(JennyEntity::new)
                    .sized(0.6f, 1.95f));  // Kích thước của zombie thường
*/

    // Phương thức đăng ký thực thể
    private static <T extends Entity> RegistryObject<EntityType<T>> register (String registryName, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryName, () -> entityTypeBuilder.build(registryName));
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {


        });
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Đăng ký các thuộc tính cho ModZombieEntity
        event.put(MOD_ZOMBIE.get(), ModZombieEntity.createAttributes().build());
        event.put(ZOMBIE_BOSS.get(), ZombieBossEntity.createAttributes().build());
        event.put(TARGET_DUMMY.get(), TargetDummyEntity.createAttributes().build());
        event.put(MOD_PLAYER.get(), ModPlayerEntity.createAttributes().build());

//        event.put(JENNY.get(), JennyEntity.createAttributes().build());

    }

}
