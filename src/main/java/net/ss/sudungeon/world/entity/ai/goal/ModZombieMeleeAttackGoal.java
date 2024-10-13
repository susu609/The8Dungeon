package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;
import net.ss.sudungeon.world.entity.ModZombie;

public class ModZombieMeleeAttackGoal extends MeleeAttackGoal {
    private final ModZombie modZombie;
    private final double localSpeedModifier;
    private static final int ATTACK_DELAY = 10;
    private int attackChargeTick = 0;

    public ModZombieMeleeAttackGoal(ModZombie mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.modZombie = mob;
        this.localSpeedModifier = speedModifier; // Gán giá trị từ speedModifier vào biến cục bộ
    }

    @Override
    public void tick() {
        super.tick();

        if (this.mob.getTarget() == null) {
            return; // Không thực hiện gì nếu không có mục tiêu
        }

        double squaredDistance = this.mob.distanceToSqr(this.mob.getTarget());

        // Kiểm tra nếu trong phạm vi tấn công
        if (squaredDistance <= this.getAttackReachSqr(this.mob.getTarget())) {
            if (attackChargeTick == ATTACK_DELAY) {
                modZombie.performAttack(); // Thực hiện tấn công khi đến tick mong muốn
                this.mob.swing(InteractionHand.MAIN_HAND);
                attackChargeTick = 0;
            }
            this.mob.level().broadcastEntityEvent(this.mob, (byte) 4); // Phát hoạt ảnh tấn công
            attackChargeTick++;
        } else {
            attackChargeTick = 0;
            if (!modZombie.walkAnimationState.isStarted()) {
                modZombie.level().broadcastEntityEvent(modZombie, (byte) 2); // Phát hoạt ảnh walk
            }
            this.mob.getNavigation().moveTo(this.mob.getTarget(), this.localSpeedModifier); // Sử dụng localSpeedModifier để di chuyển
        }
    }
}
