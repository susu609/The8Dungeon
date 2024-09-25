package net.ss.sudungeon.test;

/**
 * @param displayName The display name of the game level
 * @param mapFile     The name or path to the map file for this level
 * @param locked      The lock status of the level (whether it's accessible)
 */
public record GameLevel(String displayName, String mapFile, boolean locked) {
}
