package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.network.ClientboundDungeonSeedPacket;
import net.susu609.the8dungeon.network.ClientboundVisitedRoomsPacket;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)       // Forge bus (mặc định), giống VoidSpawn
public final class DungeonAutoBuild {
    private DungeonAutoBuild() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        DungeonSavedData data = DungeonSavedData.get(level);
        if (data.present()) return;                       // world cũ: đã có dungeon
        try {
            DungeonLayout layout = DungeonService.build(level, level.getSeed());
            level.setDefaultSpawnPos(BlockPos.containing(DungeonService.startPos(layout)), 0F);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Susu609Mod.LOGGER.error("Không dựng được dungeon", e);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        DungeonSavedData data = DungeonSavedData.get(level);

        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("entered_dungeon")) {
            BlockPos s = level.getSharedSpawnPos();
            player.teleportTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5);
            tag.putBoolean("entered_dungeon", true);
        }
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundDungeonSeedPacket(true, data.seed()));
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(),        // cả nhóm cùng thấy
                new ClientboundVisitedRoomsPacket(visitedArray(data)));

    }

    private static int[] visitedArray(DungeonSavedData data) {
        return data.visited().stream().mapToInt(Integer::intValue).toArray();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;                     // 2 lần/giây là đủ
        DungeonSavedData data = DungeonSavedData.get(player.serverLevel());
        if (!data.present()) return;
        DungeonLayout layout = DungeonService.layout(data.seed());
        int cx = Math.floorDiv(player.getBlockX() + 96, 16);
        int cz = Math.floorDiv(player.getBlockZ() + 96, 16);
        DungeonLayout.Room here = layout.roomAt(cx, cz);
        if (here != null && data.markVisited(here.id())) {
            // ◄ gửi gói VisitedRooms cho ALL (bạn tự viết, giống dòng gửi seed)
            Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(),        // cả nhóm cùng thấy
                    new ClientboundVisitedRoomsPacket(visitedArray(data)));
        }
    }


}