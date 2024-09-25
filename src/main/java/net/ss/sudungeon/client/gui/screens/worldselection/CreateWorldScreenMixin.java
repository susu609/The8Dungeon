/*
package net.ss.sudungeon.client.gui.screens.worldselection;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.ss.sudungeon.SsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Unique
    protected abstract <T extends Screen> void ss$addRenderableWidget (Button widget);

    @Invoker("onCreate")
    protected abstract void invokeOnCreate ();

    @Inject(method = "init", at = @At("TAIL"))
    private void init (CallbackInfo ci) {
        CreateWorldScreen createWorldScreen = (CreateWorldScreen) (Object) this;

        // Log for debugging
        System.out.println("Attempting to add custom button...");

        // Basic dimensions and position
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 4;
        int centerX = createWorldScreen.width / 2;
        int startY = createWorldScreen.height / 4 + 48;

        // Adding "Create Custom World" button
        Button customButton = Button.builder(
                        Component.literal("Tạo Thế Giới Tùy Chỉnh"),
                        button -> {
                            try {
                                System.out.println("Custom world button clicked.");
                                ss$createCustomWorld(createWorldScreen);
                            } catch (Exception e) {
                                SsMod.LOGGER.error("Failed to create custom world", e);
                            }
                        })
                .bounds(centerX - buttonWidth / 2, startY + (buttonHeight + spacing), buttonWidth, buttonHeight)
                .build();

        // Add the button to the screen
        this.ss$addRenderableWidget(customButton);

        // Log button creation
        System.out.println("Custom button created: " + customButton);

        // Log button addition
        System.out.println("Custom button added to the screen.");
    }

    // Custom world creation method
    @Unique
    private void ss$createCustomWorld (CreateWorldScreen createWorldScreen) {
        WorldCreationUiState uiState = createWorldScreen.getUiState();

        WorldOptions customWorldOptions = new WorldOptions(609, false, false);
        WorldDimensions customDimensions = ss$createCustomWorldDimensions(uiState);

        WorldGenSettings customSettings = new WorldGenSettings(customWorldOptions, customDimensions);

        GameRules customGameRules = ss$createCustomGameRules();

        WorldCreationContext customContext = new WorldCreationContext(
                customSettings,
                uiState.getSettings().worldgenRegistries(),
                uiState.getSettings().dataPackResources(),
                uiState.getSettings().dataConfiguration()
        );

        uiState.setSettings(customContext);
        uiState.getGameRules().assignFrom(customGameRules, null);

        this.invokeOnCreate();

        SsMod.LOGGER.info("Custom world creation settings applied.");
    }

    @Unique
    private WorldDimensions ss$createCustomWorldDimensions (WorldCreationUiState uiState) {
        return uiState.getSettings().selectedDimensions();
    }

    @Unique
    private GameRules ss$createCustomGameRules () {
        GameRules gameRules = new GameRules();
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(false, null);
        gameRules.getRule(GameRules.RULE_DOMOBLOOT).set(false, null);
        gameRules.getRule(GameRules.RULE_DOENTITYDROPS).set(true, null);
        return gameRules;
    }
}
*/
