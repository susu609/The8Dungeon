package net.ss.sudungeon.world.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.network.SsModVariables;

public class PlayerCharacterStats {

    public static void executeexample(Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player || entity instanceof ServerPlayer) {
            LivingEntity _livingEntity = (LivingEntity) entity;
            resetAttributes(_livingEntity);
        }
    }

    private static void resetAttributes(LivingEntity entity) {
        setAttribute(entity, SsModAttributes.CRITICAL_HIT_CHANCE.get(), 0);
        setAttribute(entity, SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get(), 0);
        setAttribute(entity, SsModAttributes.MAX_MANA.get(), 0);
        setAttribute(entity, SsModAttributes.MAX_STAMINA.get(), 0);
        setAttribute(entity, Attributes.ARMOR, 0);
        setAttribute(entity, Attributes.ATTACK_DAMAGE, 0);
        setAttribute(entity, Attributes.MAX_HEALTH, 0);
        setAttribute(entity, Attributes.MOVEMENT_SPEED, 0);
    }

    public static void setCharacterAttributes(Player player, String character) {
        if (player == null) return;

        switch (character.toLowerCase()) {
            case "alex" -> {
                giveCharacterItems(player, "alex");
                setPlayerAttribute(player, Attributes.ARMOR, 0.0);
                setPlayerAttribute(player, Attributes.ATTACK_DAMAGE, 1.0);
                setPlayerAttribute(player, Attributes.MAX_HEALTH, 18.0);
                setPlayerAttribute(player, Attributes.MOVEMENT_SPEED, 0.15);
                setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_CHANCE.get(), 0.05);
                setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get(), 2.0);
                setPlayerAttribute(player, SsModAttributes.MAX_MANA.get(), 20.0);
                setPlayerAttribute(player, SsModAttributes.MAX_STAMINA.get(), 25.0);
            }
            case "steve" -> {
                giveCharacterItems(player, "steve");
                setPlayerAttribute(player, Attributes.ARMOR, 2.0);
                setPlayerAttribute(player, Attributes.ATTACK_DAMAGE, 1.5);
                setPlayerAttribute(player, Attributes.MAX_HEALTH, 20.0);
                setPlayerAttribute(player, Attributes.MOVEMENT_SPEED, 0.1);
                setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_CHANCE.get(), 0.01);
                setPlayerAttribute(player, SsModAttributes.CRITICAL_HIT_DAMAGE_MULTIPLE.get(), 1.5);
                setPlayerAttribute(player, SsModAttributes.MAX_MANA.get(), 20.0);
                setPlayerAttribute(player, SsModAttributes.MAX_STAMINA.get(), 20.0);
            }
        }
    }

    private static void setPlayerAttribute(Player player, Attribute attribute, double value) {
        if (player.getAttributes().hasAttribute(attribute)) {
            player.getAttribute(attribute).setBaseValue(value);
        }
    }

    public static void setCharacterSkinAndModel(Player player, String character) {
        player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(playerVars -> {
            switch (character.toLowerCase()) {
                case "alex" -> {
                    playerVars.skinUrl = "ss:textures/entity/player/slim/alex.png";
                    playerVars.isSlim = true;
                }
                case "steve" -> {
                    playerVars.skinUrl = "ss:textures/entity/player/wide/steve.png";
                    playerVars.isSlim = false;
                }
            }
            SsMod.LOGGER.info(playerVars.skinUrl + " and " + playerVars.isSlim);
            playerVars.syncPlayerVariables(player);
        });
    }

    public static void giveCharacterItems(Player player, String character) {
        switch (character.toLowerCase()) {
            case "alex" -> {
                player.addItem(new ItemStack(Items.BOW));
                player.addItem(new ItemStack(Items.ARROW, 16));
            }
            case "steve" -> {
                player.addItem(new ItemStack(Items.WOODEN_SWORD));
            }
        }
    }

    private static void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        if (entity.getAttributes().hasAttribute(attribute)) {
            entity.getAttribute(attribute).setBaseValue(value);
        }
    }
}
