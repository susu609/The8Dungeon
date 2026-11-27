package net.susu609.the8dungeon.world.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Chiêu lao (dash) dùng chung cho mọi quái biết "gồng" (Charger).
 * <p>
 * Dòng thời gian, t đếm TĂNG từ 0 mỗi tick:
 * <pre>
 *   0 ──── aim ──── aim+lock ──── +dash ──── +recover
 *   NGẮM     │  KHOÁ    │    LAO     │   NGHỈ    │ xong → stop() → cooldown
 *   xoay theo│ đứng sững│ chạy thẳng │ đứng thở  │
 *            │ = né đi  │ không lái  │ = đánh đi │
 * </pre>
 * Cờ MOVE + LOOK: trong suốt 4 pha, goal đi bộ / tấn công thường KHÔNG chen vào được.
 */
public class DashGoal<T extends PathfinderMob & Charger> extends Goal {
    private final T mob;
    private final DashSettings s;
    private int cooldown;   // tick còn phải chờ trước lần dash sau (đếm lùi trong canUse)
    private int t;          // tick đã trôi qua kể từ start()
    private Vec3 dir;       // hướng lao, chỉ trục ngang (y = 0), độ dài 1

    // TODO 1: field "timer" cũ đã xoá vì không ai dùng nữa (t thay thế). Nếu IDE còn báo, xoá nốt.

    public DashGoal(T mob, DashSettings settings) {
        this.mob = mob;
        this.s = settings;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** Vector hướng → góc yaw kiểu Minecraft (0 = nam, 90 = tây, 180 = bắc). */
    private static float yawOf(Vec3 d) {
        return (float) Math.toDegrees(Math.atan2(-d.x, d.z));
    }

    // ───────────── Khi nào được bắt đầu ─────────────

    /** Forge hỏi mỗi tick: "giờ dash được chưa?" */
    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        LivingEntity target = mob.getTarget();          // LivingEntity: người chơi không phải Monster
        if (target == null || !mob.hasLineOfSight(target)) return false;
        double d = mob.distanceTo(target);
        return d > s.minDist() && d < s.maxDist();      // quá gần thì chém thường, quá xa thì đi bộ lại
    }

    // ───────────── Bắt đầu / kết thúc ─────────────

    @Override
    public void start() {
        t = 0;
        // TODO 2: dir có thể còn null nếu target biến mất ngay tick đầu (người chơi chết, logout).
        //   Cho dir một giá trị mặc định = hướng mặt hiện tại của quái, chỉ giữ trục ngang.
        //   Gợi ý: mob.getLookAngle() trả Vec3 hướng nhìn; .multiply(1, 0, 1) bỏ trục y; .normalize() về độ dài 1.
        //   Viết 1 dòng:  dir = ...;
        mob.getNavigation().stop();                     // huỷ đường đi đang có, đứng lại
        mob.setCharging(true);                          // client thấy → giơ tay (GraveKnightModel)
        mob.playSound(s.sound(), 1F, 1.4F);             // âm thanh báo hiệu
    }

    /** Hết 4 pha thì dừng. */
    @Override
    public boolean canContinueToUse() {
        return t < s.aim() + s.lock() + s.dash() + s.recover();
    }

    /** Chạy khi hết pha HOẶC bị ngắt giữa chừng (quái chết, mất mục tiêu...). */
    @Override
    public void stop() {
        mob.setCharging(false);                         // luôn hạ tay, kể cả khi bị ngắt giữa pha NGẮM
        cooldown = s.cooldown();
    }

    // ───────────── Mỗi tick: chọn pha theo t ─────────────

