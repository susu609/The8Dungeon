package net.susu609.the8dungeon.ai;

/** Ảnh chụp trạng thái bot để gửi cho HUD của người đang spectator. */
public record BotInfo(String name, String goal, float hp, int food, int wood, int stone,
                      int pick, boolean sheltered, boolean danger) {
}
