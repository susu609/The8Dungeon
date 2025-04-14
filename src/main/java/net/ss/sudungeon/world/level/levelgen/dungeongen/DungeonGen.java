// DungeonGen.java (Interface)
package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public interface DungeonGen {
    void generate (@NotNull ServerLevel world, @NotNull BlockPos startPos, long seed);

    void clearDungeon (ServerLevel level);
}
