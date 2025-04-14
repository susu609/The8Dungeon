package net.ss.sudungeon.world.entity;

import net.minecraft.client.animation.AnimationDefinition;

public class ModAnimationState {
    private AnimationDefinition definition;
    private long startTick;
    private long age;
    private long duration;
    private boolean running;
    private boolean looping;
    private long lastUpdateTime;
    private final long minTimeBetweenStart = 500; // Ví dụ: 500ms giữa mỗi lần bắt đầu lại animation

    // Khởi tạo và bắt đầu một animation
    public void start(AnimationDefinition def, long tick, boolean loop) {
        this.definition = def;
        this.startTick = tick;
        this.looping = loop;
        this.running = true;
        this.age = 0;  // Reset lại tuổi của animation mỗi khi bắt đầu
    }

    // Dừng animation hiện tại
    public void stop() {
        this.running = false;
    }

    // Kiểm tra xem animation có đang chạy hay không
    public boolean isRunning() {
        return running;
    }

    // Lấy định nghĩa của animation
    public AnimationDefinition getDefinition() {
        return definition;
    }

    // Kiểm tra nếu animation cần được bắt đầu lại
    public boolean shouldStart(AnimationDefinition def) {
        // Kiểm tra nếu animation mới cần được bắt đầu lại, và có đủ thời gian giữa các lần bắt đầu
        if (this.definition != def || !this.running) {
            long currentTime = System.currentTimeMillis();
            // Kiểm tra xem đã đủ thời gian giữa các lần bắt đầu animation chưa
            if (currentTime - this.lastUpdateTime >= minTimeBetweenStart) {
                this.lastUpdateTime = currentTime; // Cập nhật thời gian bắt đầu mới
                return true; // Được phép bắt đầu animation mới
            }
        }
        return false; // Không cần bắt đầu lại animation
    }


    public boolean isReadyToStart() {
        return this.lastUpdateTime + this.minTimeBetweenStart <= System.currentTimeMillis();
    }


    // Lấy thời gian bắt đầu của animation
    public long getStartTick() {
        return startTick;
    }

    // Kiểm tra xem animation có dừng hay không
    public boolean isStopped(long tick) {
        return !looping && duration > 0 && (tick - startTick) >= duration;
    }

    // Kiểm tra nếu animation là dạng một lần (không lặp lại)
    public boolean isOneShot() {
        return !looping;
    }

    // Kiểm tra nếu animation là lặp lại
    public boolean isLooping() {
        return looping;
    }

    // Tính toán thời gian hoạt động của animation
    public float getTime(long currentTick, float partialTicks) {
        return (currentTick - startTick) + partialTicks;
    }

    // Cập nhật trạng thái của animation theo thời gian
    public void update(long tick) {
        if (!running) return;

        this.age = tick - this.startTick;

        // Dừng animation khi đạt tới thời gian
        if (!looping && duration > 0 && this.age >= duration) {
            this.running = false;
        }
    }

    // Đặt lại thời gian để tái sử dụng hoạt ảnh
    public void reset(long tick) {
        this.startTick = tick;
        this.age = 0;
    }

    // Thiết lập độ dài cho animation (dành cho các animation không lặp lại)
    public void setDuration(long duration) {
        this.duration = duration;
    }
}
