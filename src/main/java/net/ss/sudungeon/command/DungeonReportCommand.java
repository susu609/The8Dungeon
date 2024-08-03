package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.packet.DungeonDataPacket;

import java.io.IOException;

import static net.ss.sudungeon.command.CreateDungeonCommand.handleError;

@Mod.EventBusSubscriber
public class DungeonReportCommand {

    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("dungeon_report")
                .requires(cs -> cs.hasPermission(2))
                .executes(DungeonReportCommand::execute)
        );
    }

    private static int execute (CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel world = context.getSource().getLevel();
            CommandSourceStack source = context.getSource().withPermission(4);

            DungeonSavedData dungeonData = DungeonSavedData.get(world);
            if (dungeonData.getRooms().isEmpty()) {
                source.sendFailure(Component.literal("Lỗi: Không tìm thấy dungeon để xóa!"));
                return 0;
            }

            // Gửi dữ liệu dungeon đến client để hiển thị
            SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> {
                        try {
                            return context.getSource().getPlayerOrException();
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }),
                    new DungeonDataPacket(dungeonData.getRooms()));

            return 1;
        } catch (Exception e) {
            // Xử lý lỗi cụ thể hơn, ví dụ:
            if (e instanceof IOException) {
                SsMod.LOGGER.error("Lỗi khi đọc hoặc ghi dữ liệu dungeon: {}", e.getMessage());
                context.getSource().sendFailure(Component.literal("Lỗi: Không thể đọc hoặc ghi dữ liệu dungeon."));
            } else if (e instanceof NullPointerException) {
                SsMod.LOGGER.error("Lỗi NullPointerException: {}", e.getMessage());
                context.getSource().sendFailure(Component.literal("Lỗi: Đối tượng null."));
            } else {
                handleError(context.getSource(), e);
            }
            return 0;
        }
    }
}