/*
package net.ss.sudungeon.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.event.PlayerEnterDungeonEvent;


@Mod.EventBusSubscriber(modid = SsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChallengePointsManager {

    private static final int POINTS_PER_MINUTE = 4;
    private static final int POINTS_PER_10_MOBS = 1;
    private static final int POINTS_ON_DUNGEON_ENTER = 1;

    public static LevelAccessor world;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {

        if (event.phase != TickEvent.Phase.END || event.level.isClientSide())
        {
            return;
        }
        SsModVariables.WorldVariables.get(world).maxChallengePoints = SsModVariables.WorldVariables.get(world).maxChallengePoints + POINTS_PER_MINUTE;
        SsModVariables.WorldVariables.get(world).syncData(world);

    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity() instanceof Monster && event.getEntity().level() instanceof ServerLevel serverWorld) {
            SsModVariables.WorldVariables.get(world).maxChallengePoints = SsModVariables.WorldVariables.get(world).maxChallengePoints + POINTS_PER_10_MOBS;
            SsModVariables.WorldVariables.get(world).syncData(world);
        }
    }

    @SubscribeEvent
    public static void onPlayerEnterDungeon(PlayerEnterDungeonEvent event) {
        ServerLevel world = (ServerLevel) event.getPlayer().level();
        SsModVariables.WorldVariables.get(world).maxChallengePoints = SsModVariables.WorldVariables.get(world).maxChallengePoints + POINTS_ON_DUNGEON_ENTER;
        SsModVariables.WorldVariables.get(world).syncData(world);
    }
    // Tăng điểm thử thách
    public static void increaseChallengePoints(ServerPlayer player, int amount) {
        SsModVariables.WorldVariables data = SsModVariables.WorldVariables.get((LevelAccessor) player.getServer());
        data.challengePoints += amount;
        data.maxChallengePoints = Math.max(data.challengePoints, data.maxChallengePoints); // Cập nhật max nếu cần
        data.syncData((LevelAccessor) player.getServer()); // Đồng bộ dữ liệu
    }

    // Reset điểm thử thách về giá trị mặc định
    public static void resetChallengePoints(ServerPlayer player) {
        SsModVariables.WorldVariables data = SsModVariables.WorldVariables.get((LevelAccessor) player.getServer());
        data.challengePoints = data.maxChallengePoints; // Đặt lại về tối đa
        data.syncData((LevelAccessor) player.getServer()); // Đồng bộ dữ liệu
    }

    // Reset điểm thử thách tối đa
    public static void resetMaxChallengePoints(ServerPlayer player) {
        SsModVariables.WorldVariables data = SsModVariables.WorldVariables.get((LevelAccessor) player.getServer());
        data.maxChallengePoints = 20; // Đặt lại về 20
        data.syncData((LevelAccessor) player.getServer()); // Đồng bộ dữ liệu
    }
}*/
