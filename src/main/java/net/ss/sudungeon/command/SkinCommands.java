package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.authlib.properties.Property;

import java.util.function.Supplier;

public class SkinCommands {

    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Đăng ký lệnh "changeskin"
        LiteralArgumentBuilder<CommandSourceStack> changeSkinCommand = Commands.literal("changeskin")
                .requires(cs -> cs.hasPermission(2)) // Yêu cầu quyền để thực hiện
                .then(Commands.argument("skinName", StringArgumentType.word())
                        .executes(context -> {
                            String skinName = StringArgumentType.getString(context, "skinName");
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();

                            if (skinName.equalsIgnoreCase("steve")) {
                                applySkin(player, new ResourceLocation("ss:textures/entity/player/wide/steve.png"));
                                source.sendSuccess((Supplier<Component>) Component.literal("Skin changed to Steve."), true);
                            } else if (skinName.equalsIgnoreCase("alex")) {
                                applySkin(player, new ResourceLocation("ss:textures/entity/player/slim/alex.png"));
                                source.sendSuccess((Supplier<Component>) Component.literal("Skin changed to Alex."), true);
                            } else {
                                source.sendFailure(Component.literal("Skin not found"));
                                return 0;
                            }
                            return 1;
                        }));

        dispatcher.register(changeSkinCommand);
    }

    private static void applySkin(ServerPlayer player, ResourceLocation skinLocation) {
        // Ví dụ này giả định rằng bạn đã có một hệ thống để tải và quản lý skins.
        // Đặt texture mới cho người chơi
        player.getGameProfile().getProperties().removeAll("textures");
        player.getGameProfile().getProperties().put("textures", new Property("textures", "Base64EncodedValue"));

        // Yêu cầu máy chủ gửi thông tin cập nhật skin đến tất cả người chơi khác
        // Phần này phức tạp và phụ thuộc vào việc bạn có thể thay đổi skin mà không cần xác thực lại từ Mojang.
    }

}





