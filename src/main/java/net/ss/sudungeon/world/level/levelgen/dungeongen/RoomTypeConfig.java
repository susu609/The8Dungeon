package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.util.RandomSource;

public class RoomTypeConfig {
    private float spawnProbability;
    private int maxCount = 1;
    private int currentCount = 0;
    private float weight;
    // Biến trạng thái: Kiểm tra phòng boss và phòng kho báu đã được đặt hay chưa
    private boolean bossRoomPlaced = false;        // Trạng thái cho phòng boss
    private boolean treasureRoomPlaced = false;    // Trạng thái cho phòng kho báu

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

    private RoomType determineRoomType(RandomSource random, int currentRooms, int totalRooms) {
        if (currentRooms == 0) {
            return RoomType.START;
        }
        if (currentRooms == totalRooms - 1) {
            if (!bossRoomPlaced) {
                bossRoomPlaced = true;
                return RoomType.BOSS;
            }
        }

        if (!treasureRoomPlaced && random.nextFloat() < RoomType.TREASURE.getConfig().getSpawnProbability()) {
            treasureRoomPlaced = true;
            return RoomType.TREASURE;
        }

        return RoomType.NORMAL;
    }
}



