package net.susu609.the8dungeon.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;

public final class ModCreativeTabsRegistry {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Susu609Mod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.susu609"))
                    .icon(() -> new ItemStack(ModItemsRegistry.TORCH_OFF.get()))
                    .displayItems((params, output) -> ModItemsRegistry.ITEMS.getEntries()
                            .forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabsRegistry() {}

    /** Gọi một lần trong constructor của mod, với mod event bus. */
    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}