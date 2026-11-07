package net.susu609.the8dungeons.dungeon;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeons.The8DungeonsMod;
import net.susu609.the8dungeons.world.WorldRandom;

/**
 * Lệnh thử nhanh: /dungeon build standard [seed] /dungeon build small [seed]
 * Dựng dungeon sao cho phòng vào nằm đúng chunk người chơi đang đứng, sàn ở độ
 * cao chân người chơi. Không có seed thì lấy ngẫu nhiên và in seed ra để chạy
 * lại được.
 */
@Mod.EventBusSubscriber(modid = The8DungeonsMod.MODID)
public final class DungeonCommand {

    private DungeonCommand() {
    }
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dungeon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("build")
                        .then(preset("standard", DungeonParams.STANDARD))
                        .then(preset("small", DungeonParams.SMALL))));
    }
    private static LiteralArgumentBuilder<CommandSourceStack> preset(
            String name, DungeonParams params)
    {
        return Commands.literal(name).executes(
                        ctx -> run(ctx.getSource(), params, System.nanoTime()))
                .then(Commands.argument("seed", LongArgumentType.longArg())
                        .executes(ctx -> run(
                                ctx.getSource(), params,
                                LongArgumentType.getLong(ctx, "seed"))));
    }
    private static int run(CommandSourceStack source, DungeonParams params,
            long seed) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos here = player.blockPosition();
        // Căn theo chunk: phòng vào (ô giữa lưới) trùng chunk của người chơi.
        int originX =
                Math.floorDiv(here.getX(), RoomPreset.CELL) * RoomPreset.CELL -
                        (params.gridW() / 2) * RoomPreset.CELL;
        int originZ =
                Math.floorDiv(here.getZ(), RoomPreset.CELL) * RoomPreset.CELL -
                        (params.gridD() / 2) * RoomPreset.CELL;
        BlockPos origin = new BlockPos(originX, here.getY(), originZ);
        DungeonLayout layout;
        try {
            layout = DungeonLayout.generate(new WorldRandom(seed), params);
            DungeonBuilder.build(source.getLevel(), layout, origin);
        } catch (IllegalStateException | IllegalArgumentException e) {
            source.sendFailure(Component.literal(
                    "Không dựng được dungeon: " + e.getMessage()));
            return 0;
        }
        // Đưa người chơi vào giữa phòng vào, đứng trên sàn.
        DungeonLayout.Room start = layout.start();
        double cx =
                originX + (start.cx() + start.cellsX() / 2.0) * RoomPreset.CELL;
        double cz =
                originZ + (start.cz() + start.cellsZ() / 2.0) * RoomPreset.CELL;
        player.teleportTo(cx, origin.getY() + 1, cz);
        source.sendSuccess(
                () -> Component.literal(
                        "Dungeon seed " + seed + ": " + layout.rooms().size() +
                                " phòng, " + layout.deadEndCount() +
                                " phòng cụt"), true);
        return 1;
    }
}
