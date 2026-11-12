package net.susu609.the8dungeon.dungeon;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.network.ClientboundDungeonSeedPacket;
import net.susu609.the8dungeon.world.WorldRandom;

/**
 * Lệnh thử nhanh:
 * /dungeon build [seed]
 * Vị trí cố định: phòng vào ở chunk (0, 0), sàn ở y = 0, bất kể người chơi đứng đâu.
 * Không có seed thì lấy ngẫu nhiên và in seed ra để chạy lại được.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class DungeonCommand {

    private DungeonCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dungeon").requires(source -> source.hasPermission(2)).then(Commands.literal("build").executes(ctx -> run(ctx.getSource(), System.nanoTime())).then(Commands.argument("seed", LongArgumentType.longArg()).executes(ctx -> run(ctx.getSource(), LongArgumentType.getLong(ctx, "seed"))))));
    }

    private static int run(CommandSourceStack source, long seed) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (source.getLevel().dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Chỉ dựng được dungeon ở Overworld."));
            return 0;
        }

        DungeonParams params = DungeonParams.DEFAULT;

        // Vị trí cố định: phòng vào ở chunk (0, 0), sàn ở y = 0.
        BlockPos origin = DungeonBuilder.fixedOrigin(params);

        DungeonLayout layout;
        try {
            layout = DungeonLayout.generate(new WorldRandom(seed), params);
            DungeonBuilder.build(source.getLevel(), layout, origin);
        } catch (IllegalStateException | IllegalArgumentException e) {
            source.sendFailure(Component.literal("Không dựng được dungeon: " + e.getMessage()));
            return 0;
        }

        // Lưu seed vào world và báo cho mọi client để inspector vẽ được.
        DungeonSavedData.get(source.getLevel()).set(seed);
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new ClientboundDungeonSeedPacket(true, seed));

        // Đưa người chơi vào giữa phòng vào, đứng trên sàn.
        DungeonLayout.Room start = layout.start();
        double cx = origin.getX() + (start.cx() + start.cellsX() / 2.0) * RoomPreset.CELL;
        double cz = origin.getZ() + (start.cz() + start.cellsZ() / 2.0) * RoomPreset.CELL;
        player.teleportTo(cx, origin.getY() + 1, cz);

        source.sendSuccess(() -> Component.literal("Dungeon seed " + seed + ": " + layout.rooms().size() + " phòng, " + layout.deadEndCount() + " phòng cụt"), true);
        return 1;
    }
}
