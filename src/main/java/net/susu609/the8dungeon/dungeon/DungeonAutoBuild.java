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

    private static void sendRooms(DungeonSavedData data) {
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new ClientboundVisitedRoomsPacket(data.knownRooms()));
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
        if (!data.present()) return;                       // chưa có dungeon (dựng lỗi): không dịch, không gửi gì

        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("entered_dungeon")) {
            BlockPos s = level.getSharedSpawnPos();
            player.teleportTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5);
            tag.putBoolean("entered_dungeon", true);
        }
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundDungeonSeedPacket(true, data.seed()));
        // Chỉ người vừa vào cần nhận bản đầy đủ; người khác đã có rồi. Gửi SAU seed: client cần layout trước.
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundVisitedRoomsPacket(data.knownRooms()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;                     // 2 lần/giây là đủ
        if (player.level().dimension() != Level.OVERWORLD) return;  // dungeon chỉ ở Overworld
        DungeonSavedData data = DungeonSavedData.get(player.serverLevel());
        if (!data.present()) return;
        DungeonLayout layout = DungeonService.layout(data.seed());
        BlockPos origin = DungeonBuilder.fixedOrigin(layout.params());
        int rx = player.getBlockX() - origin.getX();                // toạ độ tính từ góc dungeon
        int rz = player.getBlockZ() - origin.getZ();
        int lx = Math.floorMod(rx, RoomPreset.CELL);                // vị trí trong ô: 0..15
        int lz = Math.floorMod(rz, RoomPreset.CELL);
        if (lx < 1 || lx > 14 || lz < 1 || lz > 14) return;         // đang ở tường / lỗ cửa: chưa tính là vào
        int cx = Math.floorDiv(rx, RoomPreset.CELL);                // ô lưới
        int cz = Math.floorDiv(rz, RoomPreset.CELL);
        DungeonLayout.Room here = layout.roomAt(cx, cz);

        if (here == null) return;
        ServerLevel level = player.serverLevel();
        RoomState st = data.state(here.id());

        if (st == RoomState.HIDDEN) {
            boolean hasMobs = !RoomCombat.cleared(level, origin, here);
            data.setState(here.id(), hasMobs ? RoomState.COMBAT : RoomState.CLEARED);
            if (hasMobs) RoomCombat.start(level, origin, here, player);
            sendRooms(data);
        } else if (st == RoomState.COMBAT) {
            if (RoomCombat.cleared(level, origin, here)) {
                data.setState(here.id(), RoomState.CLEARED);
                RoomCombat.finish(level, origin, here);
                sendRooms(data);
            }
        }
    }
}
