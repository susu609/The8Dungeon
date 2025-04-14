package net.ss.sudungeon.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;

public class SsModTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SsMod.MODID);

    public static final RegistryObject<CreativeModeTab> SU_DUNGEON = REGISTRY.register("su_dungeon",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.ss.su_dungeon"))
                    .icon(() -> new ItemStack(Blocks.REINFORCED_DEEPSLATE))
                    .displayItems((parameters, tabData) -> {
                        // Hiển thị toàn bộ Item
                        SsModItems.REGISTERED_ITEMS.values().forEach(itemReg -> {
                            if (itemReg.isPresent()) {
                                tabData.accept(new ItemStack(itemReg.get(), 1));
                            }
                        });
                    })
                    .build());

    @SubscribeEvent
    public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
        // Nếu sau này muốn thêm vào tab gốc như Combat, Tools, Ingredients, v.v.
    }
}
