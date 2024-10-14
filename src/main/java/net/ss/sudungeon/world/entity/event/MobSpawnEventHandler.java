/*
package net.ss.sudungeon.world.entity.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import java.util.List;

@Mod.EventBusSubscriber(modid = SsMod.MOD_ID)
public class MobSpawnEventHandler {

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster monster)) {
            return; // Chỉ xử lý quái vật
        }
        applySpawnEffects(monster);


    }

    private static void applySpawnEffects (Monster monster) {
        // Hiệu ứng bất tử và bất động trong 1 giây (20 ticks)
        monster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 10, false, false)); // Bất tử
        monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, false, false)); // Bất động

        // Tạo hiệu ứng hạt (particle) xung quanh quái vật (bạn cần tùy chỉnh hiệu ứng này)
        // ... Ví dụ:
monster.level().addParticle(ParticleTypes.FLAME, monster.getX(), monster.getY(), monster.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
    }
}
*/
