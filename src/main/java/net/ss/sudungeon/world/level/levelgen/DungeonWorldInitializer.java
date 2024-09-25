/*
package net.ss.sudungeon.world.level.levelgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ss")
public class DungeonWorldInitializer {

    public static final ResourceKey<Level> DUNGEON_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ss", "dungeon_dimension"));

    @SubscribeEvent
    public static void onServerStarting (ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        // Kiểm tra nếu chiều không gian của bạn đã được tạo
        ServerLevel dungeonWorld = server.getLevel(DUNGEON_LEVEL_KEY);
        // Đảm bảo người chơi được chuyển vào chiều không gian tùy chỉnh
        server.getPlayerList().getPlayers().forEach(player -> teleportPlayerToDungeon(player, dungeonWorld));
    }

    private static void teleportPlayerToDungeon (ServerPlayer player, ServerLevel dungeonWorld) {
        // Dịch chuyển người chơi đến chiều không gian tùy chỉnh khi thế giới bắt đầu
        player.changeDimension(dungeonWorld);
    }
}
*/
