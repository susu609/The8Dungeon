package net.susu609.the8dungeons.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeons.The8DungeonsMod;

@Mod.EventBusSubscriber(modid = The8DungeonsMod.MODID)
public final class VoidSpawn {
    @SubscribeEvent
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        // 1. chỉ xử lý nếu event.getLevel() là ServerLevel (dùng instanceof pattern)
        // event.getLevel() có thể là phía client hoặc server; ta chỉ cần ServerLevel.
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        // Phải là world flat VÀ biome tại gốc tọa độ là the_void, nếu không thì bỏ qua.
        boolean isFlat = level.getChunkSource().getGenerator() instanceof FlatLevelSource;
        boolean isVoid = level.getBiome(BlockPos.ZERO).is(Biomes.THE_VOID);
        if (!isFlat || !isVoid) return;
        // 2. chỉ xử lý nếu bộ sinh là FlatLevelSource VÀ biome tại (0,64,0) là the_void
        //    gợi ý: level.getBiome(pos).is(Biomes.THE_VOID)
        //    (đừng cứ thấy flat là chặn, kẻo phá world flat bình thường)
        // 3. đặt bệ đá 5x5 ở y = 63 quanh (0,63,0): BlockPos.betweenClosed + setBlockAndUpdate
        BlockPos from = new BlockPos(-2, 63, -2);
        BlockPos to = new BlockPos(2, 63, 2);
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        // 4. event.getSettings().setSpawn(new BlockPos(0, 64, 0), 0f);
        // 5. event.setCanceled(true);  // báo vanilla "tôi đã đặt spawn rồi, đừng tìm nữa"
        event.getSettings().setSpawn(new BlockPos(0, 64, 0), 0f);  // đứng trên bệ (y 64 là mặt trên của khối y 63)
        event.setCanceled(true);                                    // "tôi đã xử lý, đừng tự tìm spawn nữa"
    }
}