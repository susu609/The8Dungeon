package net.ss.sudungeon.mixin;


import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    public net.minecraft.world.level.Level getLevel();

    @Invoker("setRot")
    void invokeSetRot(float yRot, float xRot);
}
