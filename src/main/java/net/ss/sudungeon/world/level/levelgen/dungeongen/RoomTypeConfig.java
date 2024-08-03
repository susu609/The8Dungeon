package net.ss.sudungeon.world.level.levelgen.dungeongen;

public class RoomTypeConfig {
    private float spawnProbability;
    private int maxCount = 1;
    private int currentCount = 0;
    private float weight;

    public RoomTypeConfig(float spawnProbability, int maxCount, float weight) {
        this.spawnProbability = spawnProbability;
        this.maxCount = maxCount;
        this.weight = weight;
    }

    // Getters and Setters
    public float getSpawnProbability() {
        return spawnProbability;
    }

    public void setSpawnProbability(float spawnProbability) {
        this.spawnProbability = spawnProbability;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    public int getCurrentCount() {
        return currentCount;
    }

    public void incrementCount() {
        currentCount++;
    }
}



