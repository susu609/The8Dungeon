package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber
public class DespawnCommand {
    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> despawnDungeonEntitiesCommand = Commands.literal("despawn")
                .requires((source) -> source.hasPermission(2))
                .executes(DespawnCommand::execute);
        despawnDungeonEntitiesCommand.then(Commands.argument("targets", EntityArgument.entities()).executes((p_137810_) -> despawnDungeonEntities(p_137810_.getSource(), EntityArgument.getEntities(p_137810_, "targets"))));

        dispatcher.register(despawnDungeonEntitiesCommand);
    }

    private static int execute (CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        DungeonSavedData dungeonSavedData = DungeonSavedData.get(level);
        List<RoomData> rooms = dungeonSavedData.getRooms();

        int despawnedCount = rooms.stream().mapToInt(room -> despawnDungeonEntities(context.getSource(), room.entities)).sum();
        context.getSource().sendSuccess(() -> Component.literal("Despawned " + despawnedCount + " entities in the dungeon."), true);
        return despawnedCount;
    }

    // Phương thức để despawn entity
    private static int despawnDungeonEntities (CommandSourceStack source, Collection<? extends Entity> entities) {
        int count = 0;
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) { // Chỉ despawn entity không phải người chơi
                entity.discard();
                count++;
            }
        }

        // Gửi thông báo sau khi đã despawn tất cả entity trong phòng
        if (count == 1) {
            source.sendSuccess(() -> Component.literal("Despawned 1 entity."), true);
        } else {
            int finalCount = count;
            source.sendSuccess(() -> Component.literal("Despawned " + finalCount + " entities."), true);
        }
        return count;
    }

}
