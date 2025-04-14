package net.ss.sudungeon.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.level.block.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SsModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, SsMod.MODID);

    // Lưu danh sách tên khối và cách tạo khối
    private static final Map<String, Supplier<Block>> BLOCKS_TO_REGISTER = new HashMap<>();

    // Thêm khối cần đăng ký ở đây
    static {
        // Đăng ký tên khối và loại khối tương ứng
        BLOCKS_TO_REGISTER.put("random_stone", RandomStone::new); // RandomStone với cơ chế đặc biệt
        BLOCKS_TO_REGISTER.put("random_stone_bricks", RandomStoneBricks::new); // RandomStone với cơ chế đặc biệt
    }

    // Lưu danh sách các khối đã đăng ký vào RegistryObject
    public static final Map<String, RegistryObject<Block>> REGISTERED_BLOCKS = new HashMap<>();

    // Duyệt qua danh sách và đăng ký khối
    static {
        BLOCKS_TO_REGISTER.forEach((name, supplier) -> {
            RegistryObject<Block> block = register(name, supplier);
            REGISTERED_BLOCKS.put(name, block);
            Log.i("Đăng ký block: " + name);
        });
    }

    // Helper method
    private static RegistryObject<Block> register(String name, Supplier<Block> blockSupplier) {
        return REGISTRY.register(name, blockSupplier);
    }

    // Tiện ích - ném ngoại lệ nếu khối không tồn tại
    public static Block getBlockByName (String blockName) {
        RegistryObject<Block> blockObj = REGISTERED_BLOCKS.get(blockName);
        if (blockObj != null && blockObj.isPresent()) {
            return blockObj.get();
        } else {
            throw new IllegalArgumentException("Khối '" + blockName + "' không tồn tại hoặc chưa được đăng ký!");
        }
    }
}