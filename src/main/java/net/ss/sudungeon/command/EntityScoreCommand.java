/*
package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.entity.EntityScoring;

@Mod.EventBusSubscriber

public class EntityScoreCommand {
    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("calculate_entity_score")
                .requires((source) -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entity()).then(Commands.argument("target", EntityArgument.player())
                        .executes(EntityScoreCommand::execute)));

        dispatcher.register(command);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SsMod.LOGGER.info("Bắt đầu thực thi lệnh calculate_entity_score");

        ServerPlayer player = context.getSource().getPlayerOrException();
        Entity entity = EntityArgument.getEntity(context, "target");

        if (entity instanceof LivingEntity livingEntity) {
            double score = EntityScoring.calculateEntityScore(livingEntity); // Sử dụng lớp EntityScoring
            SsMod.LOGGER.info("Điểm số: {}", score);
            player.sendSystemMessage(Component.literal("Điểm số của " + entity.getName().getString() + " là: " + score));
        } else {
            player.sendSystemMessage(Component.literal("Thực thể không hợp lệ. Hãy chọn một sinh vật."));
        }

        return 1;
    }
}*/
