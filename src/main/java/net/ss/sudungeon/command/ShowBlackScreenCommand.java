package net.ss.sudungeon.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.client.gui.BlackScreenOverlay;

@Mod.EventBusSubscriber
public class ShowBlackScreenCommand {
    @SubscribeEvent
    public static void register (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("show_black_screen")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    BlackScreenOverlay.showOverlay();
                    return 1;
                })
        );
    }
}
