package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RecreateDungeonCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("recreate_dungeon")
                .requires(cs -> cs.hasPermission(2))
                .executes(RecreateDungeonCommand::executeWithRandomSeed)
                .then(Commands.argument("seed", LongArgumentType.longArg())
                        .executes(RecreateDungeonCommand::executeWithSeed));
        dispatcher.register(command);
    }

    private static int executeWithSeed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return execute(context, LongArgumentType.getLong(context, "seed"));
    }

    private static int executeWithRandomSeed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return execute(context, context.getSource().getLevel().getRandom().nextLong());
    }

    // Lớp RecreateDungeonCommand
    private static int execute(CommandContext<CommandSourceStack> context, long seed) {
        ServerLevel level = context.getSource().getLevel();
        CommandSourceStack source = context.getSource().withPermission(4);

        // Xóa dungeon hiện tại (nếu có)
        level.getServer().getCommands().performPrefixedCommand(source, "clear_dungeon");

        // Tạo lại dungeon với seed mới
        String createCommand = "create_dungeon " + seed;
        level.getServer().getCommands().performPrefixedCommand(source, createCommand);

        // Thông báo cho người chơi
        source.sendSuccess(() -> Component.literal("Dungeon recreated."), true);
        return 1;
    }
}
