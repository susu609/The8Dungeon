package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.BlackScreenOverlay;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;
import net.ss.sudungeon.world.level.levelgen.dungeongen.exceptions.DungeonCreationException;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber
public class CreateDungeonCommand {

    public static final ResourceKey<Level> DUNGEON_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ss", "dungeon_dimension"));

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> createDungeonCommand = Commands.literal("create_dungeon")
                .requires(cs -> cs.hasPermission(2))
                .executes(CreateDungeonCommand::executeWithRandomSeed);

        createDungeonCommand.then(Commands.literal("random")
                .then(Commands.argument("teleport", BoolArgumentType.bool())
                        .executes(CreateDungeonCommand::executeWithRandomSeedAndTeleport)));

        createDungeonCommand.then(Commands.argument("seed", LongArgumentType.longArg())
                .executes(CreateDungeonCommand::executeWithSeed)
                .then(Commands.argument("teleport", BoolArgumentType.bool())
                        .executes(CreateDungeonCommand::executeWithSeedAndTeleport)));

        dispatcher.register(createDungeonCommand);
    }

    private static int executeWithSeedAndTeleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean teleport = BoolArgumentType.getBool(context, "teleport");
        return execute(context, LongArgumentType.getLong(context, "seed"), teleport);
    }

    private static int executeWithRandomSeedAndTeleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean teleport = BoolArgumentType.getBool(context, "teleport");
        return execute(context, context.getSource().getLevel().getRandom().nextLong(), teleport);
    }

    private static int executeWithSeed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return execute(context, LongArgumentType.getLong(context, "seed"), false);
    }

    private static int executeWithRandomSeed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return execute(context, context.getSource().getLevel().getRandom().nextLong(), false);
    }

    private static int execute(CommandContext<CommandSourceStack> context, long seed, boolean teleport) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        // Lấy dimension mới
        ServerLevel dungeonDimensionLevel = player.server.getLevel(DUNGEON_LEVEL_KEY);
        if (dungeonDimensionLevel == null) {
            SsMod.LOGGER.error("Dungeon dimension not loaded or does not exist.");
            return 0;
        }

        try {
            DungeonSavedData dungeonSavedData = DungeonSavedData.get(dungeonDimensionLevel);

            if (dungeonSavedData.isGenerating()) {
                source.sendFailure(Component.literal("Dungeon đang được tạo. Vui lòng đợi."));
                return 0;
            }

            if (!dungeonSavedData.getRooms().isEmpty()) {
                source.sendFailure(Component.literal("Error: A dungeon already exists! Use /recreate_dungeon to recreate."));
                return 0;
            }

            dungeonSavedData.getRooms().clear();
            dungeonSavedData.setDungeonSeed(seed);
            dungeonSavedData.setDirty();

            // Tạo dungeon mới trong dimension khác
            // Giả định bạn có `level`
            DungeonSavedData data = DungeonSavedData.get(level); // Giả định bạn có `level`
            List<RoomData> roomDataList = data.getRooms();
            DrunkardWalk drunkardWalk = new DrunkardWalk(RoomType.START);

            drunkardWalk.generate(dungeonDimensionLevel, new BlockPos(0, 64, 0), seed);

            if (drunkardWalk.rooms.isEmpty()) {
                SsMod.LOGGER.error("Dungeon creation failed. No rooms were generated.");
                throw new DungeonCreationException("Dungeon creation failed");
            } else {
                dungeonSavedData.setRooms(drunkardWalk.rooms);
                dungeonSavedData.setDirty();
            }

            if (teleport) {
                Optional<RoomData> startRoom = drunkardWalk.rooms.stream()
                        .filter(room -> room.getType() == RoomType.START)
                        .findFirst();
                startRoom.ifPresent(room -> {
                    BlockPos startRoomPos = room.getPosition().offset(8, 1, 8);
                    player.teleportTo(dungeonDimensionLevel, startRoomPos.getX() + 0.5, startRoomPos.getY() + 1.5, startRoomPos.getZ() + 0.5, 0, 0);
                    BlackScreenOverlay.showOverlay();
                });
            }

            player.sendSystemMessage(createSeedMessage(seed));
            return 1;
        } catch (DungeonCreationException e) {
            handleError(source, e);
            return 0;
        }
    }

    static void handleError (CommandSourceStack source, Exception e) {
        SsMod.LOGGER.error("Error executing /create_dungeon command: {}", e);
        source.sendFailure(Component.literal("An error occurred while creating the dungeon."));
    }

    private static Component createSeedMessage(long seed) {
        String seedString = String.valueOf(seed);
        Style style = Style.EMPTY
                .withColor(ChatFormatting.DARK_GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, seedString))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("commands.create.dungeon.seed.tooltip")));

        return Component.literal("Seed: ").append(Component.literal(seedString).withStyle(style));
    }
}
