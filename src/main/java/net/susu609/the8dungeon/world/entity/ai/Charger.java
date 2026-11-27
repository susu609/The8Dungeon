package net.susu609.the8dungeon.world.entity.ai;

/** Quái có pha "gồng" trước chiêu; client đọc để diễn cử động báo hiệu. */
public interface Charger {
    boolean isCharging();

    void setCharging(boolean charging);
}
