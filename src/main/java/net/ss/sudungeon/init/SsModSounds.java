package net.ss.sudungeon.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;

public class SsModSounds {
public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SsMod.MOD_ID);
public static final RegistryObject<SoundEvent> ZOMBIE_ATTACK = REGISTRY.register("zombie_attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ss", "zombie_attack")));
}
