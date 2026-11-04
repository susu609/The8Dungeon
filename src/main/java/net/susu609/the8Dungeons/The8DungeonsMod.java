package net.susu609.the8dungeons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Lớp khởi tạo chính của mod The8Dungeons.
 * Nơi đăng ký các tài nguyên, cấu hình và sự kiện chung của mod.
 */
@Mod(The8DungeonsMod.MODID)
public class The8DungeonsMod {

    // Mã định danh của mod; phải trùng với modId trong META-INF/mods.toml.
    public static final String MODID = "susu609";

    // Logger dùng để ghi thông tin và lỗi của mod ra console.
    private static final Logger LOGGER = LogUtils.getLogger();

    // Bộ đăng ký block thuộc namespace "the8dungeons".
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    // Bộ đăng ký item thuộc namespace "the8dungeons".
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Bộ đăng ký các tab sáng tạo riêng của mod.
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Block mẫu có ID: the8dungeons:example_block.
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register(
            "example_block", () -> new Block(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    // Item đại diện cho block mẫu, có cùng ID: the8dungeons:example_block.
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM =
            ITEMS.register(
                    "example_block",
                    () -> new BlockItem(
                            EXAMPLE_BLOCK.get(),
                            new Item.Properties()));

    // Item thực phẩm mẫu: có thể ăn bất cứ lúc nào, độ no 1 và độ bão hòa 2.
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register(
            "example_item", () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().alwaysEat().nutrition(1)
                            .saturationMod(2f).build())));

    // Tab sáng tạo mẫu, được đặt sau tab chiến đấu và hiển thị item mẫu.
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register(
                    "example_tab", () -> CreativeModeTab.builder()
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                // Thêm item mẫu vào tab sáng tạo riêng.
                                output.accept(EXAMPLE_ITEM.get());
                            }).build());

    public The8DungeonsMod() {
        // Lấy Event Bus dành riêng cho quá trình nạp mod.
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Đăng ký hàm thiết lập chung khi mod được nạp.
        modEventBus.addListener(this::commonSetup);

        // Đăng ký các bộ tài nguyên với Event Bus của mod.
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Đăng ký lớp này với Event Bus toàn cục để nhận sự kiện của Forge.
        MinecraftForge.EVENT_BUS.register(this);

        // Đăng ký hàm thêm nội dung vào các tab sáng tạo có sẵn.
        modEventBus.addListener(this::addCreative);

        // Đăng ký cấu hình chung để Forge tạo và tải tệp cấu hình.
        ModLoadingContext.get()
                .registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Hàm chạy trong giai đoạn thiết lập chung của mod.
        LOGGER.info("The8Dungeons: Đang thực hiện thiết lập chung.");

        // Ghi lại ID của block đất để kiểm tra hệ thống đăng ký.
        LOGGER.info("Block đất >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) {
            LOGGER.info("Block đất >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        }

        // In thông điệp và giá trị số được lấy từ cấu hình.
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        // In danh sách item được khai báo trong cấu hình.
        Config.items.forEach(item -> LOGGER.info("Item >> {}", item));
    }

    // Thêm block mẫu vào tab "Khối xây dựng" mặc định của Minecraft.
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // Sự kiện được gọi khi máy chủ Minecraft bắt đầu khởi động.
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("The8Dungeons: Máy chủ đang khởi động.");
    }

    // Nhóm sự kiện chỉ chạy ở phía client (máy người chơi).
    @Mod.EventBusSubscriber(
            modid = MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static class ClientModEvents {

        // Sự kiện thiết lập client sau khi Minecraft khởi tạo.
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("The8Dungeons: Đang thiết lập client.");

            // In tên người chơi hiện tại để kiểm tra môi trường client.
            LOGGER.info("Tên người chơi >> {}",
                    Minecraft.getInstance().getUser().getName());
        }
    }
}
