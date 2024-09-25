package net.ss.sudungeon.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.ss.sudungeon.IgnoreCanceled;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class PlayerSkinRenderer extends PlayerRenderer implements IgnoreCanceled {

    private final ResourceLocation PLAYER_SKIN;

    // Constructor nhận vào context và skin tùy chỉnh
    public PlayerSkinRenderer (EntityRendererProvider.Context context, boolean useSmallArms, ResourceLocation skin) {
        super(context, useSmallArms);
        this.PLAYER_SKIN = skin; // Đặt skin tùy chỉnh
    }

    // Xóa các lớp render không cần thiết
    public void clearLayers () {
        this.layers.clear();
    }

    // Lấy đường dẫn texture cho người chơi từ biến mạng
    public @NotNull ResourceLocation getTextureLocation (@NotNull AbstractClientPlayer player) {
        return PLAYER_SKIN;
    }

    // Tùy chỉnh kiểu render dựa trên RenderType
    @Nullable
    protected RenderType getRenderType (@NotNull AbstractClientPlayer player, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        ResourceLocation resourcelocation = this.getTextureLocation(player);
        return RenderType.entityTranslucentCull(resourcelocation); // Sử dụng kiểu render translucent
    }

    // Phương thức để ẩn hoặc hiển thị các phần của mô hình
    public static void hidePlayerModelPiece (PlayerModel<AbstractClientPlayer> model, int piece) {
        switch (piece) {
            case 0 -> model.hat.visible = false;
            case 1 -> model.jacket.visible = false;
            case 2 -> model.leftPants.visible = false;
            case 3 -> model.rightPants.visible = false;
            case 4 -> model.leftSleeve.visible = false;
            case 5 -> model.rightSleeve.visible = false;
            case 6 -> model.head.visible = false;
            case 7 -> model.body.visible = false;
            case 8 -> model.leftLeg.visible = false;
            case 9 -> model.rightLeg.visible = false;
            case 10 -> model.leftArm.visible = false;
            case 11 -> model.rightArm.visible = false;
        }
    }
}
