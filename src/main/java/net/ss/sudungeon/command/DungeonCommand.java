package net.ss.sudungeon.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.manager.DungeonStructureManager;
import net.ss.sudungeon.util.DungeonRandom;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;

import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber
public class DungeonCommand {
    private static final BlockPos ARENA_POS = new BlockPos(-32, 3, -32); // Vị trí arena chính giữa

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dungeon")
                        .then(Commands.literal("create")
                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                        .executes(ctx -> {
                                            long seed = LongArgumentType.getLong(ctx, "seed");
                                            return runCreate(ctx.getSource(), seed, true);
                                        }))
                                .executes(ctx -> {
                                    long seed = new java.util.Random().nextLong();
                                    return runCreate(ctx.getSource(), seed, true);
                                })
                        )
                        .then(Commands.literal("generate_arena")
                                .executes(ctx -> runArena(ctx.getSource(), true))
                        )
        );
    }

    public static void createDungeon(ServerLevel level, long seed) {
        DungeonRandom.setSeed(seed);
        new DrunkardWalk(RoomType.START).generate(level, new BlockPos(0, 0, 0), seed);
        teleportAllPlayersToStart(level);
    }

    public static void createArena(ServerLevel level, long seed) {
        DungeonRandom.setSeed(seed);
        DungeonStructureManager.generateArena(level);
        teleportAllPlayersToStart(level);
    }

    private static int runCreate(CommandSourceStack source, long seed, boolean teleport) {
        ServerLevel level = source.getLevel();
        runFromCode(level, "dungeon", seed);
        if (teleport) teleportAllPlayersToStart(level);
        source.sendSuccess(() -> Component.literal("✅ Dungeon created with seed: " + seed), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int runArena(CommandSourceStack source, boolean teleport) {
        ServerLevel level = source.getLevel();
        DungeonStructureManager.generateArena(level);
        if (teleport) teleportAllPlayersToStart(level);
        source.sendSuccess(() -> Component.literal("✅ Arena created successfully."), true);
        return Command.SINGLE_SUCCESS;
    }

    public static void runFromCode(ServerLevel level, String mode, long seed) {
        DungeonRandom.setSeed(seed);
        if (mode.equalsIgnoreCase("dungeon")) {
            new DrunkardWalk(RoomType.START).generate(level, new BlockPos(0, 0, 0), seed);
        } else if (mode.equalsIgnoreCase("arena")) {
            DungeonStructureManager.generateArena(level);
        } else {
            Log.w("⚠️ Unknown dungeon mode: " + mode);
            return;
        }

        teleportAllPlayersToStart(level);
        Log.i("✅ " + mode + " created with seed: " + seed);
    }

    public static void teleportAllPlayersToStart(ServerLevel level) {
        Map<BlockPos, RoomData> map = DrunkardWalk.dungeonRoomsByDimension.get(level.dimension());

        Optional<BlockPos> startPos = Optional.empty();
        if (map != null) {
            startPos = map.entrySet().stream()
                    .filter(e -> e.getValue().type() == RoomType.START)
                    .map(Map.Entry::getKey)
                    .findFirst();
        }

        BlockPos tp = ARENA_POS;

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            player.teleportTo(level, tp.getX() + 0.5, tp.getY(), tp.getZ() + 0.5, 0, 0);
        }

        if (startPos.isEmpty()) {
            Log.w("[DungeonCommand] ⚠️ No START room found, fallback to ARENA position.");
        }
    }
}
