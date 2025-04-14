package net.ss.sudungeon.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.util.GetTextures;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class ModLogoRenderer extends LogoRenderer {
    public static final ResourceLocation MOD_TITLE = GetTextures.screen("title/mod_title.png");

    private final boolean keepLogoThroughFade;

    public ModLogoRenderer(boolean showFadeAnimation) {
        super(showFadeAnimation);
        this.keepLogoThroughFade = showFadeAnimation;
    }

    @Override
    public void renderLogo(@NotNull GuiGraphics guiGraphics, int width, float partialTicks) {
        this.renderLogo(guiGraphics, width, partialTicks, 30); // Default height offset
    }

    @Override
    public void renderLogo(GuiGraphics guiGraphics, int width, float alpha, int heightOffset) {
        // Áp dụng giá trị alpha từ màn hình cha (RougeLikeScreen)
        float opacity = this.keepLogoThroughFade ? 1.0F : Mth.clamp(alpha, 0.0F, 1.0F);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, opacity);

        // Vẽ logo mod
        int modLogoY = heightOffset + 44; // Vị trí logo
        Minecraft.getInstance().getTextureManager().bindForSetup(MOD_TITLE);
        guiGraphics.blit(MOD_TITLE, width / 2 - 144, modLogoY, 0.0F, 0.0F, 288, 44, 288, 64); // Logo kích thước 288x44

        // Vẽ logo Minecraft gốc
        super.renderLogo(guiGraphics, width, alpha, heightOffset);

        // Reset lại màu
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}