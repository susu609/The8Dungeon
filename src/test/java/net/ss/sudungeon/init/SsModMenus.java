
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.ss.sudungeon.init;

import net.ss.sudungeon.world.inventory.*;
import net.ss.sudungeon.SsMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

public class SsModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SsMod.MODID);
    public static final RegistryObject<MenuType<TemplatesGuiMenu>> TEMPLATES_GUI = REGISTRY.register("templates_gui", () -> IForgeMenuType.create(TemplatesGuiMenu::new));
}
