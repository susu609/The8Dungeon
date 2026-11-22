package net.susu609.the8dungeon.character;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CharacterApply {
    public static void apply(ServerPlayer player, String classId) {
        if (player == null) return;
        CharacterClass cls = CharacterClass.byId(classId);
        if (cls == null) return;                                   // id lạ → bỏ qua

        CompoundTag data = persisted(player);
        if (data.contains("class")) return;                        // đã có nghề → không cho đổi

        data.putString("class", cls.id);
        giveKit(player, cls);
    }

    private static void giveKit(ServerPlayer player, CharacterClass cls) {
        switch (cls) {
            case ROGUE -> {
                ItemStack sword = new ItemStack(Items.IRON_SWORD);
                sword.setDamageValue(sword.getMaxDamage() / 2);    // nerf: chỉ còn 50% độ bền
                player.getInventory().add(sword);
            }
        }
    }

    // Dữ liệu được Forge CHÉP SANG khi chết và hồi sinh
    static CompoundTag persisted(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
}