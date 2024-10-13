package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;

@Mod.EventBusSubscriber
public class VariablesCommand {

    @SubscribeEvent
    public static void onCommandRegister (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("setVar")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerNames(), builder))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"PlayerVariables", "MapVariables", "WorldVariables"}, builder))
                                .then(Commands.argument("variable", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"mana", "maxMana", "stamina", "maxStamina", "critRate", "critDamageMultiple", "health", "defense", "level"}, builder))
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(context -> {
                                                    String playerName = StringArgumentType.getString(context, "player");
                                                    String type = StringArgumentType.getString(context, "type");
                                                    String varName = StringArgumentType.getString(context, "variable");
                                                    double value = DoubleArgumentType.getDouble(context, "value");

                                                    // Tìm người chơi dựa theo tên
                                                    ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
                                                    if (targetPlayer == null) {
                                                        context.getSource().sendFailure(Component.literal("Người chơi không tồn tại!"));
                                                        return 0;
                                                    }

                                                    // Xử lý PlayerVariables cho người chơi đã chọn
                                                    if (type.equalsIgnoreCase("PlayerVariables")) {
                                                        targetPlayer.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {
                                                            switch (varName) {
                                                                case "mana" -> vars.mana = value;
                                                                case "maxMana" -> vars.maxMana = value;
                                                                case "stamina" -> vars.stamina = value;
                                                                case "maxStamina" -> vars.maxStamina = value;
                                                                case "critRate" -> vars.critRate = value;
                                                                case "critDamageMultiple" ->
                                                                        vars.critDamageMultiple = value;
                                                                case "health" -> vars.health = (int) value;
                                                                case "defense" -> vars.defense = (int) value;
                                                                case "level" -> vars.level = (int) value;
                                                                // Thêm các biến khác của PlayerVariables
                                                            }
                                                            vars.syncPlayerVariables(targetPlayer);
                                                        });
                                                    }
                                                    // Xử lý kiểu MapVariables và WorldVariables như trước
                                                    else if (type.equalsIgnoreCase("MapVariables")) {
                                                        SsModVariables.MapVariables.get(targetPlayer.level()).GLOBAL_MAP = value;
                                                        SsModVariables.MapVariables.get(targetPlayer.level()).syncData(targetPlayer.level());
                                                    } else if (type.equalsIgnoreCase("WorldVariables")) {
                                                        SsModVariables.WorldVariables.get(targetPlayer.level()).GLOBAL_WORLD = value;
                                                        SsModVariables.WorldVariables.get(targetPlayer.level()).syncData(targetPlayer.level());
                                                    }

                                                    context.getSource().sendSuccess(() -> Component.literal("Biến đã được cập nhật thành công!"), true);
                                                    return 1;
                                                }))))));
        ;
    }
}