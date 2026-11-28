package net.susu609.the8dungeon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.susu609.the8dungeon.dungeon.DungeonBuilder;
import net.susu609.the8dungeon.dungeon.DungeonClientState;
import net.susu609.the8dungeon.dungeon.DungeonLayout;
import org.jetbrains.annotations.Nullable;

public class DoorAutoWalk {
    private static final double TRIGGER = 1.0;   // vào lỗ cửa là bắt đầu (không phải đứng trước cửa)
    private static final double INSIDE = 3.0;    // đích: 3 block sau tâm cửa
    private static final int TIMEOUT = 60;       // 3 giây: kẹt thì trả điều khiển
    private static final float TURN = 0.25F;   // mỗi TICK xoay được 25% phần góc còn lệch (0.1 = êm chậm, 0.5 = dứt khoát)
    private static Vec3 target;
    private static int ticks, cooldown;

    private static @Nullable Vec3 findTarget(LocalPlayer p, DungeonLayout layout) {
        BlockPos o = DungeonBuilder.fixedOrigin(layout.params());
        for (DungeonLayout.Door d : layout.doors()) {
            double cx = o.getX() + d.cx() * 16 + (d.east() ? 16 : 8);    // tâm lỗ cửa = vị trí gate
            double cz = o.getZ() + d.cz() * 16 + (d.east() ? 8 : 16);
            if (Math.abs(p.getX() - cx) > TRIGGER || Math.abs(p.getZ() - cz) > TRIGGER) continue;

            int dir = (d.east() ? p.getX() < cx : p.getZ() < cz) ? 1 : -1;          // đang ở phía nào của cửa
            int ax = d.east() ? d.cx() + (dir > 0 ? 1 : 0) : d.cx();                // ô PHÍA TRƯỚC
            int az = d.east() ? d.cz() : d.cz() + (dir > 0 ? 1 : 0);
            DungeonLayout.Room ahead = layout.roomAt(ax, az);
            if (ahead == null || DungeonClientState.visited().contains(ahead.id()))
                return null;   // phòng đã biết: đi tự do
            return d.east() ? new Vec3(cx + dir * INSIDE, p.getY(), cz) : new Vec3(cx, p.getY(), cz + dir * INSIDE);
        }
        return null;
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        LocalPlayer p = Minecraft.getInstance().player;
        DungeonLayout layout = DungeonClientState.layout();
        if (p == null || layout == null || event.getEntity() != p) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (target == null) target = findTarget(p, layout);
        if (target == null) return;

        Vec3 to = target.subtract(p.position());
        if (to.horizontalDistance() < 0.4) {
            stop(0);
            return;
        }
        if (++ticks > TIMEOUT) {
            stop(100);
            return;
        }

        float err = Mth.wrapDegrees(yawTo(to) - p.getYRot());           // lệch hướng bao nhiêu độ
        Input in = event.getInput();
        in.forwardImpulse = Math.max(0F, Mth.cos(err * Mth.DEG_TO_RAD));  // chưa quay thẳng thì đi chậm, thẳng rồi mới đi hết tốc độ
        in.leftImpulse = 0F;
        in.up = true;
        in.down = in.left = in.right = in.jumping = in.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onFrame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || target == null) return;
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;

        Vec3 to = target.subtract(p.position());
        float k = 1F - (float) Math.pow(1F - TURN, Minecraft.getInstance().getDeltaFrameTime());   // đổi "mỗi tick" thành "mỗi khung hình"
        float yaw = p.getYRot() + Mth.wrapDegrees(yawTo(to) - p.getYRot()) * k;
        p.setYRot(yaw);
        p.yRotO = yaw;               // tắt nội suy mặc định của vanilla, vì đã tự mượt
    }

    private static float yawTo(Vec3 d) {
        return (float) Math.toDegrees(Math.atan2(-d.x, d.z));
    }

    private static void stop(int rest) {
        target = null;
        ticks = 0;
        cooldown = rest;
    }
}
