package net.ss.sudungeon.world.entity.player;


public class CharacterStatManager {

    // Trả về stat của Steve
    public static CharacterStats getSteveStats() {
        return new CharacterStats(20, 20, 20, 10, 1);  // Steve có các chỉ số trung bình
    }

    // Trả về stat của Alex
    public static CharacterStats getAlexStats() {
        return new CharacterStats(15, 25, 18, 5, 1);  // Alex có ít máu hơn nhưng nhiều mana hơn
    }

    // Trả về stat dựa trên tên nhân vật
    public static CharacterStats getStatsForCharacter(String characterName) {
        if (characterName.equalsIgnoreCase("steve")) {
            return getSteveStats();
        } else if (characterName.equalsIgnoreCase("alex")) {
            return getAlexStats();
        } else {
            // Mặc định nếu không phải Steve hay Alex
            return new CharacterStats(20, 20, 20, 5, 1);  // Các stat mặc định
        }
    }
}
