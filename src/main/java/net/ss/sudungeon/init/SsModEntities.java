package net.ss.sudungeon.init;

import net.minecraft.resources.ResourceLocation;
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
import net.ss.sudungeon.world.entity.ColossalZombie;
import net.ss.sudungeon.world.entity.ModZombie;
import net.ss.sudungeon.world.entity.TargetDummy;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SsMod.MODID);

    // Đăng ký thực thể ModZombie
    public static final RegistryObject<EntityType<ModZombie>> MOD_ZOMBIE = register("mod_zombie",
            EntityType.Builder.<ModZombie>of(ModZombie::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(ModZombie::new)
                    .sized(0.6f, 1.95f));  // Kích thước của zombie thường

    // Đăng ký thực thể ColossalZombie
    public static final RegistryObject<EntityType<ColossalZombie>> COLOSSAL_ZOMBIE = REGISTRY.register("colossal_zombie",
            () -> EntityType.Builder.of(ColossalZombie::new, MobCategory.MONSTER)
                    .sized(0.6F * 2, 1.95F * 2) // Kích thước x2 của zombie
                    .build(new ResourceLocation(SsMod.MODID, "colossal_zombie").toString())
    );

    public static final RegistryObject<EntityType<TargetDummy>> TARGET_DUMMY = register("target_dummy",
            EntityType.Builder.<TargetDummy>of(TargetDummy::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(TargetDummy::new)
                    .sized(0.6f, 1.8f));


    // Phương thức đăng ký thực thể
    private static <T extends Entity> RegistryObject<EntityType<T>> register (String registryName, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryName, () -> entityTypeBuilder.build(registryName));
    }

    @SubscribeEvent
    public static void init (FMLCommonSetupEvent event) {
        // Thiết lập ban đầu nếu cần thiết cho ModZombie, ColossalZombie hoặc ModItemEntity
    }

    @SubscribeEvent
    public static void registerAttributes (EntityAttributeCreationEvent event) {
        // Đăng ký các thuộc tính cho ModZombie
        event.put(MOD_ZOMBIE.get(), ModZombie.createAttributes().build());

        // Đăng ký các thuộc tính cho ColossalZombie (mạnh hơn và có thuộc tính đặc biệt)
        event.put(COLOSSAL_ZOMBIE.get(), ColossalZombie.createAttributes().build());
        event.put(TARGET_DUMMY.get(), TargetDummy.createAttributes().build());

    }
}
