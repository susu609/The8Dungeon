package net.ss.sudungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.SsModVariables;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.glfw.GLFW.*;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class DungeonMapGui extends Screen {

    // Constants for dungeon map display
    private static final ResourceLocation MAP_ICONS = new ResourceLocation(SsMod.MODID, "textures/map/dungeon_map_icons.png");
    private static final int ROOM_SIZE = 16;
    private static final float MAX_SCALE = 2.0f;
    private static final float MIN_SCALE = 0.5f;
    private static final int MOVE_AMOUNT = 20;

    // Map and player data
    private final DungeonSavedData dungeonSavedData;
    private final Player player;

    // Scaling and translation parameters
    private float scale = 1.0f;
    private float targetScale = 1.0f;
    private int translateX = 0;
    private int translateY = 0;

    // Dungeon dimensions
    private int dungeonWidth;
    private int dungeonHeight;
    private int minX;
    private int minY;

    public DungeonMapGui (DungeonSavedData dungeonSavedData, Player player) {
        super(Component.literal("Dungeon Map"));
        this.dungeonSavedData = dungeonSavedData;
        this.player = player;
        initializeMap();
    }

    // Initialize the dungeon map by calculating its dimensions and centering on screen
    private synchronized void initializeMap () {
        calculateDungeonDimensions();
        centerMapOnScreen();
    }

    // Calculate the overall dungeon dimensions based on room positions
    private synchronized void calculateDungeonDimensions () {
        minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (RoomData room : dungeonSavedData.getRooms()) {
            BlockPos pos = room.getPosition();
            int roomX = pos.getX() / ROOM_SIZE;
            int roomZ = pos.getZ() / ROOM_SIZE;

            minX = Math.min(minX, roomX);
            maxX = Math.max(maxX, roomX);
            minY = Math.min(minY, roomZ);
            maxY = Math.max(maxY, roomZ);
        }

        dungeonWidth = (maxX - minX + 1) * ROOM_SIZE;
        dungeonHeight = (maxY - minY + 1) * ROOM_SIZE;
    }

    // Center the dungeon map on the screen based on its dimensions and scaling factor
    private synchronized void centerMapOnScreen () {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        translateX = (screenWidth - Math.round(dungeonWidth * scale)) / 2;
        translateY = (screenHeight - Math.round(dungeonHeight * scale)) / 2;
    }

    // Calculate the x-coordinate of a room in the map based on its position and scaling
    private int calculateMapX (BlockPos pos) {
        int scaledX = pos.getX() / ROOM_SIZE;
        return Math.round((scaledX - minX) * ROOM_SIZE * scale) + translateX;
    }

    // Calculate the y-coordinate of a room in the map based on its position and scaling
    private int calculateMapY (BlockPos pos) {
        int scaledZ = pos.getZ() / ROOM_SIZE;
        return Math.round((scaledZ - minY) * ROOM_SIZE * scale) + translateY;
    }

    // Ensure scaling is valid by rounding to the nearest room size multiple
    private float calculateValidScale (float proposedScale) {
        int roomSizeScaled = Math.round(ROOM_SIZE * proposedScale);
        return roomSizeScaled / (float) ROOM_SIZE;
    }

    // Check if the player is inside any room and update the discovery status
    private synchronized void checkAndUpdateRoomStatus () {
        BlockPos playerPos = player.blockPosition();
        for (RoomData room : dungeonSavedData.getRooms()) {
            if (room.isInsideRoom(playerPos) && !room.isDiscovered()) {
                room.setDiscovered(true);
                dungeonSavedData.setDirty(true);  // Đảm bảo dữ liệu được lưu lại
                SsMod.LOGGER.info("Room at position ({}, {}, {}) has been discovered.",
                        room.getPosition().getX(), room.getPosition().getY(), room.getPosition().getZ());
                break;  // Chỉ cần đánh dấu phòng mà người chơi đang ở
            }
        }
    }

    // Handle zooming via mouse scrolling
    @Override
    public boolean mouseScrolled (double mouseX, double mouseY, double delta) {
        handleZoom(delta);
        return true;
    }

    // Handle player input via key presses (zoom and movement)
    @Override
    public boolean keyPressed (int keyCode, int scanCode, int modifiers) {
        int moveAmount = (int) (MOVE_AMOUNT * scale);

        switch (keyCode) {
            case GLFW_KEY_KP_ADD, GLFW_KEY_EQUAL -> {
                zoomIn();
                return true;
            }
            case GLFW_KEY_KP_SUBTRACT, GLFW_KEY_MINUS -> {
                zoomOut();
                return true;
            }
            case GLFW_KEY_ESCAPE, GLFW_KEY_M -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.closeContainer();
                }
                return true;
            }
            case GLFW_KEY_LEFT -> {
                translateX += moveAmount;
                return true;
            }
            case GLFW_KEY_RIGHT -> {
                translateX -= moveAmount;
                return true;
            }
            case GLFW_KEY_UP -> {
                translateY += moveAmount;
                return true;
            }
            case GLFW_KEY_DOWN -> {
                translateY -= moveAmount;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Allow player to drag the map by holding down the mouse button
    @Override
    public synchronized boolean mouseDragged (double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            translateX += (int) dragX;
            translateY += (int) dragY;
            return true;
        }
        return false;
    }

    // Main rendering loop for the GUI, including the dungeon map and player face
    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderDirtBackground(guiGraphics);

        checkAndUpdateRoomStatus();
        renderDungeonMap(guiGraphics);
        renderPlayerFace(guiGraphics);
    }

    // Render the dungeon map, including discovered and unexplored rooms
    private synchronized void renderDungeonMap (GuiGraphics guiGraphics) {
        guiGraphics.fill(translateX, translateY,
                (int) (translateX + dungeonWidth * scale),
                (int) (translateY + dungeonHeight * scale), 0xFF000000);

        for (RoomData room : dungeonSavedData.getRooms()) {
            BlockPos pos = room.getPosition();
            int roomX = calculateMapX(pos);
            int roomY = calculateMapY(pos);

            // Kiểm tra trạng thái isDiscovered trước khi render
            if (room.isDiscovered()) {
                renderDiscoveredRoom(guiGraphics, roomX, roomY);
            } else {
                renderUnexploredRoom(guiGraphics, roomX, roomY);
            }

            // Vẽ icon cho từng phòng
            renderRoomIcon(guiGraphics, room, roomX, roomY);
        }
    }

    // Render the player's face on the map
    private void renderPlayerFace (GuiGraphics guiGraphics) {
        // Lấy vị trí người chơi
        BlockPos playerPos = player.blockPosition();

        // Tính toán kích thước của khuôn mặt người chơi dựa trên scale
        int faceSize = Math.round(((float) ROOM_SIZE / 2) * scale);

        // Tinh chỉnh giá trị `centeredX` để khắc phục lỗi lệch sang trái
        int centeredX = calculateMapX(playerPos) - (faceSize / 2) + Math.round(ROOM_SIZE * scale) / 2;
        int centeredY = calculateMapY(playerPos) - (faceSize / 2) + Math.round(ROOM_SIZE * scale) / 2 - Math.round(16 * scale);

        // Vẽ khung viền cho khuôn mặt
        guiGraphics.fill(centeredX - 1, centeredY - 1, centeredX + faceSize + 1, centeredY + faceSize + 1, 0xBFFFFFFF);

        // Lấy đường dẫn skin người chơi từ biến WorldVariables
        player.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(playerVars -> {
            // Sử dụng playerVars như trước
            ResourceLocation playerSkin = new ResourceLocation(playerVars.skinUrl);

            // Vẽ khuôn mặt người chơi trên bản đồ
            guiGraphics.blit(playerSkin, centeredX, centeredY, faceSize, faceSize, 8, 8, 8, 8, 64, 64);
        });
    }


    // Render different room icons based on their type
