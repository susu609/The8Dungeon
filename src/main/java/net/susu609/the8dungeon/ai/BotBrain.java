package net.susu609.the8dungeon.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bộ não sinh tồn (Utility AI, không dùng model AI).
 * Mỗi THINK_EVERY tick: cảm nhận -> chấm điểm các mục tiêu -> chọn mục tiêu điểm cao nhất
 * (có quán tính để không đổi qua đổi lại). Mỗi tick chỉ chạy hành động của mục tiêu đang chọn.
 */
final class BotBrain {

    private enum Goal { IDLE, FLEE, FIGHT, EAT, HUNT, COLLECT, SHELTER, WOOD, STONE, DIRT, EXPLORE }

    private static final int THINK_EVERY = 10;
    private static final double REACH = 4.0D;

    private final BotPlayer bot;
    private final BotSenses senses = new BotSenses();
    private final BotNav nav;

    private int age;
    private Goal goal = Goal.IDLE;
    private int goalSince;

    // Ảnh chụp kho đồ (cập nhật mỗi lần suy nghĩ)
    private int logs, planks, cobble, placeable, foodCount, pickRank;

    // Trạng thái con của từng mục tiêu
    private BlockPos mineTarget;
    private float mineProgress;
    private int reachFails;
    private int eatTimer = 32;
    private BlockPos fleeTarget;
    private BlockPos exploreTarget;
    private double heading = Math.random() * Math.PI * 2.0D;
    private int collectSince;
    private final List<BlockPos> shelterPlan = new ArrayList<>();
    private int shelterIdx;
    private boolean sheltered;
    private boolean shelterDone;

    BotBrain(BotPlayer bot) {
        this.bot = bot;
        this.nav = new BotNav(bot);
    }

    String status() {
        return goal + " hp=" + (int) bot.getHealth() + " food=" + bot.getFoodData().getFoodLevel()
                + " gỗ=" + (logs + planks) + " đá=" + cobble + " cuốc=" + pickRank
                + (sheltered ? " [trú ẩn]" : "");
    }

    // ===== Vòng lặp chính =====

    void tick() {
        age++;
        bot.setJumping(false);
        if (age % THINK_EVERY == 0) think();
        act();
    }

