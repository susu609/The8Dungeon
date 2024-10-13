package net.ss.sudungeon.world.entity.player;

public class CharacterStats {
    private double health;
    private double mana;
    private double stamina;
    private double defense;
    private double strength;  // Công thức tính toán strength tùy theo level
    private double level;

    public CharacterStats (double health, double mana, double stamina, double defense, double strength, double level) {
        this.health = health;
        this.mana = mana;
        this.stamina = stamina;
        this.defense = defense;
        this.strength = strength;
        this.level = level;
    }

    // Getter và setter
    public float getHealth () {
        return (float) health;
    }
    public void setHealth(int health) { this.health = health; }

    public double getMana () {
        return mana;
    }
    public void setMana(int mana) { this.mana = mana; }

    public double getStamina () {
        return stamina;
    }
    public void setStamina(int stamina) { this.stamina = stamina; }

    public double getDefense () {
        return defense;
    }
    public void setDefense(int defense) { this.defense = defense; }

    public double getLevel () {
        return level;
    }
    public void setLevel(int level) { this.level = level; }

    public double getStrength () {
        return strength;
    }
    public void setStrength(int strength) { this.strength = strength; }

}
