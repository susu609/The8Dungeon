package net.susu609.the8dungeon.init;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.item.ClawdSpawnEggItem;

/** Toàn bộ đăng ký item của mod nằm ở đây. */
public final class ModItemsRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Susu609Mod.MODID);

    // Nền cam #D97757, chấm đen #1C1412 theo mắt Clawd
    public static final RegistryObject<Item> CLAWD_SPAWN_EGG = ITEMS.register("clawd_spawn_egg",
            () -> new ClawdSpawnEggItem(ModEntitiesRegistry.CLAWD_AGENT, 0xD97757, 0x1C1412, new Item.Properties()));

    private ModItemsRegistry() {}

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItemsRegistry::onCreativeTabs);
    }

    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(CLAWD_SPAWN_EGG.get());
        }
    }
}
