package net.susu609.the8dungeon.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.susu609.the8dungeon.Susu609Mod;

/** Toàn bộ đăng ký âm thanh của mod nằm ở đây. */
public final class ModSoundsRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Susu609Mod.MODID);

    public static final RegistryObject<SoundEvent> CLAWD_AMBIENT = reg("entity.clawd.ambient");
    public static final RegistryObject<SoundEvent> CLAWD_HURT    = reg("entity.clawd.hurt");
    public static final RegistryObject<SoundEvent> CLAWD_DEATH   = reg("entity.clawd.death");
    public static final RegistryObject<SoundEvent> CLAWD_STEP    = reg("entity.clawd.step");
    public static final RegistryObject<SoundEvent> CLAWD_WORK    = reg("entity.clawd.work");
    public static final RegistryObject<SoundEvent> CLAWD_THINK   = reg("entity.clawd.think");
    public static final RegistryObject<SoundEvent> CLAWD_HAPPY   = reg("entity.clawd.happy");

    private ModSoundsRegistry() {}

    private static RegistryObject<SoundEvent> reg(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, id)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}