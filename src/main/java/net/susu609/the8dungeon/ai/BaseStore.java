package net.susu609.the8dungeon.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Các base của nhóm bot (vị trí, cấp nhà, thành viên), lưu theo thế giới. */
public final class BaseStore extends SavedData {

    private static final String NAME = "susu609_bot_bases";

    static final class Base {
        int id;
        String dim;
        BlockPos origin;
        int tier = 1;
        final Set<String> members = new LinkedHashSet<>();
    }

    private final List<Base> bases = new ArrayList<>();
    private int nextId = 1;

    public static BaseStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BaseStore::load, BaseStore::new, NAME);
    }

    List<Base> all() {
        return bases;
    }

    Base baseOf(String bot) {
        for (Base b : bases) if (b.members.contains(bot)) return b;
        return null;
    }

    Base create(String dim, BlockPos origin, String founder) {
        Base b = new Base();
        b.id = nextId++;
        b.dim = dim;
        b.origin = origin.immutable();
        b.members.add(founder);
        bases.add(b);
        setDirty();
        return b;
    }

    void join(Base b, String bot) {
        for (Base o : bases) o.members.remove(bot);
        b.members.add(bot);
        setDirty();
    }

    void changed() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Base b : bases) {
            CompoundTag t = new CompoundTag();
            t.putInt("id", b.id);
            t.putString("dim", b.dim);
            t.putLong("origin", b.origin.asLong());
            t.putInt("tier", b.tier);
            ListTag m = new ListTag();
            for (String s : b.members) m.add(StringTag.valueOf(s));
            t.put("members", m);
            list.add(t);
        }
        tag.put("bases", list);
        tag.putInt("next", nextId);
        return tag;
    }

    private static BaseStore load(CompoundTag tag) {
        BaseStore s = new BaseStore();
        for (Tag x : tag.getList("bases", Tag.TAG_COMPOUND)) {
            CompoundTag t = (CompoundTag) x;
            Base b = new Base();
            b.id = t.getInt("id");
            b.dim = t.getString("dim");
            b.origin = BlockPos.of(t.getLong("origin"));
            b.tier = Math.max(1, t.getInt("tier"));
            for (Tag m : t.getList("members", Tag.TAG_STRING)) b.members.add(m.getAsString());
            s.bases.add(b);
        }
        s.nextId = Math.max(1, tag.getInt("next"));
        return s;
    }
}
