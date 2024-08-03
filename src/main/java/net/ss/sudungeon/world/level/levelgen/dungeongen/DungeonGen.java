// DungeonGen.java (Interface)
package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.ss.sudungeon.SsMod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public interface DungeonGen {
    void generate (@NotNull ServerLevel world, @NotNull BlockPos startPos, long seed);
}
