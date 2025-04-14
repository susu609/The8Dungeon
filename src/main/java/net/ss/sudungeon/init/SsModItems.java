package net.ss.sudungeon.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.item.ModEmeraldItem;
import net.ss.sudungeon.item.ModWeaponItem;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.item.FillTool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SsModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, SsMod.MODID);

    // Khai báo map lưu các item cần đăng ký
    private static final Map<String, Supplier<Item>> ITEMS_TO_REGISTER = new HashMap<>();
    public static final Map<String, RegistryObject<Item>> REGISTERED_ITEMS = new HashMap<>();

    // Định nghĩa các item tại đây
    static {
/*
        // ModWeaponItem [emeraldCost = (baseDamage * 2.5) + (baseSpeed * 4) + (range * 1.5)]
        ITEMS_TO_REGISTER.put("wooden_sword", () -> new ModWeaponItem(ModWeaponItem.WeaponType.SWORD, 3, 1.6f, new Item.Properties(), 10, 2.0));
        ITEMS_TO_REGISTER.put("wooden_axe", () -> new ModWeaponItem(ModWeaponItem.WeaponType.AXE, 4, 0.9f, new Item.Properties(), 10, 2.0));
        ITEMS_TO_REGISTER.put("wooden_pickaxe", () -> new ModWeaponItem(ModWeaponItem.WeaponType.PICKAXE, 4, 1.0f, new Item.Properties(), 10, 2.0));
        ITEMS_TO_REGISTER.put("wooden_shovel", () -> new ModWeaponItem(ModWeaponItem.WeaponType.SHOVEL, 3, 1.4f, new Item.Properties(), 10, 2.0));
        ITEMS_TO_REGISTER.put("wooden_hoe", () -> new ModWeaponItem(ModWeaponItem.WeaponType.HOE, 3, 1.7f, new Item.Properties(), 10, 2.5));

        // ModEmeraldItem
        ITEMS_TO_REGISTER.put("small_emerald", () -> new ModEmeraldItem(new Item.Properties(), 1));
        ITEMS_TO_REGISTER.put("emerald", () -> new ModEmeraldItem(new Item.Properties(), 5));
        ITEMS_TO_REGISTER.put("large_emerald", () -> new ModEmeraldItem(new Item.Properties(), 10));
*/

        // Fill tool (Selection tool)
        ITEMS_TO_REGISTER.put("fill_tool", () -> new FillTool(new Item.Properties().stacksTo(1)));
    }

    // Thực hiện đăng ký
    static {
        // Đăng ký các item bình thường
        ITEMS_TO_REGISTER.put("fill_tool", () -> new FillTool(new Item.Properties().stacksTo(1)));

        // ⚠️ Lưu ý: DỜI đăng ký block item xuống đây!
        SsModBlocks.REGISTERED_BLOCKS.forEach((name, blockReg) -> {
            if (!ITEMS_TO_REGISTER.containsKey(name)) {
                ITEMS_TO_REGISTER.put(name, () -> new BlockItem(blockReg.get(), new Item.Properties()));
                Log.i("Thêm BlockItem cho: " + name);
            }
        });

        // Đăng ký toàn bộ item sau cùng
        ITEMS_TO_REGISTER.forEach((name, supplier) -> {
            RegistryObject<Item> item = REGISTRY.register(name, supplier);
            REGISTERED_ITEMS.put(name, item);
            Log.i("Đăng ký item: " + name);
        });
    }


    // Truy cập item bằng tên
    public static Item getItemByName(String name) {
        RegistryObject<Item> itemObj = REGISTERED_ITEMS.get(name);
        if (itemObj != null && itemObj.isPresent()) {
            return itemObj.get();
        } else {
            throw new IllegalArgumentException("Item '" + name + "' không tồn tại hoặc chưa được đăng ký!");
        }
    }
}
