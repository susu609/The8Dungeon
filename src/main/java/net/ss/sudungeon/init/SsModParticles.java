
package net.ss.sudungeon.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.client.particle.ParticleNumeric;

import static net.ss.sudungeon.SsMod.LOGGER;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SsModParticles {
    @SubscribeEvent
    public static void registerParticles (RegisterParticleProvidersEvent event) {
        LOGGER.info("Registering particle providers..."); // Thêm dòng log này để kiểm tra
        event.registerSpriteSet(SsModParticleTypes.NUMERIC_PARTICLE.get(), ParticleNumeric::provider);
    }
}

