package net.ss.sudungeon.init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.renderer.entity.TargetDummyRenderer;
import net.ss.sudungeon.world.entity.TargetDummy;
public class ModModelLayers {
        public static final ModelLayerLocation RHINO_LAYER = new ModelLayerLocation(
                new ResourceLocation(SsMod.MODID, "rhino_layer"), "main");
}
