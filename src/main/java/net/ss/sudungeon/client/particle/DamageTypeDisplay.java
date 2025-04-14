package net.ss.sudungeon.client.particle;

public enum DamageTypeDisplay {
    NORMAL(0xD87C00, 0xD87C00),     // cam đậm, không viền rõ
    CRITICAL(0xFFA94C, 0xD87C00);   // cam sáng, viền cam đậm

    public final int color;
    public final int outline;

    DamageTypeDisplay(int color, int outline) {
        this.color = color;
        this.outline = outline;
    }
}
