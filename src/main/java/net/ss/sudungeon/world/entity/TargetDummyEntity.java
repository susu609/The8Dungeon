package net.ss.sudungeon.world.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModEntities;
import org.jetbrains.annotations.NotNull;

public class TargetDummyEntity extends Monster {
    public final AnimationState hurtAnimationState = new AnimationState();
    public int hurtAnimationTimeout = 0;

    public TargetDummyEntity(EntityType<TargetDummyEntity> type, Level level) {
        super(type, level);
    }

    public TargetDummyEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.TARGET_DUMMY.get(), level);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (amount <= 0) return false;

        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 33); // hiệu ứng hurt
            // Gửi sự kiện giả để tạo particle sát thương
            net.minecraftforge.eventbus.api.Event event = new net.minecraftforge.event.entity.living.LivingHurtEvent(this, source, amount);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        }

        return true;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 33) {
            hurtAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }
    public static AttributeSupplier.Builder createAttributes() {
        return ModMonster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.ARMOR, 0)

                ;

    }

}
