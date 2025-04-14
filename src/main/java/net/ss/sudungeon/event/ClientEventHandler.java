package net.ss.sudungeon.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.mixin.EntityAccessor;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.entity.ModPlayerEntity;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "ss", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    public static final KeyMapping MORPH_KEY = new KeyMapping(
            "key.ss.morph", // dịch key
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M, // phím M
            "key.categories.ss" // category
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        int key = event.getKey();
        int action = event.getAction();

        Minecraft mc = Minecraft.getInstance();



        // Nhấn phím R để đặt lại vị trí entity đại diện
        if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS && mc.player instanceof LocalPlayer) {
            LocalPlayer player = (LocalPlayer) mc.player;
            ClientLevel level = mc.level;
            if (level == null) return;

            level.getEntitiesOfClass(ModPlayerEntity.class, player.getBoundingBox().inflate(10))
                    .stream()
                    .filter(e -> player.getUUID().equals(e.getRealPlayerUUID()))
                    .findFirst()
                    .ifPresent(entity -> {
                        entity.setPos(player.getX(), player.getY(), player.getZ());
                        ((EntityAccessor) entity).invokeSetRot(player.getYRot(), player.getXRot());
                        entity.syncWithPlayer(player);
                        Log.i("ModPlayerEntity", "Đã đặt lại vị trí ModPlayerEntity theo player");
                    });
        }
    }



    @Mod.EventBusSubscriber(modid = "ss", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(MORPH_KEY);
        }
    }
}
