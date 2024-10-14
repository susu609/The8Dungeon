package net.ss.sudungeon.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.world.entity.player.PlayerCharacterStats;

@Mod.EventBusSubscriber
public class SetCharacterCommand {

    @SubscribeEvent
    public static void registerCommand (RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("setCharacter")
                .requires(source -> source.hasPermission(2)) // Cần có quyền mức 2
                .then(Commands.argument("name", EntityArgument.players())
                        .then(Commands.literal("character")
                                .then(Commands.literal("alex")
                                        .then(Commands.argument("giveItems", BoolArgumentType.bool())
                                                .then(Commands.argument("setAttributes", BoolArgumentType.bool())
                                                        .executes(arguments -> {
                                                            boolean giveItems = arguments.getArgument("giveItems", Boolean.class);
                                                            boolean setAttributes = arguments.getArgument("setAttributes", Boolean.class);
                                                            executeCharacterSetup(arguments, "alex", giveItems, setAttributes);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("steve")
                                        .then(Commands.argument("giveItems", BoolArgumentType.bool())
                                                .then(Commands.argument("setAttributes", BoolArgumentType.bool())
                                                        .executes(arguments -> {
                                                            boolean giveItems = arguments.getArgument("giveItems", Boolean.class);
                                                            boolean setAttributes = arguments.getArgument("setAttributes", Boolean.class);
                                                            executeCharacterSetup(arguments, "steve", giveItems, setAttributes);
                                                            return 1;
                                                        })))))));
    }

    private static void executeCharacterSetup (CommandContext<CommandSourceStack> arguments, String character, boolean giveItems, boolean setAttributes) {
        try {
            Player player = EntityArgument.getPlayer(arguments, "name");
            // Cài đặt skin và mô hình cho nhân vật
            PlayerCharacterStats.setCharacterSkinAndModel(player, character);

            // Nếu `giveItems` là true, cấp item cho người chơi
            if (giveItems) {
                PlayerCharacterStats.giveCharacterItems(player, character);
            }

            // Nếu `setAttributes` là true, cài đặt thuộc tính cho người chơi
            if (setAttributes) {
                PlayerCharacterStats.setCharacterAttributes(player, character);
            }

            arguments.getSource().sendSuccess(() -> Component.literal("Character setup complete for " + character), true);

        } catch (Exception e) {
            arguments.getSource().sendFailure(Component.literal("An error occurred while setting up character: " + e.getMessage()));
        }
    }
}
