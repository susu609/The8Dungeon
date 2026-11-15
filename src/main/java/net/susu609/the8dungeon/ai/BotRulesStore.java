package net.susu609.the8dungeon.ai;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Luật của bot, lưu theo thế giới. Mỗi bot hoặc dùng luật chung, hoặc có bộ luật riêng. */
public final class BotRulesStore extends SavedData {

    private static final String NAME = "susu609_bot_rules";

    /** Luật riêng của một bot; inherit=true nghĩa là đang dùng luật chung. */
    public static final class Entry {
        public final boolean inherit;
        public final BotRules rules;

        Entry(boolean inherit, BotRules rules) {
            this.inherit = inherit;
            this.rules = rules;
        }
    }

    private BotRules global = new BotRules();
    private final Map<String, Entry> bots = new HashMap<>();

    public static BotRulesStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BotRulesStore::load, BotRulesStore::new, NAME);
    }

    public BotRules global() {
        return global;
    }

    public Entry entry(String bot) {
        return bots.get(bot);
    }

    /** Luật đang có hiệu lực cho bot này. */
    public BotRules effective(String bot) {
        Entry e = bots.get(bot);
        return e != null && !e.inherit ? e.rules : global;
    }

    public void setGlobal(BotRules rules) {
        global = rules;
        setDirty();
    }

    public void setBot(String bot, boolean inherit, BotRules rules) {
        bots.put(bot, new Entry(inherit, rules));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("global", global.save());
        CompoundTag list = new CompoundTag();
        bots.forEach((name, e) -> {
            CompoundTag t = e.rules.save();
            t.putBoolean("inherit", e.inherit);
            list.put(name, t);
        });
        tag.put("bots", list);
        return tag;
    }

    private static BotRulesStore load(CompoundTag tag) {
        BotRulesStore s = new BotRulesStore();
        if (tag.contains("global")) s.global = BotRules.load(tag.getCompound("global"));
        CompoundTag list = tag.getCompound("bots");
        for (String name : list.getAllKeys()) {
            CompoundTag t = list.getCompound(name);
            s.bots.put(name, new Entry(t.getBoolean("inherit"), BotRules.load(t)));
        }
        return s;
    }
}