    private void think() {
        for (int i = 0; i < 6 && BotCrafting.step(bot); i++) {
            // chế liên tục tới khi hết việc
        }
        snapshotInventory();

        boolean inShelter = sheltered && isNight();
        senses.update(bot, age, !inShelter);

        float hp = bot.getHealth() / bot.getMaxHealth();
        int food = bot.getFoodData().getFoodLevel();
        LivingEntity h = senses.hostile;
        double hd = senses.hostileDist;
        boolean night = isNight();
        boolean dusk = isDusk();
        boolean creeper = h instanceof Creeper && hd < 6.5D;
        ItemStack foodStack = BotItems.bestFood(bot, food <= 6);

        float[] sc = new float[Goal.values().length];
        if (h != null && !inShelter) {
            if (creeper || hp < 0.4F) sc[Goal.FLEE.ordinal()] = 0.95F;
            else if (hd <= 16.0D) sc[Goal.FIGHT.ordinal()] = hd < 6.0D ? 0.85F : 0.6F;
        }
        if (!foodStack.isEmpty() && (food <= 14 || (hp < 0.7F && food < 20)) && (h == null || hd > 5.0D || inShelter)) {
            sc[Goal.EAT.ordinal()] = food <= 6 ? 1.0F : 0.8F;
        }
        if (!inShelter) {
            if (senses.prey != null && foodCount < 2 && food <= 16) sc[Goal.HUNT.ordinal()] = 0.6F + (20 - food) / 80.0F;
            if (senses.item != null) sc[Goal.COLLECT.ordinal()] = 0.65F;
            boolean needWood = logs * 4 + planks < 20 || pickRank == 0;
            if (senses.best(BotSenses.LOG) != null && needWood) sc[Goal.WOOD.ordinal()] = pickRank == 0 ? 0.65F : 0.5F;
            boolean shortOnBlocks = placeable < 9 && (dusk || night);
            if (pickRank >= 1 && senses.best(BotSenses.STONE) != null && (cobble < 24 || shortOnBlocks)) {
                sc[Goal.STONE.ordinal()] = shortOnBlocks ? 0.75F : 0.45F;
            }
            if (shortOnBlocks && senses.best(BotSenses.DIRT) != null) sc[Goal.DIRT.ordinal()] = 0.7F;
            sc[Goal.EXPLORE.ordinal()] = 0.2F;
        }
        if (night && !shelterDone && (placeable >= 9 || !shelterPlan.isEmpty())) sc[Goal.SHELTER.ordinal()] = 0.8F;
        else if (!night && !shelterPlan.isEmpty()) sc[Goal.SHELTER.ordinal()] = 0.9F; // phải phá chỗ trú để ra
        if (!night && !dusk && shelterPlan.isEmpty()) shelterDone = false;

        // Chọn điểm cao nhất, thêm quán tính cho mục tiêu hiện tại
        Goal pick = Goal.IDLE;
        float bestScore = 0.0F;
        for (Goal g : Goal.values()) {
            float s = sc[g.ordinal()] + (g == goal ? 0.08F : 0.0F);
            if (sc[g.ordinal()] > 0.0F && s > bestScore) {
                bestScore = s;
                pick = g;
            }
        }
        if (pick != goal) {
            boolean dwell = age - goalSince < 30 && sc[goal.ordinal()] > 0.0F && bestScore < 0.9F;
            if (!dwell) switchGoal(pick);
        }
        if (goal == Goal.FLEE && h != null) fleeTarget = awayFrom(h);
    }

    private void switchGoal(Goal next) {
        goal = next;
        goalSince = age;
        nav.stop();
        mineTarget = null;
        mineProgress = 0.0F;
        reachFails = 0;
        eatTimer = 32;
        fleeTarget = null;
        exploreTarget = null;
        if (next == Goal.FIGHT || next == Goal.HUNT) BotItems.equipWeapon(bot);
    }

    private void snapshotInventory() {
        Inventory inv = bot.getInventory();
        logs = BotItems.count(inv, s -> s.is(ItemTags.LOGS));
        planks = BotItems.count(inv, s -> s.is(ItemTags.PLANKS));
        cobble = BotItems.count(inv, s -> s.is(Items.COBBLESTONE));
        placeable = BotItems.count(inv, BotItems::isPlaceable);
        foodCount = BotItems.count(inv, s -> s.isEdible() && !s.is(Items.ROTTEN_FLESH) && !s.is(Items.SPIDER_EYE));
        pickRank = BotItems.rank(inv, PickaxeItem.class);
    }

    private boolean isNight() {
        long t = bot.level().getDayTime() % 24000L;
        return t >= 12700L && t < 23000L;
    }

    private boolean isDusk() {
        long t = bot.level().getDayTime() % 24000L;
        return t >= 11000L && t < 12700L;
    }

    // ===== Hành động =====

    private void act() {
        switch (goal) {
            case FLEE -> actFlee();
            case FIGHT -> chase(senses.hostile);
            case HUNT -> chase(senses.prey);
            case EAT -> actEat();
            case COLLECT -> actCollect();
            case SHELTER -> actShelter();
            case WOOD -> actMine(BotSenses.LOG);
            case STONE -> actMine(BotSenses.STONE);
            case DIRT -> actMine(BotSenses.DIRT);
            case EXPLORE -> actExplore();
            default -> nav.stop();
        }
    }

    private void actFlee() {
        if (fleeTarget == null) {
            nav.stop();
            return;
        }
        if (nav.moveTo(fleeTarget, age, true) || nav.failed) fleeTarget = null;
    }

