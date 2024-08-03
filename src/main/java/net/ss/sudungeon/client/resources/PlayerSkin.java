/*
package net.ss.sudungeon.client.resources;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.ss.sudungeon.SsMod;

import net.ss.sudungeon.network.SsModVariables;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class PlayerSkin extends DefaultPlayerSkin implements ISkinProvider {
    
    public static final SkinType MY_CUSTOM_SKIN =
            new SkinType(new ResourceLocation(SsMod.MODID, "textures/entity/player/wide/steve.png"), ModelType.WIDE);


    @Override
    public ResourceLocation getSkin(GameProfile profile) {
        // Lấy skin tùy chỉnh từ WorldVariables (giả sử nó đã được đồng bộ hóa)
        SsModVariables.WorldVariables worldVariables = SsModVariables.WorldVariables.get(Minecraft.getInstance().level);
        String selectedSkinName = worldVariables.getPlayerSkin();

        // Tìm skin tương ứng trong enum PlayerSkins
        return switch (PlayerSkins.valueOf(selectedSkinName.toUpperCase())) { // Chuyển selectedSkinName thành chữ hoa để so sánh với enum
            case STEVE -> PlayerSkins.STEVE.getSkinLocation();
            case ALEX -> PlayerSkins.ALEX.getSkinLocation();
            // ... các trường hợp khác cho các skin tùy chỉnh ...
            default -> PlayerSkins.STEVE.getSkinLocation(); // Skin mặc định nếu không tìm thấy
        };
    }
}
*/
