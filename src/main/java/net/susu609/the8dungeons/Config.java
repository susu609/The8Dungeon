package net.susu609.the8dungeons;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quản lý các tùy chọn cấu hình của mod The8Dungeons.
 * Đây là cấu hình mẫu của Forge; có thể mở rộng hoặc thay thế theo nhu cầu mod.
 */
@Mod.EventBusSubscriber(
        modid = The8DungeonsMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER =
            new ForgeConfigSpec.Builder();

    // Bật/tắt việc ghi ID của block đất vào nhật ký khi thiết lập chung.
    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK =
            BUILDER.comment("Có ghi ID của block đất vào nhật ký khi thiết lập chung hay không")
                    .define("logDirtBlock", true);

    // Giá trị số nguyên mẫu, mặc định là 42.
    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER =
            BUILDER.comment("Một giá trị số nguyên mẫu")
                    .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    // Phần nội dung giới thiệu được ghi trước giá trị số mẫu.
    public static final ForgeConfigSpec.ConfigValue<String>
            MAGIC_NUMBER_INTRODUCTION = BUILDER.comment(
                    "Nội dung giới thiệu cho giá trị số mẫu")
            .define("magicNumberIntroduction", "Giá trị số là... ");

    // Danh sách ID item cần ghi vào nhật ký khi thiết lập chung.
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>>
            ITEM_STRINGS =
            BUILDER.comment("Danh sách ID item cần ghi vào nhật ký.")
                    .defineListAllowEmpty(
                            "items",
                            List.of("minecraft:iron_ingot"),
                            Config::validateItemName);

    // Tạo bản cấu hình hoàn chỉnh để đăng ký với Forge.
    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Các giá trị cấu hình sau khi được đọc từ tệp cấu hình.
    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    // Kiểm tra chuỗi có phải ID của item đã đăng ký hay không.
    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName
                && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    // Forge gọi sự kiện này khi cấu hình được tải hoặc nạp lại.
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // Chuyển danh sách ID dạng chuỗi thành tập hợp đối tượng Item.
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }
}
