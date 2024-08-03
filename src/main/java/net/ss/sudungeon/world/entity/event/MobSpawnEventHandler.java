package net.ss.sudungeon.world.entity.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import java.util.List;

@Mod.EventBusSubscriber(modid = "sudungeon") // Thay "sudungeon" bằng modid của bạn
public class MobSpawnEventHandler {

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster monster)) {
            return; // Chỉ xử lý quái vật
        }

        // Kiểm tra xem có phải là baby zombie
        if (monster.getType() == EntityType.ZOMBIE && monster.isBaby()) {
            monster.discard(); // Loại bỏ baby zombie
            BlockPos spawnPos = monster.blockPosition(); // Lấy vị trí spawn của baby zombie

            // Sinh ra một con zombie trưởng thành mới
            Monster newMob = EntityType.ZOMBIE.spawn((ServerLevel) event.getLevel(), spawnPos, MobSpawnType.EVENT);
            if (newMob != null) {
                newMob.finalizeSpawn((ServerLevel) event.getLevel(), event.getLevel().getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
            }
        }

        ServerLevel world = (ServerLevel) event.getLevel();
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(world);
        List<RoomData> rooms = dungeonSavedData.getRooms();

        for (RoomData room : rooms) {
            if (room.isInside(monster.blockPosition())) { // Kiểm tra xem quái vật có trong dungeon không
                applySpawnEffects(monster); // Áp dụng hiệu ứng spawn
                break; // Thoát khỏi vòng lặp nếu đã tìm thấy phòng chứa quái vật
            }
        }
    }

    private static void applySpawnEffects (Monster monster) {
        // Hiệu ứng bất tử và bất động trong 1 giây (20 ticks)
        monster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 10, false, false)); // Bất tử
        monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, false, false)); // Bất động

        // Tạo hiệu ứng hạt (particle) xung quanh quái vật (bạn cần tùy chỉnh hiệu ứng này)
        // ... Ví dụ:
        ((ServerLevel) monster.level()).sendParticles(ParticleTypes.FLAME, monster.getX(), monster.getY(), monster.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
    }
}
