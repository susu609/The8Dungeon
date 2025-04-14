package net.ss.sudungeon.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.inventory.FillToolMenu;


public class SsModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SsMod.MODID);
//    public static final RegistryObject<MenuType<ChosenModeGuiMenu>> CHOSEN_MODE_GUI =
//            REGISTRY.register("chosen_mode_gui", () -> IForgeMenuType.create(ChosenModeGuiMenu::new));
//    public static final RegistryObject<MenuType<MenuModGuiMenu>> MENU_MOD_GUI =
//            REGISTRY.register("menu_mod_gui", () -> IForgeMenuType.create(MenuModGuiMenu::new));
//    public static final RegistryObject<MenuType<StarterGearGuiMenu>> STARTER_GEAR_GUI =
//            REGISTRY.register("starter_gear_gui", () -> IForgeMenuType.create(StarterGearGuiMenu::new));
//    public static final RegistryObject<MenuType<ShopGuiMenu>> SHOP_GUI =
//            REGISTRY.register("shop_gui", () -> IForgeMenuType.create(ShopGuiMenu::new));
public static final RegistryObject<MenuType<FillToolMenu>> FILL_TOOL_GUI =
        REGISTRY.register("fill_tool_gui", () ->
                IForgeMenuType.create((windowId, inv, data) -> {
                    return new FillToolMenu(windowId, inv); // Nếu có pos hoặc buf, truyền thêm
                }));

}
