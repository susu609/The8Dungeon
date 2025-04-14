package net.ss.sudungeon.world.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.packet.OpenModeSelectionPacket;
import net.ss.sudungeon.util.Log;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class DungeonDimension {
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DungeonDimensionSpecialEffectsHandler {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void registerDimensionSpecialEffects (RegisterDimensionSpecialEffectsEvent event) {
            DimensionSpecialEffects customEffect = new DimensionSpecialEffects(192f, true, DimensionSpecialEffects.SkyType.NONE, false, false) {
                @Override
                public @NotNull Vec3 getBrightnessDependentFogColor (@NotNull Vec3 color, float sunHeight) {
                    return color;
                }

                @Override
                public boolean isFoggyAt (int x, int y) {
                    return false;
                }
            };
            event.register(new ResourceLocation("ss:dungeon_dimension"), customEffect);
        }
    }

    public static final ResourceKey<Level> DUNGEON_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("ss:dungeon_dimension")
    );


    @SubscribeEvent
    public static void onPlayerChangedDimensionEvent (PlayerEvent.PlayerChangedDimensionEvent event) {
        Entity entity = event.getEntity();

        if (event.getTo() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dungeon_dimension"))) {
            if (entity instanceof ServerPlayer player) {
                ServerLevel serverWorld = player.serverLevel();
                // Dịch chuyển người chơi
                player.teleportTo(0.5, 30, 0.5); // Dịch chuyển người chơi về trung tâm
                player.setGameMode(GameType.SPECTATOR);
                // Kiểm tra và thiết lập RULE_DAYLIGHT
                boolean isDaylightDisabled = !serverWorld.getGameRules().getRule(GameRules.RULE_DAYLIGHT).get();
                if (!isDaylightDisabled) {
                    serverWorld.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
                    Log.i("RULE_DAYLIGHT đã được đặt thành FALSE.");
                } else {
                    Log.i("RULE_DAYLIGHT đã được đặt và không thay đổi.");
                }

                // Đặt thời gian thành nửa đêm
                serverWorld.setDayTime(18000);
                Log.i("Thời gian đã được đặt thành nửa đêm (18000).");

                // Kiểm tra và thiết lập RULE_DOMOBSPAWNING
                boolean isMobSpawningDisabled = !serverWorld.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).get();
                if (!isMobSpawningDisabled) {
                    serverWorld.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, serverWorld.getServer());
                    Log.i("RULE_DOMOBSPAWNING đã được đặt thành FALSE.");
                } else {
                    Log.i("RULE_DOMOBSPAWNING đã được đặt và không thay đổi.");
                }
                SsMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new OpenModeSelectionPacket());
                Log.i("Gửi yêu cầu hiển thị MainMenuScreen đến " + player.getName().getString());
                generateBarrierPlatform(serverWorld);
                // Thông báo cho người chơi (tùy ý)
                player.sendSystemMessage(Component.literal("Chào mừng tới Dungeon Dimension! Chọn chế độ chơi của bạn."));

            }
        }
    }
    public static void generateBarrierPlatform(ServerLevel level) {
        BlockPos origin = new BlockPos(0, 64, 0); // Y = 64 tuỳ chỉnh độ cao
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                level.setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
            }
        }
    }

}
