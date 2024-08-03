package net.ss.sudungeon.setup;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldSetup {

    @SubscribeEvent
    public static void onWorldLoad (LevelEvent.Load event) {

        double x = 0.0; // Replace with the desired x-coordinate
        double y = 0.0; // Replace with the desired y-coordinate
        double z = 0.0; // Replace with the desired z-coordinate

        if (event.getLevel() instanceof ServerLevel serverWorld) {
            // 1. sửa Gamerule
            GameRules rules = serverWorld.getGameRules();
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, serverWorld.getServer());
            rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
            rules.getRule(GameRules.RULE_DOMOBLOOT).set(false, serverWorld.getServer());
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(1, serverWorld.getServer());
            rules.getRule(GameRules.RULE_SPAWN_RADIUS).set(0, serverWorld.getServer());
            rules.getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());


            // 2. Tạo biên giới thế giới
            WorldBorder worldBorder = serverWorld.getWorldBorder();
            worldBorder.setCenter(8.5, 8.5);  // Đặt trung tâm tại (8.5, 8.5) để giữa chunk đầu tiên
            worldBorder.setSize(512);        // Đặt kích thước là 512 (32 chunks * 16 blocks/chunk)

            // Final
            WorldSetup.execute(serverWorld, x, y, z);

        }
    }
    

    public static void execute (LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level)
            _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), "deop @a ");
    }


}

