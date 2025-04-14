package net.ss.sudungeon.world.entity.player;

import net.minecraft.client.Camera;

public interface CameraAccess {
    void ss_setPosition(double x, double y, double z);
    void ss_setRotation(float yRot, float xRot);
}
