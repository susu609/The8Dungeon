package net.ss.sudungeon.world.entity.player;

import net.minecraft.world.entity.AnimationState;
import net.ss.sudungeon.world.entity.ModAnimationState;

public interface IPlayerAnim {
    AnimationState getAttackAnimState();
    AnimationState getHurtAnimState();
    AnimationState getDrinkAnimState();
    AnimationState getIdleAnimState();
    AnimationState getWalkAnimState();
    AnimationState getRunAnimState();
}
