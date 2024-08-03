package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ServerSkinUpdater {
    public static void updatePlayerSkin (ServerPlayer player) {
        String playerUUID = player.getGameProfile().getId().toString();
        String skinName = SkinManager.getSkinChoice(playerUUID);

        // Cập nhật skin cho người chơi
        GameProfile profile = Minecraft.getInstance().getUser().getGameProfile();
        ResourceLocation skinLocation = PlayerSkinProvider.getSkin(profile, skinName);
/*
        // Bạn cần gửi thông tin đến client để cập nhật skin
        player.connection.send(new ClientboundSetEquipmentPacket(player.getId(), EquipmentSlot.CHEST, new ItemStack(Items.ARMOR)));
*/
    }
}
