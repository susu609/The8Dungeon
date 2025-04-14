package net.ss.sudungeon.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<T, T1> {
    @Accessor("model")
    void setModel(EntityModel<?> model);

    @Accessor("model")
    EntityModel<?> getModel();
}
