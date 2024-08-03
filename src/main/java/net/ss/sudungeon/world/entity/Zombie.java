/*
package net.ss.sudungeon.world.entity;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class Zombie extends net.minecraft.world.entity.monster.Zombie {
    private static final int SPEED_MODIFIER_BABY = 0;

    public Zombie (Level p_34274_) {
        super(p_34274_);
    }

    @Override
    public void setBaby(boolean p_34309_) {
        this.getEntityData().set(DATA_BABY_ID, p_34309_);
        this.level();
        if (!this.level().isClientSide) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            assert attributeinstance != null;
            attributeinstance.removeModifier(SPEED_MODIFIER_BABY);
            if (p_34309_) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }

    }
}
*/