    @Override
    public void tick() {
        t++;
        int lockAt = s.aim(), dashAt = lockAt + s.lock(), restAt = dashAt + s.dash();
        LivingEntity target = mob.getTarget();

        if (t <= lockAt) {                                       // NGẮM: còn xoay theo người chơi
            stand();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 30F, 30F);
                dir = target.position().subtract(mob.position()).multiply(1, 0, 1).normalize();
            }
            smoke();
        } else if (t <= dashAt) {                                // KHOÁ: hướng đã chốt, đứng sững
            stand();
            mob.setYRot(yawOf(dir));                             // thân + đầu nhìn đúng hướng sẽ lao
            mob.setYHeadRot(yawOf(dir));
            smoke();
        } else if (t <= restAt) {                                // LAO: không bẻ lái
            mob.setCharging(false);                              // hạ tay khi bắt đầu chạy
            mob.setDeltaMovement(dir.x * s.speed(), mob.getDeltaMovement().y, dir.z * s.speed());
            hitAround();
        } else {                                                 // NGHỈ: đứng thở, chờ bị phản công
            stand();
        }
    }

    // ───────────── Các khối nhỏ ─────────────

    /**
     * Khói dưới chân trong lúc gồng: dấu hiệu thứ hai (ngoài giơ tay) cho người chơi biết sắp lao.
     */
    private void smoke() {
        // TODO 3: chỉ server mới gửi hạt được → kiểm tra level có phải ServerLevel không.
        //   Mẫu "kiểm tra + lấy biến" trong một dòng:  if (!(mob.level() instanceof ServerLevel sl)) return;
        //
        // TODO 4: gửi hạt khói ở chân quái.
        //   Hàm: sl.sendParticles(LOẠI_HẠT, x, y, z, SỐ_LƯỢNG, độLệchX, độLệchY, độLệchZ, tốcĐộ)
        //     LOẠI_HẠT : ParticleTypes.CLOUD (khói trắng) — thử thêm ParticleTypes.CRIT cho có tia
        //     x, y, z  : mob.getX(), mob.getY() + 0.1 (sát sàn), mob.getZ()
        //     SỐ_LƯỢNG : 3 là vừa (hàm chạy mỗi tick → 18 tick × 3 = ~54 hạt)
        //     độ lệch  : 0.3, 0, 0.3 → toả ngang quanh chân, không bay lên
        //     tốc độ   : 0.02 → gần như đứng yên
        //   Import: net.minecraft.core.particles.ParticleTypes, net.minecraft.server.level.ServerLevel
        //
        // ❓ Muốn pha KHOÁ khác pha NGẮM (để người chơi biết "đã chốt rồi, né ngay")?
        //   Thêm tham số boolean locked, gọi smoke(false) ở NGẮM và smoke(true) ở KHOÁ,
        //   rồi đổi loại hạt: locked ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.CLOUD.
    }

    /**
     * Gây sát thương cho ai bị quái đâm trúng trong lúc LAO.
     */
    private void hitAround() {
        // TODO 5: lấy mọi LivingEntity nằm trong hitbox quái nới thêm 0.3 block, TRỪ chính nó.
        //   Hàm: mob.level().getEntitiesOfClass(LivingEntity.class, HỘP, ĐIỀU_KIỆN)
        //     HỘP       : mob.getBoundingBox().inflate(0.3)
        //     ĐIỀU_KIỆN : other -> other != mob
        //     ⚠️ đặt tên tham số lambda KHÁC "mob" (bản cũ viết "e -> e != e" nên không trúng ai)
        //   Viết thành vòng: for (LivingEntity hit : ...) { ... }
        //
        // TODO 6: trong vòng, gây sát thương rồi hất ngược.
        //   hit.hurt(NGUỒN, SỐ_MÁU) trả về true nếu THẬT SỰ trúng (không bị miễn nhiễm vừa trúng xong).
        //     NGUỒN  : mob.damageSources().mobAttack(mob)
        //     SỐ_MÁU : s.damage()
        //   Chỉ khi hurt(...) == true thì mới hất: hit.knockback(LỰC, hướngX, hướngZ)
        //     LỰC    : 1.2
        //     hướng  : -dir.x, -dir.z   ← knockback nhận hướng NGƯỢC (từ nạn nhân về phía kẻ đánh)
        //
        // ❓ Quái cùng phòng đứng chắn đường cũng bị đâm. Muốn chỉ đâm người chơi?
        //   Đổi LivingEntity.class thành Player.class (import net.minecraft.world.entity.player.Player).
        //
        // ❓ Đâm trúng rồi có nên dừng lao luôn không? Thử: sau khi trúng, gán t = restAt
        //   (cần đưa restAt thành field hoặc tính lại) → nhảy thẳng sang pha NGHỈ.
    }

    /** Giữ quái đứng yên theo phương ngang; vẫn để trọng lực (y) hoạt động. */
    private void stand() {
        mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
    }
}
