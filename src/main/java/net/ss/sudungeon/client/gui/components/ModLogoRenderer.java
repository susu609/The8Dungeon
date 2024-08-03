package net.ss.sudungeon.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class ModLogoRenderer extends LogoRenderer {
    public static final ResourceLocation MOD_TITLE = new ResourceLocation("ss:textures/gui/title/mod_title.png");

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
    public void renderLogo(GuiGraphics guiGraphics, int width, float partialTicks, int heightOffset) {
        // Apply fade effect based on configuration
        float opacity = this.keepLogoThroughFade ? 1.0F : partialTicks / 30.0F;
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, opacity);

        // Calculate and render your mod's logo position
        int modLogoY = heightOffset + 44; // Adjust the 44 value for desired spacing
        Minecraft.getInstance().getTextureManager().bindForSetup(MOD_TITLE);
        guiGraphics.blit(MOD_TITLE, width / 2 - 144, modLogoY, 0.0F, 0.0F, 288, 44, 288, 64); // Adjust size if needed

        // Render the vanilla Minecraft logo and Edition text using the parent class
        super.renderLogo(guiGraphics, width, partialTicks, heightOffset);

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color

    }
}
