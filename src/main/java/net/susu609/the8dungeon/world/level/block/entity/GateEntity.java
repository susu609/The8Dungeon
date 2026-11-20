package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class GateEntity extends PropEntity {
    public GateEntity(EntityType<?> p_19870_, Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }
    @Override protected void defineSynchedData() {
    }
    @Override protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }
    @Override protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }
    // GateEntity: GateModel có addBox(..., 32F, 48F, 16F)
    private static final Vec3 SIZE = new Vec3(/* TODO: rộng, cao, sâu theo block */);

    @Override
    protected Vec3 propSize() {
        return SIZE;
    }
}
