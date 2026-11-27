package net.susu609.the8dungeon.world.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.susu609.the8dungeon.world.entity.ai.Charger;
import net.susu609.the8dungeon.world.entity.ai.DashGoal;
import net.susu609.the8dungeon.world.entity.ai.DashSettings;
import org.jetbrains.annotations.NotNull;

/** Boss phòng BOSS: zombie to 10%, 100 máu, giáp xích + kiếm sắt, có chiêu dash. */
public class GraveKnight extends Zombie implements Charger {
    public static final float SCALE = 1.1F;
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(GraveKnight.class, EntityDataSerializers.BOOLEAN);

    public GraveKnight(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0F);   // không rơi đồ
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);   // boss khó bị đẩy
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new DashGoal<>(this, DashSettings.KNIGHT));   // ưu tiên 1 > tấn công thường
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING, false);
    }

    @Override
    public boolean isCharging() {
        return entityData.get(DATA_CHARGING);
    }

    @Override
    public void setCharging(boolean b) {
        entityData.set(DATA_CHARGING, b);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;   // không cháy nắng
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;   // không biến thành drowned
    }
}
