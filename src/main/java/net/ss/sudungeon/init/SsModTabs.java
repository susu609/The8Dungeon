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
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SsMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> SU_DUNGEON = REGISTRY.register("su_dungeon",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.ss.su_dungeon")).icon(() -> new ItemStack(Blocks.STONE_BRICKS)).displayItems((parameters, tabData) -> {

                        tabData.accept(SsModBlocks.RANDOM_STONE.get().asItem());
                        tabData.accept(SsModBlocks.RANDOM_STONE_BRICKS.get().asItem());

                    })

                    .build());

    @SubscribeEvent
    public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
/*        if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
          tabData.accept(SsModItems.ROOM_MARKER_ENTITY_SPAWN_EGG.get());
        }*/
    }

}

