package net.ss.sudungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.world.entity.monster.Zombie;
import net.ss.sudungeon.world.entity.ColossalZombie;
import org.jetbrains.annotations.NotNull;

public class ColossalZombieRenderer extends ZombieRenderer {

    public ColossalZombieRenderer (EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale (@NotNull Zombie entity, @NotNull PoseStack poseStack, float partialTickTime) {
        if (entity instanceof ColossalZombie) {
            poseStack.scale(2.0F, 2.0F, 2.0F); // Tăng kích thước của thực thể lên gấp đôi
        }
        super.scale(entity, poseStack, partialTickTime);
    }
}
