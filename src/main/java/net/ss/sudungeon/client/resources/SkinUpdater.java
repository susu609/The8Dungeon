package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.network.packet.ServerboundChangeSkinPacket;

public class SkinUpdater {
    public static void updateSkin () {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            String playerUUID = minecraft.player.getGameProfile().getId().toString();
            String skinName = SkinManager.getSkinChoice(playerUUID);

            // Cập nhật skin cho người chơi
            GameProfile profile = Minecraft.getInstance().getUser().getGameProfile();
            ResourceLocation skinLocation = PlayerSkinProvider.getSkin(profile, skinName);

            // Cập nhật skin có thể phải thực hiện qua server hoặc không có phương thức trực tiếp
            // Giả sử bạn có một phương thức để áp dụng skin cho người chơi qua server
            if (minecraft.getConnection() != null) {
                // Gửi yêu cầu đến server để thay đổi skin
                minecraft.getConnection().send(new ServerboundChangeSkinPacket(skinLocation));
            }
        }
    }
}
