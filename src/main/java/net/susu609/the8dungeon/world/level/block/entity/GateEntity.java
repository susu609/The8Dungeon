package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Cửa hầm ngục: prop 2x3x1 block, vẽ bằng GateModel.
 * Dữ liệu đồng bộ và lưu/đọc NBT dùng lại của PropEntity (không ghi đè).
 */
public class GateEntity extends PropEntity {

    // Hitbox khớp model "Gate" (32 x 48 x 16 px), sinh bởi plugin Blockbench java_single_texture.
    // Sửa model thì xuất lại và dán đè đoạn này.
    private static final Vec3 SIZE = new Vec3(2.0, 3.0, 1.0);   // rộng X, cao Y, sâu Z (block) khi yaw = 0

    public GateEntity(EntityType<? extends PropEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Vec3 size() {
        return SIZE;
    }
}
