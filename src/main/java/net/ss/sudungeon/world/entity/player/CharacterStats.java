package net.ss.sudungeon.world.entity.player;

public class CharacterStats {
    private int health;
    private int mana;
    private int stamina;
    private int defense;
    private int level;

    public CharacterStats(int health, int mana, int stamina, int defense, int level) {
        this.health = health;
        this.mana = mana;
        this.stamina = stamina;
        this.defense = defense;
        this.level = level;
    }

    // Getter và setter
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }

    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
