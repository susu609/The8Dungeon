/*
package net.ss.sudungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DungeonMapOverlay {

    private static final ResourceLocation MAP_ICONS = new ResourceLocation(SsMod.MODID, "textures/map/dungeon_map_icons.png");

    private static boolean shouldDisplayMap() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.level != null &&
                minecraft.level.dimension().location().toString().contains("dungeon");
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        if (shouldDisplayMap()) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player != null) {
                // Sử dụng dữ liệu giả lập thay vì gọi DungeonSavedData.get() để kiểm tra hiển thị
                renderMapInOverlay(event.getGuiGraphics(), getDummyRoomData(), player.blockPosition());
            }
        }
    }

    private static void renderMapInOverlay(GuiGraphics guiGraphics, List<RoomData> rooms, BlockPos playerPosition) {
        float overlayScale = 0.5f;

        // Lấy kích thước màn hình
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        // Đặt bản đồ ở góc trên bên phải
        int mapPosX = screenWidth - 110; // Điều chỉnh để bản đồ không sát mép phải
        int mapPosY = 10;  // Giữ bản đồ sát trên đỉnh màn hình

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mapPosX, mapPosY, 0);  // Vị trí góc trên phải màn hình
        guiGraphics.pose().scale(overlayScale, overlayScale, 1.0f);

        for (RoomData room : rooms) {
            BlockPos roomPos = room.getPosition();
            int mapX = calculateMapX(roomPos, playerPosition);
            int mapY = calculateMapY(roomPos, playerPosition);

            // Hiển thị phòng dựa trên trạng thái khám phá
            if (room.isDiscovered()) {
                renderDiscoveredRoom(guiGraphics, mapX, mapY);
            } else {
                renderUnexploredRoom(guiGraphics, mapX, mapY);
            }

            // Hiển thị icon đặc biệt nếu phòng đã được kích hoạt hoặc đã khám phá
            if (room.isTriggered() || room.isDiscovered()) {
                renderSpecialIcon(guiGraphics, mapX, mapY, room.getType());
            }
        }

        guiGraphics.pose().popPose();
    }

    // Hàm giả lập dữ liệu RoomData (thay thế cho việc gọi dữ liệu từ server)
    private static List<RoomData> getDummyRoomData() {
        List<RoomData> dummyRooms = new ArrayList<>();
        dummyRooms.add(new RoomData(new BlockPos(0, 64, 0), RoomType.NORMAL, EnumSet.noneOf(Direction.class)));
        dummyRooms.add(new RoomData(new BlockPos(16, 64, 0), RoomType.TREASURE, EnumSet.noneOf(Direction.class)));
        return dummyRooms;
    }

    // Tính toán vị trí x của phòng trên bản đồ
    private static int calculateMapX(BlockPos roomPos, BlockPos playerPosition) {
        return ((roomPos.getX() >> 4) - (playerPosition.getX() >> 4)) * 16;
    }

    // Tính toán vị trí y của phòng trên bản đồ
    private static int calculateMapY(BlockPos roomPos, BlockPos playerPosition) {
        return ((roomPos.getZ() >> 4) - (playerPosition.getZ() >> 4)) * 16;
    }

    // Render phòng đã khám phá
    private static void renderDiscoveredRoom(GuiGraphics guiGraphics, int mapX, int mapY) {
        guiGraphics.blit(MAP_ICONS, mapX, mapY, 0, 0, 16, 16);
    }

    // Render phòng chưa khám phá
    private static void renderUnexploredRoom(GuiGraphics guiGraphics, int mapX, int mapY) {
        guiGraphics.blit(MAP_ICONS, mapX, mapY, 16, 0, 16, 16);
    }

    // Render icon đặc biệt (SHOP, TREASURE, ELITE, BOSS, EVENT)
    private static void renderSpecialIcon(GuiGraphics guiGraphics, int mapX, int mapY, RoomType roomType) {
        int iconU = switch (roomType) {
            case TREASURE, ELITE -> 16;
            case BOSS -> 32;
            case EVENT -> 48;
            default -> 0;
        };
        int iconV = switch (roomType) {
            case SHOP, TREASURE -> 16;
            case ELITE, BOSS, EVENT -> 32;
            default -> 0;
        };
        guiGraphics.blit(MAP_ICONS, mapX, mapY, iconU, iconV, 16, 16);
    }
}
*/
