package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.ss.sudungeon.util.Vars;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber
public class VariableCommand {

    private static final List<String> KEYS = List.of(
            "character", "attack", "defense", "speed", "health", "mana", "critChance"
    );

    private static final Map<String, List<String>> VALUE_SUGGESTIONS = Map.of(
            "character", List.of("steve", "alex", "sunny", "zuri", "efe", "makena", "kai", "noor", "ari")
    );

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("ssvar")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests(VariableCommand::suggestKeys)
                                .executes(ctx -> getPlayerVariable(
                                        EntityArgument.getPlayers(ctx, "target"),
                                        StringArgumentType.getString(ctx, "key"),
                                        ctx
                                ))
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests(VariableCommand::suggestValues)
                                        .executes(ctx -> setPlayerVariable(
                                                EntityArgument.getPlayers(ctx, "target"),
                                                StringArgumentType.getString(ctx, "key"),
                                                StringArgumentType.getString(ctx, "value"),
                                                ctx
                                        ))
                                )
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestKeys(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(KEYS, builder);
    }

    private static CompletableFuture<Suggestions> suggestValues(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String key = StringArgumentType.getString(ctx, "key");
        return SharedSuggestionProvider.suggest(VALUE_SUGGESTIONS.getOrDefault(key, List.of()), builder);
    }

    private static int getPlayerVariable(Collection<ServerPlayer> players, String key, CommandContext<CommandSourceStack> ctx) {
        for (ServerPlayer player : players) {
            var pv = Vars.PP(player);
            String result = switch (key) {
                case "character" -> pv.character;
                case "attack" -> "" + pv.attack;
                case "defense" -> "" + pv.defense;
                case "speed" -> "" + pv.speed;
                case "health" -> "" + pv.health;
                case "mana" -> "" + pv.mana;
                case "critChance" -> "" + pv.critChance;
                default -> "[❌] Key không hợp lệ.";
            };
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + "." + key + " = " + result
            ), false);
        }
        return 1;
    }

    private static int setPlayerVariable(Collection<ServerPlayer> players, String key, String value, CommandContext<CommandSourceStack> ctx) {
        for (ServerPlayer player : players) {
            Vars.setPP(player, pv -> {
                try {
                    switch (key) {
                        case "character" -> pv.character = value;
                        case "attack" -> pv.attack = Double.parseDouble(value);
                        case "defense" -> pv.defense = Double.parseDouble(value);
                        case "speed" -> pv.speed = Double.parseDouble(value);
                        case "health" -> pv.health = Double.parseDouble(value);
                        case "mana" -> pv.mana = Double.parseDouble(value);
                        case "critChance" -> pv.critChance = Double.parseDouble(value);
                        default -> throw new IllegalArgumentException("Key không hợp lệ: " + key);
                    }
                } catch (Exception e) {
                    ctx.getSource().sendFailure(Component.literal("❌ Gán thất bại: " + e.getMessage()));
                }
            });
            ctx.getSource().sendSuccess(() -> Component.literal("✅ Đã gán " + key + " cho " + player.getName().getString()), false);
        }
        return 1;
    }
}
