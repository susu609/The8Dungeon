package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;

@Mod.EventBusSubscriber
public class SetSkinCommand {

    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("setSkin")
                        .then(Commands.argument("target", EntityArgument.player())  // Thêm đối số cho mục tiêu người chơi
                                .then(Commands.argument("skinName", StringArgumentType.string())
                                        .executes(context -> setSkin(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "skinName"), "wide")))  // Sử dụng 'wide' làm mặc định
                                .then(Commands.literal("slim")  // Tùy chọn cho slim
                                        .then(Commands.argument("skinName", StringArgumentType.string())
                                                .executes(context -> setSkin(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "skinName"), "slim"))))
                                .then(Commands.literal("wide")  // Tùy chọn cho wide
                                        .then(Commands.argument("skinName", StringArgumentType.string())
                                                .executes(context -> setSkin(context.getSource(), EntityArgument.getPlayer(context, "target"), StringArgumentType.getString(context, "skinName"), "wide"))))
                        ));
    }

    // Phương thức để thiết lập skin và điều khiển việc ẩn mô hình vanilla
    private static int setSkin (CommandSourceStack source, ServerPlayer targetPlayer, String skinName, String skinType) {
        Level world = targetPlayer.level();

        // Cập nhật biến skinUrl trong PlayerVariables của người chơi mục tiêu
        targetPlayer.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(playerVariables -> {
            // Đồng bộ hóa dữ liệu


            playerVariables.skinUrl = "ss:textures/entity/player/" + skinType + "/" + skinName + ".png";
            playerVariables.isSlim = skinType.equalsIgnoreCase("slim");  // Kiểm tra nếu skin là kiểu "slim"

            // Gửi thông báo đến người dùng
            source.sendSuccess(() -> Component.literal("Skin của " + targetPlayer.getName().getString() + " đã thay đổi thành: " + skinName + " với kiểu: " + skinType), true);


            playerVariables.syncPlayerVariables(targetPlayer);
        });
        return 1;
    }
}
