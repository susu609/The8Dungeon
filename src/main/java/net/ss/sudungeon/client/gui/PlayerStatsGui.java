package net.ss.sudungeon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.network.SsModVariables;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class PlayerStatsGui extends Screen {

    private final Player player;
    private final Map<Attribute, Double> playerAttributes = new HashMap<>();

    public PlayerStatsGui(Player player) {
        super(Component.literal("Player Stats"));
        this.player = player;
        initializeAttributes();
    }

    private void initializeAttributes() {
        setPlayerAttribute(player, Attributes.ARMOR, player.getAttribute(Attributes.ARMOR) != null ? Objects.requireNonNull(player.getAttribute(Attributes.ARMOR)).getValue() : 2.0);
        setPlayerAttribute(player, Attributes.ATTACK_DAMAGE, player.getAttribute(Attributes.ATTACK_DAMAGE) != null ? Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getValue() : 1.5);
        setPlayerAttribute(player, Attributes.MAX_HEALTH, player.getAttribute(Attributes.MAX_HEALTH) != null ? Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).getValue() : 20.0);
        setPlayerAttribute(player, Attributes.MOVEMENT_SPEED, player.getAttribute(Attributes.MOVEMENT_SPEED) != null ? Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).getValue() : 0.1);
        setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_CHANCE.get(), player.getAttribute(SsModAttributes.CRITICAL_HIT_CHANCE.get()) != null ? Objects.requireNonNull(player.getAttribute(SsModAttributes.CRITICAL_HIT_CHANCE.get())).getValue() : 0.01);
        setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get(), player.getAttribute(SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get()) != null ? Objects.requireNonNull(player.getAttribute(SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get())).getValue() : 1.5);
        setPlayerAttribute(player, SsModAttributes.MAX_MANA.get(), player.getAttribute(SsModAttributes.MAX_MANA.get()) != null ? Objects.requireNonNull(player.getAttribute(SsModAttributes.MAX_MANA.get())).getValue() : 20.0);
        setPlayerAttribute(player, SsModAttributes.MAX_STAMINA.get(), player.getAttribute(SsModAttributes.MAX_STAMINA.get()) != null ? Objects.requireNonNull(player.getAttribute(SsModAttributes.MAX_STAMINA.get())).getValue() : 20.0);
    }

    private void setPlayerAttribute(Player player, Attribute attribute, double value) {
        playerAttributes.put(attribute, value);
    }

    @Override
    protected void init() {
        // Initialize GUI components, if any
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Fill background
        this.renderBackground(guiGraphics);

        // Draw the title
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 15, 0xFFFFFF);

        // Fetch player stats
        int experienceLevel = player.experienceLevel;

        // Fetch and calculate health attributes
        double maxHealth = playerAttributes.getOrDefault(Attributes.MAX_HEALTH, 20.0);
        float currentHealth = player.getHealth();

        // Fetch other attributes
        double attackDamage = playerAttributes.getOrDefault(Attributes.ATTACK_DAMAGE, 1.0);
        double armor = playerAttributes.getOrDefault(Attributes.ARMOR, 0.0);
        double movementSpeed = playerAttributes.getOrDefault(Attributes.MOVEMENT_SPEED, 0.1);
        double criticalHitChance = playerAttributes.getOrDefault(SsModAttributes.CRITICAL_HIT_CHANCE.get(), 0.01);
        double criticalHitDamageMultiple = playerAttributes.getOrDefault(SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get(), 1.5);
        float currentMana = (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).mana;
        float currentStamina = (float) (player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).stamina;
        double maxMana = playerAttributes.getOrDefault(SsModAttributes.MAX_MANA.get(), 20.0);
        double maxStamina = playerAttributes.getOrDefault(SsModAttributes.MAX_STAMINA.get(), 20.0);

        // Display stats
        String health = String.format("Health: %.1f / %.1f", currentHealth, maxHealth);
        String damage = String.format("Attack Damage: %.1f", attackDamage);
        String armorStat = String.format("Armor: %.1f", armor);
        String movement = String.format("Movement Speed: %.2f", movementSpeed);
        String critChance = String.format("Critical Hit Chance: %.2f%%", criticalHitChance * 100);
        String critDamage = String.format("Critical Hit Damage Multiplier: %.1f", criticalHitDamageMultiple);
        String mana = String.format("Mana: %.1f / %.1f", currentMana, maxMana);
        String stamina = String.format("Stamina: %.1f / %.1f", currentStamina, maxStamina);
        String hunger = String.format("Hunger: %d", player.getFoodData().getFoodLevel());
        String experience = String.format("Experience Level: %d", experienceLevel);

        // Draw player stats on the screen
        guiGraphics.drawString(this.font, health, this.width / 2 - 100, 50, 0xFFFFFF);
        guiGraphics.drawString(this.font, damage, this.width / 2 - 100, 70, 0xFFFFFF);
        guiGraphics.drawString(this.font, armorStat, this.width / 2 - 100, 90, 0xFFFFFF);
        guiGraphics.drawString(this.font, movement, this.width / 2 - 100, 110, 0xFFFFFF);
        guiGraphics.drawString(this.font, critChance, this.width / 2 - 100, 130, 0xFFFFFF);
        guiGraphics.drawString(this.font, critDamage, this.width / 2 - 100, 150, 0xFFFFFF);
        guiGraphics.drawString(this.font, mana, this.width / 2 - 100, 170, 0xFFFFFF);
        guiGraphics.drawString(this.font, stamina, this.width / 2 - 100, 190, 0xFFFFFF);
        guiGraphics.drawString(this.font, hunger, this.width / 2 - 100, 210, 0xFFFFFF);
        guiGraphics.drawString(this.font, experience, this.width / 2 - 100, 230, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game when the screen is open
    }
}