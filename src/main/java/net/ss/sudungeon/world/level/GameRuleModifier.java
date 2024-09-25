package net.ss.sudungeon.world.level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GameRuleModifier {

    @SubscribeEvent
    public static void onServerStarting (ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        // Ví dụ: sửa GameRule để vô hiệu hóa mob looting
        server.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(false, server);

        // Ví dụ: sửa GameRule để vô hiệu hóa việc sinh ra mob tự nhiên
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);

        // Ví dụ: sửa GameRule để cho phép KeepInventory
        server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);

        server.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(false, server);
        // Thêm các thay đổi GameRule khác nếu cần
    }

}
