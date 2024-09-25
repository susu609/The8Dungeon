package net.ss.sudungeon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerStatsGui extends Screen {

    private final Player player;

    public PlayerStatsGui (Player player) {
        super(Component.literal("Player Stats"));
        this.player = player;
    }

    @Override
    protected void init () {
        // Initialize GUI components, if any
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Fill background
        this.renderBackground(guiGraphics);

        // Draw the title
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 15, 0xFFFFFF);

        // Fetch player stats
        int experienceLevel = player.experienceLevel;

        // Base stats
        float baseHealth = 20.0f;  // Giá trị máu cơ bản
        float healthBonus = experienceLevel * 0.5f; // Thêm 0.5 máu cho mỗi cấp độ kinh nghiệm
        float currentHealth = player.getHealth();
        float maxHealth = baseHealth + healthBonus;

        // Fetch attack damage
        double baseAttackDamage = 1.0; // Giá trị mặc định nếu thuộc tính không tồn tại
        double attackDamageBonus = 0.0; // Thêm giá trị bonus
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            // Giả sử đây là sát thương cơ bản
            attackDamageBonus = Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getValue() - baseAttackDamage; // Tính phần bonus dựa trên cấp độ hoặc item
        }

        // Display stats
        String health = String.format("Health: %.1f / %.1f (%.1f + %.1f)", currentHealth, maxHealth, baseHealth, healthBonus);
        String damage = String.format("Attack Damage: %.1f (%.1f + %.1f)", baseAttackDamage + attackDamageBonus, baseAttackDamage, attackDamageBonus);
        String hunger = String.format("Hunger: %d", player.getFoodData().getFoodLevel());
        String experience = String.format("Experience Level: %d", experienceLevel);
        // Draw player stats on the screen
        guiGraphics.drawString(this.font, health, this.width / 2 - 100, 50, 0xFFFFFF);
        guiGraphics.drawString(this.font, hunger, this.width / 2 - 100, 70, 0xFFFFFF);
        guiGraphics.drawString(this.font, experience, this.width / 2 - 100, 90, 0xFFFFFF);
        guiGraphics.drawString(this.font, damage, this.width / 2 - 100, 110, 0xFFFFFF); // Attack damage display

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen () {
        return false; // Don't pause the game when the screen is open
    }
}