    private BlockPos awayFrom(LivingEntity threat) {
        Vec3 dir = bot.position().subtract(threat.position()).multiply(1.0D, 0.0D, 1.0D);
        if (dir.lengthSqr() < 1.0E-4D) dir = new Vec3(1.0D, 0.0D, 0.0D);
        dir = dir.normalize().scale(14.0D);
        return surface(bot.getX() + dir.x, bot.getZ() + dir.z);
    }

    private void chase(LivingEntity e) {
        if (e == null || !e.isAlive()) {
            nav.stop();
            return;
        }
        double d = bot.distanceTo(e);
        if (d > 2.6D) nav.moveTo(e.blockPosition(), age, true);
        else nav.stop();
        if (d <= 3.2D) {
            bot.lookAt(EntityAnchorArgument.Anchor.EYES, e.getEyePosition());
            if (bot.getAttackStrengthScale(0.5F) >= 1.0F) {
                bot.attack(e);
                bot.swing(InteractionHand.MAIN_HAND, true);
            }
        }
    }

    private void actEat() {
        nav.stop();
        if (--eatTimer > 0) return;
        eatTimer = 32;
        ItemStack food = BotItems.bestFood(bot, bot.getFoodData().getFoodLevel() <= 6);
        if (!food.isEmpty()) bot.eat(bot.level(), food);
    }

    private void actCollect() {
        var item = senses.item;
        if (item == null || !item.isAlive()) {
            collectSince = 0;
            nav.stop();
            return;
        }
        if (collectSince == 0) collectSince = age;
        if (age - collectSince > 160) { // mãi không nhặt được thì bỏ qua món này
            senses.ignoreItem(item, age + 1200);
            collectSince = 0;
            return;
        }
        nav.moveTo(item.blockPosition(), age, false);
    }

    private void actExplore() {
        if (exploreTarget == null) exploreTarget = pickExploreTarget();
        if (exploreTarget == null) {
            nav.stop();
            return;
        }
        if (nav.moveTo(exploreTarget, age, false) || nav.failed) exploreTarget = null;
    }

