/*
package net.ss.sudungeon.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ModTextDisplay extends Display.TextDisplay {

    public ModTextDisplay (EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TEXT_ID, Component.literal("Custom Damage"));
        this.entityData.define(DATA_LINE_WIDTH_ID, 200);
        this.entityData.define(DATA_BACKGROUND_COLOR_ID, 0xFF000000);  // Background color
        this.entityData.define(DATA_TEXT_OPACITY_ID, (byte) 255);
    }


    // Custom methods for setting text, background, etc.
    public void setCustomText(String text) {
        this.entityData.set(DATA_TEXT_ID, Component.literal(text));
    }

    public void setCustomOpacity(byte opacity) {
        this.entityData.set(DATA_TEXT_OPACITY_ID, opacity);
    }

    public void setCustomBackgroundColor(int color) {
        this.entityData.set(DATA_BACKGROUND_COLOR_ID, color);
    }
}
*/
