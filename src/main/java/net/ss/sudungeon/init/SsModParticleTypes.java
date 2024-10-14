
package net.ss.sudungeon.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.ss.sudungeon.SsMod;


public class SsModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SsMod.MOD_ID);
	public static final RegistryObject<SimpleParticleType> NUMERIC_PARTICLE  = REGISTRY.register("numeric_particle", () -> new SimpleParticleType(false));
}
