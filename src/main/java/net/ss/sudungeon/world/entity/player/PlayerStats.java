package net.ss.sudungeon.world.entity.player;

public class PlayerStats {
    private int health;
    private int maxHealth;
    private int mana;
    private int maxMana;
    private int stamina;
    private int maxStamina;
    private int defense;
    private int strength; // Biến mới để lưu sức mạnh của người chơi
    private int level;

    // Các phương thức getter cho maxMana, maxStamina, maxHealth
    public int getMaxMana() {
        return maxMana;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    // Constructor, thêm biến strength vào
    public PlayerStats(int health, int mana, int stamina, int defense, int strength, int level) {
        this.health = health;
        this.mana = mana;
        this.stamina = stamina;
        this.defense = defense;
        this.strength = strength; // Khởi tạo sức mạnh
        this.level = level;
    }

    public void levelUp() {
        level++;
        // Tăng chỉ số khi nhân vật lên cấp
        this.health += 2;     // Tăng 2 máu mỗi cấp
        this.mana += 1;       // Tăng 1 mana mỗi cấp
        this.stamina += 2;    // Tăng 2 stamina mỗi cấp
        this.defense += 1;    // Tăng 1 phòng thủ mỗi cấp
        this.strength += 1;   // Tăng 1 sức mạnh mỗi cấp
    }

    // Getter và setter
    public int getHealth() { return health; }
    public int getMana() { return mana; }
    public int getStamina() { return stamina; }
    public int getDefense() { return defense; }
    public int getStrength() { return strength; } // Getter cho strength
    public int getLevel() { return level; }

    public void setHealth(int health) { this.health = health; }
    public void setMana(int mana) { this.mana = mana; }
    public void setStamina(int stamina) { this.stamina = stamina; }
    public void setDefense(int defense) { this.defense = defense; }
    public void setStrength(int strength) { this.strength = strength; } // Setter cho strength
    public void setLevel(int level) { this.level = level; }

}
