package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ClearDungeonCommand {

    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> clearDungeonCommand = Commands.literal("clear_dungeon")
                .requires(cs -> cs.hasPermission(2))
                .executes(ClearDungeonCommand::execute); // Loại bỏ phần then(Commands.argument("all", BoolArgumentType.bool())
        dispatcher.register(clearDungeonCommand);
    }

    private static int execute (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = context.getSource().getLevel();
        CommandSourceStack source = context.getSource().withPermission(4);

        DungeonSavedData dungeonData = DungeonSavedData.get(level);
        if (dungeonData.getRooms().isEmpty()) {
            source.sendFailure(Component.literal("Lỗi: Không tìm thấy dungeon để xóa!"));
            return 0;
        }

        // Sử dụng Optional và đưa ra thông báo lỗi chi tiết hơn
        Optional.ofNullable(dungeonData.getDungeonGenerator())
                .ifPresentOrElse(
                        dungeonGen -> dungeonGen.clearDungeon(level),
                        () -> {
                            source.sendFailure(Component.literal("Lỗi: Không tìm thấy dungeon generator cho chiều không gian này!"));
                            throw new IllegalStateException("Không tìm thấy dungeon generator cho chiều không gian này"); // Ném ra một exception để dừng việc thực thi
                        });

        source.sendSuccess((Supplier<Component>) Component.literal("Dungeon đã được xóa!").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

}