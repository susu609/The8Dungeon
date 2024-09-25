/*
package net.ss.sudungeon.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class WorldManagementScreen extends Screen {
    private static final int NUM_SAVES = 3;
    private static final String SAVES_DIRECTORY = Minecraft.getInstance().gameDirectory + "/saves";
    protected final Screen lastScreen;

    public WorldManagementScreen (Screen lastScreen) {
        super(Component.literal("Manage Your Worlds"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init () {
        super.init();
        int centerX = this.width / 2;

        // Tạo nút Save Slot
        for (int i = 0; i < NUM_SAVES; i++) {
            final int index = i;
            String worldName = "World" + (index + 1);
            boolean exists = worldExists(worldName);

            // Đặt màu nền dựa trên sự tồn tại của thế giới
            int backgroundColor = exists ? 0xFF00FF00 : 0xFF0000FF; // Màu xanh lá nếu tồn tại, màu xanh lam nếu không

            this.addRenderableWidget(Button.builder(
                    Component.literal("Save Slot " + (i + 1)),
                    button -> loadWorld(index)
            ).bounds(centerX - 100, 60 + i * 30, 200, 20).build());
        }

        // Nút "Tạo Thế Giới Mới"
        this.addRenderableWidget(Button.builder(
                Component.literal("Create New World"),
                button -> createNewWorld()
        ).bounds(centerX - 100, this.height - 40, 200, 20).build());

        // Nút "Quay lại"
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_CANCEL,
                button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(this.lastScreen);
                }
        ).bounds(centerX - 100, this.height - 70, 200, 20).build());
    }

    private void loadWorld (int index) {
        String worldName = "World" + (index + 1);
        if (worldExists(worldName)) {
            WorldOpenFlows worldOpenFlows = new WorldOpenFlows(Minecraft.getInstance(), Minecraft.getInstance().getLevelSource());
            worldOpenFlows.loadLevel(this, worldName);
        } else {
            System.out.println("World does not exist, attempting to copy default world.");
            Path targetPath = Paths.get(getSavesDirectory().getPath(), worldName);
            try {
                copyDefaultWorldTo(targetPath);
                WorldOpenFlows worldOpenFlows = new WorldOpenFlows(Minecraft.getInstance(), Minecraft.getInstance().getLevelSource());
                worldOpenFlows.loadLevel(this, worldName);
            } catch (IOException e) {
                Logger.getLogger(WorldManagementScreen.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void createNewWorld () {
        String newWorldName = "World_" + UUID.randomUUID();
        Path targetPath = Paths.get(getSavesDirectory().getPath(), newWorldName);

        try {
            copyDefaultWorldTo(targetPath);
            WorldOpenFlows worldOpenFlows = new WorldOpenFlows(Minecraft.getInstance(), Minecraft.getInstance().getLevelSource());
            worldOpenFlows.loadLevel(this, newWorldName);
        } catch (IOException e) {
            Logger.getLogger(WorldManagementScreen.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void copyDefaultWorldTo(Path target) throws IOException {
        URL resource = getClass().getClassLoader().getResource("default_world/Save Game");
        if (resource == null) {
            throw new IOException("Resource not found: default_world/Save Game");
        }

        try (Stream<Path> stream = Files.walk(Paths.get(resource.toURI()))) {
            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }
            stream.forEach(source -> {
                // Sử dụng Paths.get từ một chuỗi thay vì URL để tránh lỗi ProviderMismatchException
                Path destination = target.resolve(source.toString().substring(source.toString().indexOf("default_world")));
                try {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Logger.getLogger(WorldManagementScreen.class.getName()).log(Level.SEVERE, null, e);
                }
            });
        } catch (URISyntaxException e) {
            Logger.getLogger(WorldManagementScreen.class.getName()).log(Level.SEVERE, "Invalid URI", e);
        }
    }


    private File getSavesDirectory () {
        return new File(Minecraft.getInstance().gameDirectory, "saves");
    }

    private boolean worldExists (String worldName) {
        Path worldPath = Paths.get(SAVES_DIRECTORY, worldName);
        return Files.exists(worldPath);
    }

    @Override
    public void onClose () {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render (@NotNull GuiGraphics guigraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guigraphics);
        super.render(guigraphics, mouseX, mouseY, delta);
        guigraphics.drawCenteredString(this.font, "Select a World to Load", this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public boolean shouldCloseOnEsc () {
        return false;
    }
}
*/
