package net.ss.sudungeon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.ss.sudungeon.network.packet.FillPacketHandler;
import net.ss.sudungeon.util.MinecraftUtils;
import net.ss.sudungeon.world.inventory.FillToolMenu;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class FillToolScreen extends AbstractContainerScreen<FillToolMenu> {
    Minecraft mc = MinecraftUtils.getMinecraft();

    private EditBox blockIdBox;
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;
    private final static HashMap<String, Object> guistate = FillToolMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;

    public FillToolScreen(FillToolMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = 60;
        int spacing = 30;

        // Khôi phục từ guistate nếu có
        if (guistate.containsKey("pos1")) pos1 = (BlockPos) guistate.get("pos1");
        if (guistate.containsKey("pos2")) pos2 = (BlockPos) guistate.get("pos2");

        blockIdBox = new EditBox(font, centerX - 100, startY, 200, 20, Component.literal("Block ID"));
        blockIdBox.setValue(guistate.containsKey("blockId") ? guistate.get("blockId").toString() : "");
        addRenderableWidget(blockIdBox);

        addRenderableWidget(Button.builder(Component.literal("Set First Position"), btn -> {
            pos1 = mc.player.blockPosition();
            guistate.put("pos1", pos1);
            mc.player.displayClientMessage(Component.literal("First position set: " + pos1), true);
        }).bounds(centerX - 100, startY + spacing, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Set Second Position"), btn -> {
            pos2 = mc.player.blockPosition();
            guistate.put("pos2", pos2);
            mc.player.displayClientMessage(Component.literal("Second position set: " + pos2), true);
        }).bounds(centerX - 100, startY + spacing * 2, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Fill!"), btn -> {
            if (pos1 != null && pos2 != null) {
                String blockId = blockIdBox.getValue();
                guistate.put("blockId", blockId);
                FillPacketHandler.sendFillPacket(pos1, pos2, blockId);
            } else {
                mc.player.displayClientMessage(Component.literal("Chưa đặt đủ vị trí!"), false);
            }
        }).bounds(centerX - 100, startY + spacing * 3, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Lấy Block Dưới Chân"), btn -> {
            BlockPos under = mc.player.blockPosition().below();
            Block block = mc.level.getBlockState(under).getBlock();
            String id = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString();
            blockIdBox.setValue(id);
            guistate.put("blockId", id);
            mc.player.displayClientMessage(Component.literal("Đã lấy ID: " + id), true);
        }).bounds(centerX - 100, startY + spacing * 4, 200, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, title.getString(), width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Hiển thị pos1, pos2
        int textY = 60 + 30 * 5 + 10;
        if (pos1 != null) guiGraphics.drawCenteredString(font, "Pos1: " + pos1, width / 2, textY, 0x00FF00);
        if (pos2 != null) guiGraphics.drawCenteredString(font, "Pos2: " + pos2, width / 2, textY + 12, 0x00FF00);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        // Không cần render nền
    }
}
