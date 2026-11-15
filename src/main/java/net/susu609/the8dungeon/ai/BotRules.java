package net.susu609.the8dungeon.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Bộ luật hành vi của bot. Một bộ chung cho tất cả, mỗi bot có thể có bộ riêng. */
public final class BotRules {

    // Được phép làm gì
    public boolean fight = true;
    public boolean hunt = true;
    public boolean mine = true;       // chặt gỗ, đào đá/đất
    public boolean shelter = true;
    public boolean explore = true;
    public boolean collect = true;
    /** Tiết kiệm tài nguyên: đứng yên khi không có người chơi thật trên server. */
    public boolean pauseWhenAlone = false;

    // Ngưỡng
    public int fleeHpPct = 40;        // chạy trốn khi máu dưới X%
    public int eatBelow = 14;         // ăn khi thanh đói <= X
    public int roam = 0;              // bán kính tối đa quanh nhà, 0 = không giới hạn

    // Hiệu năng
    public int thinkEvery = 10;       // suy nghĩ mỗi X tick
    public int scanBudget = 700;      // số khối quét mỗi lần suy nghĩ

    public BotRules copy() {
        BotRules r = new BotRules();
        r.fight = fight;
        r.hunt = hunt;
        r.mine = mine;
        r.shelter = shelter;
        r.explore = explore;
        r.collect = collect;
        r.pauseWhenAlone = pauseWhenAlone;
        r.fleeHpPct = fleeHpPct;
        r.eatBelow = eatBelow;
        r.roam = roam;
        r.thinkEvery = thinkEvery;
        r.scanBudget = scanBudget;
        return r;
    }

    /** Ép về khoảng hợp lệ. Server luôn gọi sau khi nhận dữ liệu từ client. */
    public void clamp() {
        fleeHpPct = Math.max(10, Math.min(90, fleeHpPct));
        eatBelow = Math.max(4, Math.min(19, eatBelow));
        roam = roam <= 0 ? 0 : Math.max(32, Math.min(512, roam));
        thinkEvery = Math.max(5, Math.min(40, thinkEvery));
        scanBudget = Math.max(200, Math.min(1500, scanBudget));
    }

    public void write(FriendlyByteBuf b) {
        b.writeBoolean(fight);
        b.writeBoolean(hunt);
        b.writeBoolean(mine);
        b.writeBoolean(shelter);
        b.writeBoolean(explore);
        b.writeBoolean(collect);
        b.writeBoolean(pauseWhenAlone);
        b.writeVarInt(fleeHpPct);
        b.writeVarInt(eatBelow);
        b.writeVarInt(roam);
        b.writeVarInt(thinkEvery);
        b.writeVarInt(scanBudget);
    }

    public static BotRules read(FriendlyByteBuf b) {
        BotRules r = new BotRules();
        r.fight = b.readBoolean();
        r.hunt = b.readBoolean();
        r.mine = b.readBoolean();
        r.shelter = b.readBoolean();
        r.explore = b.readBoolean();
        r.collect = b.readBoolean();
        r.pauseWhenAlone = b.readBoolean();
        r.fleeHpPct = b.readVarInt();
        r.eatBelow = b.readVarInt();
        r.roam = b.readVarInt();
        r.thinkEvery = b.readVarInt();
        r.scanBudget = b.readVarInt();
        return r;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("fight", fight);
        t.putBoolean("hunt", hunt);
        t.putBoolean("mine", mine);
        t.putBoolean("shelter", shelter);
        t.putBoolean("explore", explore);
        t.putBoolean("collect", collect);
        t.putBoolean("pause", pauseWhenAlone);
        t.putInt("flee", fleeHpPct);
        t.putInt("eat", eatBelow);
        t.putInt("roam", roam);
        t.putInt("think", thinkEvery);
        t.putInt("budget", scanBudget);
        return t;
    }

    public static BotRules load(CompoundTag t) {
        BotRules r = new BotRules();
        if (t.contains("fight")) r.fight = t.getBoolean("fight");
        if (t.contains("hunt")) r.hunt = t.getBoolean("hunt");
        if (t.contains("mine")) r.mine = t.getBoolean("mine");
        if (t.contains("shelter")) r.shelter = t.getBoolean("shelter");
        if (t.contains("explore")) r.explore = t.getBoolean("explore");
        if (t.contains("collect")) r.collect = t.getBoolean("collect");
        r.pauseWhenAlone = t.getBoolean("pause");
        if (t.contains("flee")) r.fleeHpPct = t.getInt("flee");
        if (t.contains("eat")) r.eatBelow = t.getInt("eat");
        r.roam = t.getInt("roam");
        if (t.contains("think")) r.thinkEvery = t.getInt("think");
        if (t.contains("budget")) r.scanBudget = t.getInt("budget");
        r.clamp();
        return r;
    }
}
