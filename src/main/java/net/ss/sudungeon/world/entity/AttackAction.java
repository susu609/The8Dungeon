package net.ss.sudungeon.world.entity;

public enum AttackAction {
    ZOMBIE_ATTACK(
            1, 2f,
            10,   // windupTicks
            12,   // attackTicks
            10,   // recoverTicks
            30,   // cooldownTicks
            2,  // range
            90,   // arc
            false,// multiTarget
            10    // damageTick
    ),

    ZOMBIE_GROUND_SLAM(
            5, 6f,
            14,
            16,
            24,
            40,
            2.5,
            120,
            true,
            10
    ),

    ZOMBIE_WIDE_SLASH(
            6, 5f,
            14,
            16,
            24,
            40,
            4.0,
            160,
            true,
            10
    ),

    PLAYER_ATTACK_1(
        10, 5f,
                6, 10, 8,
                20,
                2.0, 100,
                true,
                8
    ),

    PLAYER_ATTACK_2(
        11, 5f,
                6, 10, 8,
                20,
                2.0, 100,
                true,
                8
    ),

    PLAYER_ATTACK_3(
        12, 6f,
                8, 12, 10,
                24,
                2.5, 50,
                false,
                10
    )




    ;



    public final int id;
    public final float baseDamage;
    public final int windupTicks;
    public final int attackTicks;
    public final int recoverTicks;
    public final int totalTicks;
    public final int cooldownTicks;
    public final double baseRange;
    public final double arcAngle;
    public final boolean multiTarget;
    public final int damageTick;
    private final String displayName;

    AttackAction(int id, float damage, int windup, int attack, int recover, int cooldown,
                 double range, double arcAngle, boolean multiTarget, int damageTick) {
        this.id = id;
        this.baseDamage = damage;
        this.windupTicks = windup;
        this.attackTicks = attack;
        this.recoverTicks = recover;
        this.totalTicks = windupTicks + attackTicks + recoverTicks;
        this.cooldownTicks = cooldown;
        this.baseRange = range;
        this.arcAngle = arcAngle;
        this.multiTarget = multiTarget;
        this.damageTick = damageTick;
        this.displayName = name().toLowerCase().replace("_", " ");
    }

    public String getDisplayName() {
        return displayName;
    }

    public float damage(float scale) {
        return baseDamage * scale;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public int delayTicks() {
        return windupTicks;
    }

    public int damageTick() {
        return damageTick;
    }

    public double range(float scale) {
        return baseRange * scale;
    }

    public double arcAngle() {
        return arcAngle;
    }

    public boolean isMultiTarget() {
        return multiTarget;
    }

    public int totalTicks() {
        return totalTicks;
    }

    public int attackTicks() {
        return attackTicks;
    }

    public int recoverTicks() {
        return recoverTicks;
    }

    public float baseDamage() {
        return baseDamage;
    }

    public double baseRange() {
        return baseRange;
    }
}
