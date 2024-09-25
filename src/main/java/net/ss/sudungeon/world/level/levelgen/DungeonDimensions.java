package net.ss.sudungeon.world.level.levelgen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.gui.gamemodeselection.MainMenuScreen;
import net.ss.sudungeon.command.TeleportDimensionCommand;
import org.jetbrains.annotations.NotNull;

import static net.ss.sudungeon.SsMod.LOGGER;

@Mod.EventBusSubscriber(modid = "ss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonDimensions {

    @SubscribeEvent
    public static void onPlayerLoggedIn (PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            teleportPlayerToDungeon(serverPlayer);  // Gọi hàm teleport
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension (PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getTo().equals(TeleportDimensionCommand.DUNGEON_DIMENSION)) {
                LOGGER.info("Player is now in Dungeon Dimension, teleporting to spawn.");
                teleportPlayerToDungeon(player);

                ServerLevel dungeonLevel = player.server.getLevel(TeleportDimensionCommand.DUNGEON_DIMENSION);
                if (dungeonLevel != null && DungeonSavedData.get(dungeonLevel).getRooms().isEmpty()) {
                    sendToClientMainMenu();  // Chỉ bật menu nếu chưa có dungeon
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn (PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            teleportPlayerToDungeon(serverPlayer);  // Gọi hàm teleport khi hồi sinh
        }
    }

    // Hàm teleport để sử dụng từ lớp khác
    public static void teleportPlayerToDungeon (ServerPlayer player) {
        // Gọi trực tiếp hàm teleport từ lớp TeleportDimensionCommand
        TeleportDimensionCommand.teleportPlayerToDungeonDimension(player);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DungeonDimensionSpecialEffectsHandler {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void registerDimensionSpecialEffects (RegisterDimensionSpecialEffectsEvent event) {
            DimensionSpecialEffects customEffect = new DimensionSpecialEffects(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, false) {
                @Override
                public @NotNull Vec3 getBrightnessDependentFogColor (@NotNull Vec3 color, float sunHeight) {
                    return new Vec3(0.0, 0.0, 0.0);  // Bầu trời màu đen
                }

                @Override
                public boolean isFoggyAt (int x, int y) {
                    return false;
                }
            };
            LOGGER.info("Registering Dimension Special Effects for Dungeon Dimension");
            event.register(new ResourceLocation("ss", "dungeon_dimension"), customEffect);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void sendToClientMainMenu () {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().setScreen(new MainMenuScreen());
            }
        });
    }
}
