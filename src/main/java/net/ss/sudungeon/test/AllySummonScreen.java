//
//package net.ss.sudungeon.test;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import org.checkerframework.checker.units.qual.h;
//
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.eventbus.api.EventPriority;
//import net.minecraftforge.client.event.RenderGuiEvent;
//import net.minecraftforge.api.distmarker.Dist;
//
//import net.minecraft.world.level.Level;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.client.Minecraft;
//
//import java.awt.*;
//
//@Mod.EventBusSubscriber({Dist.CLIENT})
//public class AllySummonOverlay extends Screen {
//    protected AllySummonOverlay(Component p_96550_) {
//        super(p_96550_);
//    }
//
//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    public static void eventHandler(RenderGuiEvent.Pre event) {
//        int w = event.getWindow().getGuiScaledWidth();
//        int h = event.getWindow().getGuiScaledHeight();
//        Level world = null;
//        double x = 0;
//        double y = 0;
//        double z = 0;
//        Player entity = Minecraft.getInstance().player;
//        if (entity != null) {
//            world = entity.level();
//            x = entity.getX();
//            y = entity.getY();
//            z = entity.getZ();
//        }
//        if (true) {
//            // Logic của bạn
//        }
//    }
//
//    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("mymod", "textures/gui/ally_summon.png");
//    private boolean isFrontSide = true;  // true: mặt trước, false: mặt sau
//
//    // Danh sách đồng minh cho mặt trước và mặt sau
//    private final ItemStack[] frontAllies = new ItemStack[5];
//    private final ItemStack[] backAllies = new ItemStack[5];
//
//    // Kích thước của ô và khung GUI
//    private static final int GUI_WIDTH = 160;
//    private static final int GUI_HEIGHT = 112;
//    private static final int SLOT_WIDTH = 30;
//    private static final int SLOT_HEIGHT = 30;
//    private static final int SLOT_SPACING = 4;
//
//    public AllySummonOverlay(Component titleIn) {
//        super(titleIn);
//        // Khởi tạo danh sách đồng minh (ví dụ)
//        for (int i = 0; i < 5; i++) {
//            frontAllies[i] = new ItemStack(net.minecraft.world.item.Items.APPLE);  // Ví dụ: Đồng minh là táo
//            backAllies[i] = new ItemStack(net.minecraft.world.item.Items.BEEF);  // Ví dụ: Đồng minh là thịt bò
//        }
//    }
//
//    @Override
//    protected void init() {
//        // Thêm nút để chuyển đổi giữa mặt trước và mặt sau
//        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height / 2 + 60, 100, 20, new TextComponent("Đổi Mặt"), button -> {
//            isFrontSide = !isFrontSide;  // Đổi trạng thái mặt trước và mặt sau
//        }));
//    }
//
//    @Override
//    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//        // Vẽ khung GUI
//        this.renderBackground(matrixStack);
//        this.minecraft.getTextureManager().bind(BACKGROUND_TEXTURE);
//        blit(matrixStack, this.width / 2 - GUI_WIDTH / 2, this.height / 2 - GUI_HEIGHT / 2, 0, 0, GUI_WIDTH, GUI_HEIGHT);
//
//        // Vẽ các khung ô
//        int totalSlotsWidth = (SLOT_WIDTH * 5) + (SLOT_SPACING * 4);
//        int xPos = (this.width / 2) - (totalSlotsWidth / 2);  // Căn giữa theo chiều ngang
//        int yPos = this.height / 2 - 40;  // Vị trí Y của hàng đồng minh
//
//        // Render khung của 5 ô
//        for (int i = 0; i < 5; i++) {
//            fill(matrixStack, xPos + (i * (SLOT_WIDTH + SLOT_SPACING)), yPos,
//                    xPos + (i * (SLOT_WIDTH + SLOT_SPACING)) + SLOT_WIDTH, yPos + SLOT_HEIGHT, 0xFFCCCCCC);  // Màu xám làm ví dụ
//        }
//
//        // Render icon đồng minh
//        drawAllyIcons(matrixStack, xPos, yPos);
//
//        // Render các thành phần mặc định
//        super.render(matrixStack, mouseX, mouseY, partialTicks);
//    }
//
//    // Vẽ icon của đồng minh dựa trên trạng thái (mặt trước hoặc mặt sau)
//    private void drawAllyIcons(PoseStack matrixStack, int xPos, int yPos) {
//        ItemRenderer itemRenderer = this.minecraft.getItemRenderer();
//        ItemStack[] currentAllies = isFrontSide ? frontAllies : backAllies;  // Lấy đồng minh tương ứng mặt trước hoặc sau
//
//        for (int i = 0; i < 5; i++) {
//            // Render icon đồng minh
//            itemRenderer.renderGuiItem(currentAllies[i], xPos + (i * (SLOT_WIDTH + SLOT_SPACING)), yPos);
//        }
//    }
//
//    @Override
//    public boolean isPauseScreen() {
//        return false;  // Không tạm dừng game khi mở GUI
//    }
//
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        // Xử lý click chuột nếu cần
//        return super.mouseClicked(mouseX, mouseY, button);
//    }
//}
