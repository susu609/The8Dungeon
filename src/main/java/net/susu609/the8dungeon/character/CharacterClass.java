package net.susu609.the8dungeon.character;

import org.jetbrains.annotations.Nullable;

public enum CharacterClass {
    ROGUE("rogue");

    public final String id;

    CharacterClass(String id) {
        this.id = id;
    }

    public static @Nullable CharacterClass byId(String id) {
        for (CharacterClass c : values()) if (c.id.equals(id)) return c;
        return null;                              // id lạ (client sửa gói) -> null
    }
}