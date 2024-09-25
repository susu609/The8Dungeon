package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.utils.DungeonUtils;
import net.ss.sudungeon.world.level.levelgen.dungeongen.DrunkardWalk;

@Mod.EventBusSubscriber
public class ClearDungeonCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("clear_dungeon")
                        .requires(cs -> cs.hasPermission(2)) // Yêu cầu quyền hạn 2
                        .executes(ClearDungeonCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        CommandSourceStack source = context.getSource();

        DungeonSavedData dungeonData = DungeonSavedData.get(level);
        if (dungeonData.getRooms().isEmpty()) {
            source.sendFailure(Component.literal("Lỗi: Không tìm thấy dungeon để xóa!"));
            return 0;
        }

        DrunkardWalk dungeonGen = dungeonData.getDungeonGenerator();
        if (dungeonGen != null) {
            // Xóa tất cả các cấu trúc phòng trong thế giới
            DungeonUtils.clearDungeon(level, dungeonGen.getRooms());

            // Sau khi xóa cấu trúc, tiến hành xóa dữ liệu phòng khỏi DungeonSavedData
            dungeonData.clearRooms();

            source.sendSuccess(() -> Component.literal("Dungeon đã được xóa!").withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Lỗi: Không tìm thấy dungeon generator cho chiều không gian này!"));
            return 0;
        }
    }
}
