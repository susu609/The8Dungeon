package net.ss.sudungeon.utils;

public class PlayerSkinManager {
    private static String selectedCharacter = "steve";

    public static void setSelectedCharacter(String character) {
        selectedCharacter = character;
    }

    public static String getSelectedCharacter() {
        return selectedCharacter;
    }
}