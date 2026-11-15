package net.susu609.the8dungeon.init;

import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;

/** Toàn bộ đăng ký item của mod nằm ở đây. */
public final class ModItemsRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Susu609Mod.MODID);

    // Đuốc đã tắt: đặt được trên sàn (torch_off) hoặc gắn tường (wall_torch_off)
    public static final RegistryObject<Item> TORCH_OFF = ITEMS.register("torch_off",
            () -> new StandingAndWallBlockItem(ModBlocksRegistry.TORCH_OFF.get(),
                    ModBlocksRegistry.WALL_TORCH_OFF.get(), new Item.Properties(), Direction.DOWN));

    private ModItemsRegistry() {}

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(ModItemsRegistry::onCreativeTabs);
    }

    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {

        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TORCH_OFF.get());
        }
    }
}
