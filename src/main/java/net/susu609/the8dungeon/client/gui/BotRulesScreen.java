package net.susu609.the8dungeon.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.ai.BotRules;
import net.susu609.the8dungeon.network.RulesActionPacket;
import net.susu609.the8dungeon.network.RulesSyncPacket;

/**
 * Màn hình luật cho bot: chọn "Tất cả" để sửa luật chung, hoặc chọn một bot để cho nó luật riêng.
 * Phía dưới có thống kê hiệu năng (heap, thời gian tick từng bot). Mở bằng /bot gui.
 */
public final class BotRulesScreen extends Screen {

    private RulesSyncPacket data;
    private String selected = "";      // "" = luật chung
    private boolean inherit = true;    // chỉ có nghĩa khi đã chọn một bot
    private BotRules cur;
    private int ticks;
    private int statsY = 184;

    private BotRulesScreen(RulesSyncPacket data) {
        super(Component.literal("Luật cho bot"));
        this.data = data;
        this.cur = data.global.copy();
    }

    /** Gọi khi client nhận RulesSyncPacket. */
    public static void accept(RulesSyncPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BotRulesScreen s) {
            s.update(p);
        } else if (p.open) {
            mc.setScreen(new BotRulesScreen(p));
        }
    }

    private void update(RulesSyncPacket p) {
        boolean structure = p.bots.size() != data.bots.size();
        data = p;
        if (!selected.isEmpty() && p.bots.stream().noneMatch(r -> r.name().equals(selected))) {
            selected = "";
            cur = p.global.copy();
            structure = true;
        }
        if (structure) rebuildWidgets();
    }

    private void select(String target) {
        selected = target;
        if (target.isEmpty()) {
            cur = data.global.copy();
            inherit = true;
        } else {
            for (RulesSyncPacket.BotRow r : data.bots) {
                if (r.name().equals(target)) {
                    inherit = r.inherit();
                    cur = (inherit ? data.global : r.rules()).copy();
                }
            }
        }
        rebuildWidgets();
    }

    private void send() {
        Susu609Mod.PACKET_HANDLER.sendToServer(new RulesActionPacket(RulesActionPacket.APPLY, selected, inherit, cur));
    }

    // ===== Bố cục =====

    @Override
    protected void init() {
        int cx = width / 2;
        int lx = cx - 155;
        int rx = cx + 5;

        // Hàng chọn đối tượng
        List<String> names = new ArrayList<>();
        names.add("");
        data.bots.forEach(r -> names.add(r.name()));
        int bw = Math.min(70, 310 / names.size());
        int x = cx - bw * names.size() / 2;
        for (String n : names) {
            String label = (n.equals(selected) ? "> " : "") + (n.isEmpty() ? "Tất cả" : n);
            addRenderableWidget(Button.builder(Component.literal(label), b -> select(n))
                    .bounds(x, 18, bw - 2, 18).build());
            x += bw;
        }

        int y = 40;
        if (!selected.isEmpty()) {
            addRenderableWidget(CycleButton.onOffBuilder(inherit).create(lx, y, 310, 18,
                    Component.literal("Dùng luật chung"), (b, v) -> {
                        inherit = v;
                        cur = data.global.copy(); // bộ riêng bắt đầu từ luật chung
                        send();
                        rebuildWidgets();
                    }));
        }

        boolean editable = selected.isEmpty() || !inherit;
        y = 62;
        toggle(lx, y, "Đánh quái", cur.fight, editable, v -> cur.fight = v);
        toggle(rx, y, "Săn thức ăn", cur.hunt, editable, v -> cur.hunt = v);
        y += 20;
        toggle(lx, y, "Chặt gỗ / đào", cur.mine, editable, v -> cur.mine = v);
        toggle(rx, y, "Trú ẩn ban đêm", cur.shelter, editable, v -> cur.shelter = v);
        y += 20;
        toggle(lx, y, "Khám phá", cur.explore, editable, v -> cur.explore = v);
        toggle(rx, y, "Nhặt đồ rơi", cur.collect, editable, v -> cur.collect = v);
        y += 20;
        toggle(lx, y, "Nấu ăn bằng lò", cur.cook, editable, v -> cur.cook = v);
        toggle(rx, y, "Dừng khi vắng người", cur.pauseWhenAlone, editable, v -> cur.pauseWhenAlone = v);
        y += 20;
        slider(lx, y, "Chạy trốn khi máu <", 10, 90, 5, cur.fleeHpPct, editable, v -> cur.fleeHpPct = v, v -> v + "%");
        slider(rx, y, "Ăn khi đói ≤", 4, 19, 1, cur.eatBelow, editable, v -> cur.eatBelow = v, String::valueOf);
        y += 20;
        slider(lx, y, "Bán kính", 0, 512, 16, cur.roam, editable, v -> cur.roam = v, v -> v < 32 ? "không giới hạn" : v + " khối");
        slider(rx, y, "Nhịp suy nghĩ", 5, 40, 5, cur.thinkEvery, editable, v -> cur.thinkEvery = v, v -> v + " tick");
        y += 20;
        slider(lx, y, "Quét khối", 200, 1500, 100, cur.scanBudget, editable, v -> cur.scanBudget = v, v -> v + "/lần");
        toggle(rx, y, "Hợp tác nhóm", cur.coop, editable, v -> cur.coop = v);
        statsY = y + 24;
    }

    private void toggle(int x, int y, String name, boolean value, boolean editable, Consumer<Boolean> set) {
        CycleButton<Boolean> b = CycleButton.onOffBuilder(value).create(x, y, 150, 18,
                Component.literal(name), (btn, v) -> {
                    set.accept(v);
                    send();
                });
        b.active = editable;
        addRenderableWidget(b);
    }

    private void slider(int x, int y, String name, int min, int max, int step, int value, boolean editable,
                        IntConsumer set, IntFunction<String> fmt) {
        IntSlider s = new IntSlider(x, y, 150, 18, name, min, max, step, value, set, fmt);
        s.active = editable;
        addRenderableWidget(s);
    }

    private final class IntSlider extends AbstractSliderButton {
        private final String name;
        private final int min, max, step;
        private final IntConsumer set;
        private final IntFunction<String> fmt;

        IntSlider(int x, int y, int w, int h, String name, int min, int max, int step, int value,
                  IntConsumer set, IntFunction<String> fmt) {
            super(x, y, w, h, Component.empty(), (value - min) / (double) (max - min));
            this.name = name;
            this.min = min;
            this.max = max;
            this.step = step;
            this.set = set;
            this.fmt = fmt;
            updateMessage();
        }

        private int val() {
            int v = min + (int) Math.round(this.value * (max - min) / step) * step;
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(name + ": " + fmt.apply(val())));
        }

        @Override
        protected void applyValue() {
            set.accept(val());
        }

        @Override
        public void onRelease(double mx, double my) {
            super.onRelease(mx, my);
            send(); // chỉ gửi khi thả chuột, không gửi liên tục lúc kéo
        }
    }

    // ===== Vẽ và cập nhật =====

    @Override
    public void tick() {
        if (++ticks % 40 == 0) { // làm mới thống kê mỗi 2 giây
            Susu609Mod.PACKET_HANDLER.sendToServer(new RulesActionPacket(RulesActionPacket.REQUEST, "", true, cur));
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 6, 0xFFFFFF);
        super.render(g, mx, my, partial);

        int x = width / 2 - 155;
        int y = statsY;
        int needMb = data.maxBots * 1024;
        boolean low = data.heapMaxMb < needMb;
        g.drawString(font, "Bot " + data.bots.size() + "/" + data.maxBots + "  |  Heap server "
                + data.heapUsedMb + "/" + data.heapMaxMb + " MB (đề xuất ≥ " + needMb + " MB)  |  chunk "
                + data.loadedChunks, x, y, low ? 0xFFE05050 : 0xFFC8C8C8, false);
        y += 10;
        if (data.reduced) {
            g.drawString(font, "RAM gần đầy: đang ở chế độ tiết kiệm (suy nghĩ chậm hơn, không thêm bot)", x, y, 0xFFE0A050, false);
            y += 10;
        }
        for (RulesSyncPacket.BotRow r : data.bots) {
            g.drawString(font, r.name() + ": " + r.goal() + ", " + String.format("%.2f", r.micros() / 1000.0)
                    + " ms/tick" + (r.inherit() ? "" : " (luật riêng)"), x, y, 0xFFA0C8A0, false);
            y += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