    private BlockPos pickExploreTarget() {
        var rng = bot.getRandom();
        ServerLevel level = bot.serverLevel();
        for (int i = 0; i < 6; i++) {
            heading += (rng.nextDouble() - 0.5D) * Math.toRadians(120.0D);
            double dist = 18.0D + rng.nextDouble() * 12.0D;
            double x = bot.getX() + Math.cos(heading) * dist;
            double z = bot.getZ() + Math.sin(heading) * dist;
            if (!level.hasChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4)) continue;
            BlockPos p = surface(x, z);
            if (level.getFluidState(p.below()).isEmpty() && level.getFluidState(p).isEmpty()) return p;
        }
        heading += Math.PI; // bí đường thì quay đầu
        return null;
    }

    private BlockPos surface(double x, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        ServerLevel level = bot.serverLevel();
        if (!level.hasChunk(bx >> 4, bz >> 4)) return bot.blockPosition();
        return new BlockPos(bx, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz), bz);
    }

    // ===== Đào / chặt =====

    private void actMine(int kind) {
        BlockPos target = senses.best(kind);
        if (target == null) {
            nav.stop();
            return;
        }
        if (mineTick(target)) {
            senses.refreshNear(bot, 3, age); // chặt tiếp khối kề bên mà không chờ quét lại
        }
    }

    /** Đào target (đi tới, nhìn, đập). Trả true khi xong hoặc phải bỏ. */
    private boolean mineTick(BlockPos target) {
        ServerLevel level = bot.serverLevel();
        BlockState state = level.getBlockState(target);
        if (state.isAir() || !breakable(level, target, state)) {
            senses.ban(target, age + 2400);
            return true;
        }
        Vec3 center = Vec3.atCenterOf(target);
        if (bot.getEyePosition().distanceToSqr(center) > REACH * REACH) {
            boolean arrived = nav.moveTo(target, age, false);
            if (nav.failed || (arrived && ++reachFails >= 3)) {
                senses.ban(target, age + 2400);
                reachFails = 0;
                return true;
            }
            return false;
        }
        nav.stop();

        // Có khối chắn giữa bot và mục tiêu thì đào khối chắn trước
        BlockPos hitPos = target;
        BlockHitResult hit = level.clip(new ClipContext(bot.getEyePosition(), center,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bot));
        if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(target)) {
            hitPos = hit.getBlockPos();
            BlockState obstacle = level.getBlockState(hitPos);
            if (!breakable(level, hitPos, obstacle)) {
                senses.ban(target, age + 2400);
                return true;
            }
            state = obstacle;
        }

        if (!hitPos.equals(mineTarget)) {
            mineTarget = hitPos;
            mineProgress = 0.0F;
            BotItems.equipBest(bot, state);
        }
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(hitPos));
        mineProgress += state.getDestroyProgress(bot, level, hitPos);
        if (age % 4 == 0) bot.swing(InteractionHand.MAIN_HAND, true);
        if (mineProgress >= 1.0F) {
            bot.gameMode.destroyBlock(hitPos);
            level.destroyBlockProgress(bot.getId(), hitPos, -1);
            mineTarget = null;
            mineProgress = 0.0F;
            return hitPos.equals(target);
        }
        level.destroyBlockProgress(bot.getId(), hitPos, (int) (mineProgress * 10.0F));
        return false;
    }

    /** Không phá thứ nguy hiểm hoặc quan trọng: đáy chân, chất lỏng kề, khối có dữ liệu, bedrock... */
    private boolean breakable(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        if (state.hasBlockEntity() || state.is(Blocks.SPAWNER) || !state.getFluidState().isEmpty()) return false;
        if (pos.equals(bot.blockPosition().below())) return false;
        for (Direction d : Direction.values()) {
            if (!level.getBlockState(pos.relative(d)).getFluidState().isEmpty()) return false;
        }
        return true;
    }

    // ===== Trú ẩn ban đêm =====

    private void actShelter() {
        if (isNight()) {
            nav.stop();
            if (shelterPlan.isEmpty()) planShelter();
            if (shelterIdx < shelterPlan.size()) {
                if (age % 3 == 0) placeBlock(shelterPlan.get(shelterIdx++));
            } else {
                sheltered = true; // đứng yên trong hộp tới sáng
            }
            return;
        }
        // Trời sáng: phá lại từng khối đã đặt để thu hồi vật liệu và đi ra
        sheltered = false;
        if (shelterPlan.isEmpty()) {
            shelterDone = true;
            return;
        }
        BlockPos p = shelterPlan.get(shelterPlan.size() - 1);
        BlockState s = bot.level().getBlockState(p);
        if (s.isAir() || !(s.is(Blocks.DIRT) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.COARSE_DIRT))) {
            shelterPlan.remove(shelterPlan.size() - 1);
        } else if (mineTick(p)) {
            shelterPlan.remove(shelterPlan.size() - 1);
        }
        if (shelterPlan.isEmpty()) {
            shelterIdx = 0;
            shelterDone = true;
        }
    }

    /** Hộp 1x1: 4 hướng x 2 tầng + nắp trên đầu = 9 khối. */
    private void planShelter() {
        BlockPos feet = bot.blockPosition();
        shelterPlan.clear();
        shelterIdx = 0;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            shelterPlan.add(feet.relative(d));
            shelterPlan.add(feet.relative(d).above());
        }
        shelterPlan.add(feet.above(2));
    }

    private void placeBlock(BlockPos pos) {
        ServerLevel level = bot.serverLevel();
        if (!level.getBlockState(pos).canBeReplaced()) return;
        ItemStack stack = BotItems.findPlaceable(bot.getInventory());
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return;
        BlockState place = bi.getBlock().defaultBlockState();
        level.setBlock(pos, place, 3);
        level.playSound(null, pos, place.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
        stack.shrink(1);
        bot.swing(InteractionHand.MAIN_HAND, true);
    }
}
