package net.ss.sudungeon.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.ss.sudungeon.world.level.levelgen.dungeongen.RoomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DungeonReportScreen extends Screen {
    private final List<RoomData> rooms; // Danh sách phòng

    public DungeonReportScreen (List<RoomData> rooms) {
        super(Component.literal("Dungeon Report"));
        this.rooms = rooms;
    }

    @Override
    protected void init () {
        super.init();
        // Thêm nút "Done" để đóng màn hình
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"), (button) -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render (@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        int y = 40; // Khai báo y ở đây
        if (rooms != null && !rooms.isEmpty()) {
            // Hiển thị thông tin của từng phòng
            for (RoomData room : rooms) {
                String roomInfo = String.format("Phòng %s tại %s", room.type, room.pos);
                guiGraphics.drawString(this.font, roomInfo, 20, y, 0xFFFFFF);
                y += 15;
            }
        } else {
            // Hiển thị thông báo nếu không có phòng nào
            guiGraphics.drawString(this.font, "Không có dữ liệu phòng nào.", 20, y, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
