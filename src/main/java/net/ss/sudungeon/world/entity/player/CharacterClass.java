package net.ss.sudungeon.world.entity.player;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class CharacterClass {
    public static final CharacterClass STEVE = new CharacterClass("steve", 20, 1.0f, List.of(new ItemStack(Items.WOODEN_SWORD)));
    public static final CharacterClass ALEX = new CharacterClass("alex", 16, 1.2f, List.of(new ItemStack(Items.BOW)));
    public static final CharacterClass SUNNY = new CharacterClass("sunny", 20, 0.8f, List.of(new ItemStack(Items.GOLDEN_APPLE)));
    public static final CharacterClass ZURI = new CharacterClass("zuri", 28, 0.7f, List.of(new ItemStack(Items.SHIELD)));
    public static final CharacterClass EFE = new CharacterClass("efe", 18, 1.0f, List.of(new ItemStack(Items.BLAZE_ROD)));
    public static final CharacterClass MAKENA = new CharacterClass("makena", 18, 1.4f, List.of(new ItemStack(Items.IRON_SWORD)));
    public static final CharacterClass KAI = new CharacterClass("kai", 16, 1.0f, List.of(new ItemStack(Items.FIRE_CHARGE)));
    public static final CharacterClass NOOR = new CharacterClass("noor", 20, 1.0f, List.of(new ItemStack(Items.STRING)));
    public static final CharacterClass ARI = new CharacterClass("ari", 16, 1.6f, List.of(new ItemStack(Items.FEATHER)));

    private static final Map<String, CharacterClass> REGISTRY = new HashMap<>();

    static {
        for (CharacterClass cls : List.of(STEVE, ALEX, SUNNY, ZURI, EFE, MAKENA, KAI, NOOR, ARI)) {
            REGISTRY.put(cls.id, cls);
        }
    }

    public final String id;
    public final int baseHealth;
    public final float attackSpeed;
    public final List<ItemStack> startingItems;

    public CharacterClass(String id, int baseHealth, float attackSpeed, List<ItemStack> startingItems) {
        this.id = id;
        this.baseHealth = baseHealth;
        this.attackSpeed = attackSpeed;
        this.startingItems = startingItems;
    }

    public static CharacterClass fromId(String id) {
        return REGISTRY.getOrDefault(id.toLowerCase(), STEVE);
    }
}

