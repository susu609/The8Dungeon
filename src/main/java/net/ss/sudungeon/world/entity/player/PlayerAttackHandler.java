package net.ss.sudungeon.world.entity.player;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.ss.sudungeon.client.animation.definitions.ModPlayerAnimation;
import net.ss.sudungeon.world.entity.AttackAction;

import java.util.IdentityHashMap;
import java.util.Map;

public class PlayerAttackHandler {

    private static final Map<Player, PlayerAttackHandler> INSTANCES = new IdentityHashMap<>();

    public static PlayerAttackHandler get(Player player) {
        return INSTANCES.computeIfAbsent(player, p -> new PlayerAttackHandler());
    }

    private final AnimationState idle = new AnimationState();
    private final AnimationState hurt = new AnimationState();
    private final AnimationState[] attacks = {
            new AnimationState(), new AnimationState(), new AnimationState()
    };

    private int currentIndex = 0;
    private int attackCooldown = 0;

    public void tick(AbstractClientPlayer player) {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    public void onSwingInput(AbstractClientPlayer player, boolean isSwinging) {
        if (!isSwinging || attackCooldown > 0) return;

        AnimationState attack = attacks[currentIndex];
        attack.stop();
        attack.start(player.tickCount);

        player.level().broadcastEntityEvent(player, (byte) 4); // trigger animation (client side)

        currentIndex = (currentIndex + 1) % 3;
        attackCooldown = 10;
    }

    public AnimationState getActiveAttackAnimation() {
        return attacks[currentIndex];
    }

    public AnimationDefinition getCurrentAnimation() {
        return switch (currentIndex) {
            case 1 -> ModPlayerAnimation.PLAYER_SWORD_ATTACK2;
            case 2 -> ModPlayerAnimation.PLAYER_SWORD_ATTACK3;
            default -> ModPlayerAnimation.PLAYER_SWORD_ATTACK1;
        };
    }

    public AttackAction randomAttack() {
        return switch (Mth.nextInt(RandomSource.create(), 0, 2)) {
            case 0 -> AttackAction.PLAYER_ATTACK_1;
            case 1 -> AttackAction.PLAYER_ATTACK_2;
            default -> AttackAction.PLAYER_ATTACK_3;
        };
    }

    public AnimationState getIdleAnimation() {
        return idle;
    }

    public AnimationState getHurtAnimation() {
        return hurt;
    }

    public AnimationState getAttack1Animation() {
        return attacks[0];
    }

    public AnimationState getAttack2Animation() {
        return attacks[1];
    }

    public AnimationState getAttack3Animation() {
        return attacks[2];
    }

    public void reset() {
        idle.stop();
        hurt.stop();
        for (AnimationState attack : attacks) {
            attack.stop();
        }
        currentIndex = 0;
        attackCooldown = 0;
    }
}
