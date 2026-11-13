package net.susu609.the8dungeon.ai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.TickEvent;
import net.susu609.the8dungeon.world.entity.ClawdEntity;

/**
 * Thực hiện kế hoạch đã kiểm tra: mỗi vài tick đặt hoặc phá một khối.
 * Sáng tạo: đặt khối bất kỳ. Sinh tồn: lấy khối từ túi của chủ nhân, phá thì rơi đồ.
 */
public final class ClawdWorker {

    private static final int TICKS_PER_STEP = 4;
    private static final Map<UUID, Job> JOBS = new HashMap<>(); // chỉ dùng trên luồng server

    private ClawdWorker() {}

    private static final class Job {
        final ClawdEntity clawd;
        final ServerPlayer owner;
        final BlockPos origin;
        final Deque<ClawdPlan.Step> steps;
        int cooldown;
        int done;
        int skipped;
        int missingItems;

        Job(ClawdEntity clawd, ServerPlayer owner, List<ClawdPlan.Step> steps) {
            this.clawd = clawd;
            this.owner = owner;
            this.origin = clawd.blockPosition();
            this.steps = new ArrayDeque<>(steps);
        }
    }

    public static void start(ClawdEntity clawd, ServerPlayer owner, List<ClawdPlan.Step> steps) {
        clawd.getNavigation().stop();
        JOBS.put(clawd.getUUID(), new Job(clawd, owner, steps));
    }

    public static void stop(ClawdEntity clawd) {
        Job job = JOBS.remove(clawd.getUUID());
        if (job != null) ClawdBrain.say(job.owner, "Mình dừng lại rồi (" + job.done + " khối đã xong).");
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || JOBS.isEmpty()) return;
        Iterator<Job> it = JOBS.values().iterator();
        while (it.hasNext()) {
            Job job = it.next();
            if (!job.clawd.isAlive() || job.owner.isRemoved()) {
                it.remove();
                continue;
            }
            if (--job.cooldown > 0) continue;
            job.cooldown = TICKS_PER_STEP;

            ClawdPlan.Step step = job.steps.poll();
            if (step == null) {
                finish(job);
                it.remove();
                continue;
            }
            if (perform(job, step)) job.done++; else job.skipped++;
        }
    }

    private static boolean perform(Job job, ClawdPlan.Step step) {
        ServerLevel level = (ServerLevel) job.clawd.level();
        BlockPos pos = job.origin.offset(step.dx(), step.dy(), step.dz());
        if (!level.isLoaded(pos) || !level.mayInteract(job.owner, pos)) return false;

        BlockState current = level.getBlockState(pos);
        boolean creative = job.owner.isCreative();
        job.clawd.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (step.op() == ClawdPlan.Op.BREAK) {
            if (current.isAir() || current.getDestroySpeed(level, pos) < 0) return false; // không phá khối không phá được
            level.destroyBlock(pos, !creative);
            job.clawd.playWorkSound();
            return true;
        }

        // PLACE
        ResourceLocation rl = ResourceLocation.tryParse(step.block());
        if (rl == null) return false;
        Block block = BuiltInRegistries.BLOCK.get(rl);
        if (block == Blocks.AIR || !current.canBeReplaced()) return false;
        BlockState state = block.defaultBlockState();
        if (!level.isUnobstructed(state, pos, CollisionContext.empty())) return false; // có entity đứng đó

        if (!creative) {
            Item item = block.asItem();
            if (item == Items.AIR) return false;
            int slot = job.owner.getInventory().findSlotMatchingItem(new ItemStack(item));
            if (slot < 0) {
                job.missingItems++;
                return false;
            }
            job.owner.getInventory().removeItem(slot, 1);
        }
        level.setBlock(pos, state, 3);
        job.clawd.playWorkSound();
        return true;
    }

    private static void finish(Job job) {
        String msg = "Xong rồi! " + job.done + " khối";
        if (job.missingItems > 0) msg += ", thiếu " + job.missingItems + " khối trong túi của bạn";
        if (job.skipped - job.missingItems > 0) msg += ", bỏ qua " + (job.skipped - job.missingItems) + " chỗ không làm được";
        ClawdBrain.say(job.owner, msg + ".");
        job.clawd.playHappySound();
    }
}
