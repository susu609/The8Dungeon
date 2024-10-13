/*
package net.ss.sudungeon.world.entity;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "ss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerAttributeHandler {

    // Listen for when the player's XP level changes
    @SubscribeEvent
    public static void onPlayerLevelUp (PlayerXpEvent.LevelChange event) {
        Player player = event.getEntity();

        // Call method to scale attributes based on new level
        scalePlayerAttributes(player);
    }

    // Method to scale player attributes based on their level
    private static void scalePlayerAttributes (Player player) {
        int playerLevel = player.experienceLevel;

        // Adjust Health based on level
        double healthBonus = playerLevel * 0.25; // Add 0.5 health per level
        applyAttributeModifier(player, Attributes.MAX_HEALTH, player.getUUID(), "Health Modifier", healthBonus);

        // Adjust Damage based on level
        double damageBonus = playerLevel * 0.1; // Add 0.2 damage per level
        applyAttributeModifier(player, Attributes.ATTACK_DAMAGE, player.getUUID(), "Damage Modifier", damageBonus);

        // Ensure the player's health is within bounds after updating max health
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    // Utility method to apply an attribute modifier to the player
    private static void applyAttributeModifier (Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID playerUUID, String modifierName, double bonusAmount) {
        var attributeInstance = player.getAttribute(attribute);

        if (attributeInstance != null) {
            // Remove existing modifier with the same UUID if it exists
            AttributeModifier existingModifier = attributeInstance.getModifier(playerUUID);
            if (existingModifier != null) {
                attributeInstance.removeModifier(playerUUID);
            }

            // Add the new or updated modifier
            attributeInstance.addPermanentModifier(
                    new AttributeModifier(playerUUID, modifierName, bonusAmount, AttributeModifier.Operation.ADDITION));
        }
    }
}
*/
