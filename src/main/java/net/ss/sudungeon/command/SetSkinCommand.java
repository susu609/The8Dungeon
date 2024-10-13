package net.ss.sudungeon.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.network.SsModVariables;

@Mod.EventBusSubscriber
public class SetSkinCommand {

    @SubscribeEvent
    public static void registerCommand (RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("setSkin")
                .then(Commands.argument("name", EntityArgument.players()).then(Commands.literal("steve").executes(arguments -> {
                    executeSkinChange(arguments, "steve");
                    return 0;
                })).then(Commands.literal("alex").executes(arguments -> {
                    executeSkinChange(arguments, "alex");
                    return 0;
                }))));
    }

    private static void executeSkinChange (CommandContext<CommandSourceStack> arguments, String skin) {
        Level world = arguments.getSource().getUnsidedLevel();
        Entity entity = arguments.getSource().getEntity();
        if (entity == null && world instanceof ServerLevel _servLevel) {
            entity = FakePlayerFactory.getMinecraft(_servLevel);
        }

        // Custom logic for skin change
        Entity finalEntity = entity;
        assert entity != null;
        entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            switch (skin) {
                case "alex":
                    capability.skinUrl = "ss:textures/entity/player/slim/alex.png";
                    capability.isSlim = true;
                    break;
                case "steve":
                default:
                    capability.skinUrl = "ss:textures/entity/player/wide/steve.png";
                    capability.isSlim = false;
                    break;
            }
            capability.syncPlayerVariables(finalEntity);
        });
    }

}
