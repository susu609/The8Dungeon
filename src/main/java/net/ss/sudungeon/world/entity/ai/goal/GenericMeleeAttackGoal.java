/*
package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class GenericMeleeAttackGoal extends MeleeAttackGoal {
    private final Mob mob;
    private final double warningDistanceMultiplier;
    private final SoundEvent warningSound;
    private int warningSoundCooldown = 0;
    private final int warningSoundDelay;
    private final boolean useStandingAnimation;
    private final int standingAnimationDuration;

    public GenericMeleeAttackGoal(Mob mob, double speed, boolean pauseWhenMobIdle,
                                  double warningDistanceMultiplier,
                                  SoundEvent warningSound,
                                  int warningSoundDelay,
                                  boolean useStandingAnimation,
                                  int standingAnimationDuration) {
        super((PathfinderMob) mob, speed, pauseWhenMobIdle);
        this.mob = mob;
        this.warningDistanceMultiplier = warningDistanceMultiplier;
        this.warningSound = warningSound != null ? warningSound : SoundEvents.POLAR_BEAR_WARNING;
        this.warningSoundDelay = warningSoundDelay;
        this.useStandingAnimation = useStandingAnimation;
        this.standingAnimationDuration = standingAnimationDuration;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target, double distanceToTarget) {
        double attackReach = this.getAttackReachSqr(target);

        // Thực hiện tấn công nếu mục tiêu trong phạm vi tấn công và thời gian hồi chiêu đã hết
        if (distanceToTarget <= attackReach && this.isTimeToAttack()) {
            this.resetAttackCooldown();

            // Lấy sát thương từ thuộc tính của mob
            float attackDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // Thực hiện tấn công với sát thương đã lấy
            target.hurt(DamageSource.mobAttack(this.mob), attackDamage);

            // Dừng di chuyển khi tấn công
            this.mob.getNavigation().stop();

            if (this.useStandingAnimation) {
                this.mob.setPose(Pose.STANDING);
            }
        }
        // Kiểm tra nếu mục tiêu ở trong phạm vi cảnh báo nhưng ngoài phạm vi tấn công
        else if (distanceToTarget <= attackReach * this.warningDistanceMultiplier) {
            // Đặt lại hồi chiêu khi trong phạm vi cảnh báo
            if (this.isTimeToAttack()) {
                this.resetAttackCooldown();
            }
            // Phát âm thanh cảnh báo nếu thời gian hồi chiêu đã hết
            if (warningSoundCooldown <= 0) {
                this.mob.playSound(this.warningSound, 1.0F, this.mob.getVoicePitch());
                warningSoundCooldown = this.warningSoundDelay;
            }
            // Đặt hoạt ảnh đứng nếu được bật
            if (this.useStandingAnimation && this.getTicksUntilNextAttack() <= this.standingAnimationDuration) {
                this.mob.setPose(Pose.STANDING);
            }
        }
        // Đặt lại bất kỳ hành vi cảnh báo nào khi ra ngoài phạm vi cảnh báo
        else {
            this.resetAttackCooldown();
            if (this.useStandingAnimation) {
                this.mob.setPose(Pose.CROUCHING);
            }
        }
        // Giảm thời gian hồi chiêu âm thanh cảnh báo
        if (warningSoundCooldown > 0) {
            warningSoundCooldown--;
        }
    }

    @Override
    public void stop() {
        super.stop();
        // Đặt lại tư thế và thời gian hồi chiêu khi dừng tấn công
        if (this.useStandingAnimation) {
            this.mob.setPose(Pose.CROUCHING);
        }
        warningSoundCooldown = 0;
    }

    @Override
    protected double getAttackReachSqr(LivingEntity target) {
        // Cập nhật vùng tấn công để phù hợp với các loại mob khác nhau
        double reach = 0.5; // Sử dụng vùng tấn công mặc định là nửa khối phía trước
        return (reach + target.getBbWidth()) * (reach + target.getBbWidth());
    }
}
*/
