/*
package net.ss.sudungeon.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = SsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandBlocker {

    private static final List<String> ALLOWED_COMMANDS = Arrays.asList("help", "seed", "list"); // Danh sách các lệnh được phép

    @SubscribeEvent
    public static void onCommand (CommandEvent event) {
        String commandName = event.getParseResults().getReader().getString();

        if (!isAllowedCommand(commandName)) {
            event.setCanceled(true); // Hủy bỏ lệnh
            event.getParseResults().getContext().getSource().sendFailure(Component.literal("Bạn không được phép sử dụng lệnh này!"));
        }
    }

    private static boolean isAllowedCommand (String commandName) {
        return ALLOWED_COMMANDS.contains(commandName);
    }
}
*/