// Render different room icons based on their type
    private void renderRoomIcon (GuiGraphics guiGraphics, RoomData room, int roomX, int roomY) {
        int iconSize = Math.round(ROOM_SIZE * scale);  // Điều chỉnh kích thước icon theo scale
        int iconU = 100, iconV = 100;

        // Xác định vị trí và loại icon dựa vào loại phòng
        switch (room.getType()) {
            case SHOP -> {
                iconU = 0;
                iconV = 16;
            }
            case TREASURE -> {
                iconU = 16;
                iconV = 16;
            }
            case NORMAL -> {
                if (room.hasMonsters()) {
                    iconV = 32;
                }
            }
            case ELITE -> {
                iconU = 16;
                iconV = 32;
            }
            case BOSS -> {
                iconU = 32;
                iconV = 32;
            }
            case EVENT -> {
                iconU = 48;
                iconV = 32;
            }
        }

        // Sử dụng phương thức `blit` để vẽ icon với vị trí và kích thước đã tính toán
        guiGraphics.blit(
                MAP_ICONS,                // Texture của bản đồ (icons)
                roomX,                    // Vị trí x của phòng trên bản đồ
                roomY,                    // Vị trí y của phòng trên bản đồ
                iconSize,                 // Chiều rộng icon (tính theo scale)
                iconSize,                 // Chiều cao icon (tính theo scale)
                iconU,                    // Tọa độ x trong texture sheet (phần icon cần render)
                iconV,                    // Tọa độ y trong texture sheet (phần icon cần render)
                16,                       // Chiều rộng của icon trong texture sheet
                16,                       // Chiều cao của icon trong texture sheet
                256,                      // Chiều rộng tổng của texture sheet
                256                       // Chiều cao tổng của texture sheet
        );
    }


    private synchronized void renderDiscoveredRoom (GuiGraphics guiGraphics, int mapX, int mapY) {
        int iconSize = Math.round(ROOM_SIZE * scale);  // Điều chỉnh kích thước icon theo scale
        guiGraphics.blit(MAP_ICONS, mapX, mapY, iconSize, iconSize, 0, 0, 16, 16, 256, 256);
    }

    private synchronized void renderUnexploredRoom (GuiGraphics guiGraphics, int mapX, int mapY) {
        int iconSize = Math.round(ROOM_SIZE * scale);  // Điều chỉnh kích thước icon theo scale
        guiGraphics.blit(MAP_ICONS, mapX, mapY, iconSize, iconSize, 16, 0, 16, 16, 256, 256);
    }

    // Handle zooming in and out
    private void handleZoom (double delta) {
        if (delta > 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    }

    // Zoom in by increasing the scale, but don't exceed the maximum
    private void zoomIn () {
        if (targetScale < MAX_SCALE) {
            targetScale = Math.min(targetScale * 1.1f, MAX_SCALE);
            scale = calculateValidScale(targetScale);
        }
    }

    // Zoom out by decreasing the scale, but don't go below the minimum
    private void zoomOut () {
        if (targetScale > MIN_SCALE) {
            targetScale = Math.max(targetScale / 1.1f, MIN_SCALE);
            scale = calculateValidScale(targetScale);
        }
    }
}
