package net.ss.sudungeon.command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.util.Log;

@Mod.EventBusSubscriber

public class DimensionCommand {

    private static final ResourceKey<Level> DUNGEON_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation("ss:dungeon_dimension")
    );

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Đăng ký lệnh /teleport_to_dungeon
        dispatcher.register(Commands.literal("teleport_to_dungeon")
                .requires(source -> source.hasPermission(2)) // Chỉ định quyền sử dụng lệnh (level 2 trở lên)
                .executes(context -> {
                    CommandSourceStack source = context.getSource();

                    if (source.getEntity() instanceof ServerPlayer player) {
                        teleportPlayerToDungeon(player);
                        source.sendSuccess(() -> Component.literal("Bạn đã được dịch chuyển đến dimension Dungeon!"), true);
                    } else {
                        source.sendFailure(Component.literal("Chỉ người chơi mới có thể sử dụng lệnh này!"));
                    }

                    return 1; // Thành công
                })
        );
    }

    /**
     * Dịch chuyển người chơi đến dimension Dungeon và đặt điểm spawn.
     */
    private static void teleportPlayerToDungeon(ServerPlayer player) {
        ServerLevel dungeonLevel = player.server.getLevel(DUNGEON_DIMENSION);

        if (dungeonLevel == null) {
            Log.e("Dimension " + DUNGEON_DIMENSION.location() + " không tồn tại!");
            player.sendSystemMessage(Component.literal("Dimension không tồn tại! Hãy kiểm tra lại."));
            return;
        }

        // Dịch chuyển người chơi đến chiều không gian `dungeon_dimension`
        player.teleportTo(dungeonLevel, 0.5, 3, 0.5, player.getYRot(), player.getXRot());
        Log.i("Người chơi " + player.getName().getString() + " đã được dịch chuyển đến " + DUNGEON_DIMENSION.location());

        // Đặt điểm spawn cho người chơi trong dimension mới
        player.setRespawnPosition(DUNGEON_DIMENSION, player.blockPosition(), 0.0F, true, false);
        Log.i("Đặt điểm spawn cho " + player.getName().getString() + " tại dimension " + DUNGEON_DIMENSION.location());
    }
}
