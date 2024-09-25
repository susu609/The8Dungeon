package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber
public class TeleportDimensionCommand {

    public static final ResourceKey<Level> DUNGEON_DIMENSION = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ss", "dungeon_dimension"));

    @SubscribeEvent
    public static void registerCommands (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("tpdungeon")
                        .requires(cs -> cs.hasPermission(2)) // Permission level 2
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            teleportPlayerToDungeonDimension(player);
                            return 1;
                        })
        );
    }

    public static void teleportPlayerToDungeonDimension (ServerPlayer player) {
        ServerLevel nextLevel = player.server.getLevel(DUNGEON_DIMENSION);

        if (nextLevel != null && !player.level().dimension().equals(DUNGEON_DIMENSION)) {
            SsMod.LOGGER.info("Teleporting player to Dungeon Dimension");

            // Dịch chuyển người chơi đến chiều không gian dungeon_dimension
            player.teleportTo(nextLevel, 8.5, 4, 8.5, player.getYRot(), player.getXRot());
            SsMod.LOGGER.info("Player teleported to Dungeon Dimension at x:8.5, y:4, z:8.5");
        } else if (player.level().dimension().equals(DUNGEON_DIMENSION)) {
            SsMod.LOGGER.info("Player is already in the Dungeon Dimension.");
        } else {
            SsMod.LOGGER.error("Dungeon Dimension does not exist.");
        }
    }
}
