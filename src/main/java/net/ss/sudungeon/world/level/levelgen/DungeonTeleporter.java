package net.ss.sudungeon.world.level.levelgen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class DungeonTeleporter implements ITeleporter {
    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        // Dịch chuyển người chơi đến tọa độ cụ thể trong Dungeon Dimension
        entity.moveTo(0, 100, 0);
        return entity;
    }
}
