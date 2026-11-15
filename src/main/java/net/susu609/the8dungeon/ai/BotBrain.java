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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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

    private enum Goal { IDLE, FLEE, FIGHT, EAT, HUNT, COLLECT, SHELTER, WOOD, STONE, DIRT, EXPLORE, HOME, COOK, GROUP, SHARE }

    private static final double REACH = 4.0D;

    private final BotPlayer bot;
    private final BotSenses senses = new BotSenses();
    private final BotNav nav;
    private final BotSocial social;
    private LivingEntity fightTarget; // quái thù địch, hoặc người/bot đang bị coi là thù
    // Sát thương gần nhất: ai/cái gì gây ra, ở đâu, loại nguy hiểm môi trường hay không
    private LivingEntity lastAttacker;
    private Vec3 lastDamagePos;
    private boolean lastDamageHazard;
    private int lastDamageAt = -100000;
    // Quay đầu nhìn (về phía âm thanh, kẻ đánh lén, hoặc nhìn quanh khi rảnh)
    private float glanceYaw;
    private int glanceUntil;
    // Làm hòa giữa trận với bot khác
    private int fightSince = -1;
    private int nextPeaceOffer;

    private BotRules rules = new BotRules();
    private int age;
    private boolean rethink;
    private int cannotFleeUntil; // không chạy được thì đánh, tránh đổi qua lại chạy/đánh
    private int mineStartAge;
    private float[] lastScores = new float[Goal.values().length];
    private Goal goal = Goal.IDLE;
    private int goalSince;

    // Ảnh chụp kho đồ (cập nhật mỗi lần suy nghĩ)
    private int logs, planks, cobble, placeable, foodCount, pickRank, rawFood;
    private BlockPos furnacePos; // lò bot đã đặt (nhớ để quay lại lấy đồ chín)

    // Trạng thái con của từng mục tiêu
    private BlockPos mineTarget;
    private float mineProgress;
    private int reachFails;
    private int eatTimer = 32;
    private BlockPos fleeTarget;
    private BlockPos exploreTarget;
    private double heading = Math.random() * Math.PI * 2.0D;
    private int collectSince;
    private ShelterPlan shelter;      // hầm đang dùng (riêng hoặc chung với nhóm)
    private BlockPos myCell;          // ô đứng của mình trong hầm
    private boolean shelterAbandoned; // hết vật liệu vá / bị đánh lúc máu thấp: bỏ hầm tới sáng
    private int shelterStuckSince = -1;
    // Hợp tác
    private final List<net.minecraft.world.entity.player.Player> allies = new ArrayList<>();
    private net.minecraft.world.entity.player.Player nearestAlly;
    private double nearestAllyDist = Double.MAX_VALUE;
    private Vec3 groupCenter;
    private LivingEntity helpTarget;  // kẻ đang tấn công đồng đội
    private int helpUntil;
    private net.minecraft.world.entity.player.Player shareWith;
    private ItemStack shareKind = ItemStack.EMPTY; // loại đồ định cho
    private final java.util.Map<java.util.UUID, Integer> shareCooldown = new java.util.HashMap<>();
    private boolean sheltered;
    private boolean shelterDone;

    BotBrain(BotPlayer bot) {
        this.bot = bot;
        this.nav = new BotNav(bot);
        this.social = new BotSocial(bot);
    }

    /** Mọi loại sát thương: ghi lại nguồn để phản ứng ở lần suy nghĩ ngay sau. */
    void onDamaged(net.minecraft.world.damagesource.DamageSource src) {
        lastDamageAt = age;
        lastDamageHazard = src.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                || src.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                || src.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)
                || src.is(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH)
                || src.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR);
        Vec3 at = src.getSourcePosition();
        lastDamagePos = at != null ? at : bot.position();
        lastAttacker = src.getEntity() instanceof LivingEntity le && le != bot ? le : null;
        if (lastAttacker != null) {
            senses.remember(lastAttacker, age);  // bị đánh lén: giờ đã biết nó ở đâu
            glanceAt(lastAttacker.position(), 30);
        }
        final String what = src.getMsgId();
        BotLog.log(bot, "HURT", () -> what + (lastAttacker != null ? " từ " + lastAttacker.getType().getDescriptionId() : "")
                + (lastDamageHazard ? " (môi trường nguy hiểm)" : ""));
        rethink = true;
    }

    /** Nghe một âm thanh (do BotManager lọc từ sự kiện âm thanh của server). */
    void onSound(Vec3 pos, int kind) {
        senses.hear(pos, kind, age);
        if (kind == BotSenses.SOUND_DANGER) rethink = true;
    }

    /** Đồng đội bị tấn công gần đây (BotManager báo). Giúp nếu nạn nhân là đồng đội và kẻ đánh không phải đồng đội. */
    void onAllyAttacked(net.minecraft.world.entity.player.Player victim, LivingEntity attacker) {
        if (!rules.coop || !social.isAlly(victim)) return;
        if (attacker instanceof net.minecraft.world.entity.player.Player ap && (ap == bot || social.isAlly(ap))) return;
        helpTarget = attacker;
        helpUntil = age + 200;
        senses.remember(attacker, age);
        BotLog.log(bot, "COOP", () -> victim.getGameProfile().getName() + " bị " + attacker.getType().getDescriptionId()
                + " tấn công, đi giúp");
        rethink = true;
    }

    boolean acceptPeaceInFight(BotPlayer from) {
        boolean ok = social.acceptPeaceInFight(from, bot.getHealth() / bot.getMaxHealth(), age);
        if (ok) rethink = true;
        return ok;
    }

    private void glanceAt(Vec3 pos, int ticks) {
        double dx = pos.x - bot.getX();
        double dz = pos.z - bot.getZ();
        glanceYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        glanceUntil = age + ticks;
    }

    void onHurtBy(net.minecraft.world.entity.player.Player attacker, boolean accidental) {
        social.onHurtBy(attacker, accidental, age);
        rethink = true; // phản ứng ngay tick sau
    }

    void onGift(net.minecraft.world.entity.player.Player giver, int count) {
        social.onGift(giver, count, age);
    }

    boolean acceptPeace(BotPlayer from) {
        return social.acceptPeace(from, age);
    }

    int forgive(java.util.UUID target) {
        return social.forgive(target, age);
    }

    String socialStatus() {
        return social.describe();
    }

    String status() {
        return goal + " hp=" + (int) bot.getHealth() + " food=" + bot.getFoodData().getFoodLevel()
                + " gỗ=" + (logs + planks) + " đá=" + cobble + " cuốc=" + pickRank
                + (sheltered ? " [trú ẩn]" : "");
    }

    BotInfo info(String name) {
        return new BotInfo(name, goal.name(), bot.getHealth(), bot.getFoodData().getFoodLevel(),
                logs + planks / 4, cobble, pickRank, sheltered, senses.hostile != null);
    }

    // ===== Vòng lặp chính =====

    void tick() {
        age++;
        bot.setJumping(bot.isInWater() || bot.isInLava()); // luôn bơi lên, kể cả khi đang đứng yên đào
        if (age % 5 == 0 && bot.isInWall()) unstick();
        int every = rules.thinkEvery * (BotManager.reduced() ? 2 : 1); // RAM gần đầy thì suy nghĩ thưa hơn
        if (age % every == 0 || rethink) {
            rethink = false;
            think();
        }
        if (rules.pauseWhenAlone && !BotManager.humansOnline()) { // vắng người chơi thật: đứng yên cho nhẹ
            nav.stop();
            return;
        }
        if (age % 200 == 0) {
            BotLog.log(bot, "BEAT", () -> status() + " pos=" + bot.blockPosition().toShortString()
                    + " mục tiêu=" + goal + " đã=" + (age - goalSince) + "tick");
        }
        act();
        // Rảnh thì thỉnh thoảng nhìn quanh (tầm nhìn chỉ có phía trước)
        if (age >= glanceUntil && age % 80 == 0 && goal != Goal.FIGHT && goal != Goal.FLEE) {
            glanceYaw = bot.getYRot() + (bot.getRandom().nextFloat() - 0.5F) * 220.0F;
            glanceUntil = age + 15;
        }
        if (age < glanceUntil) bot.setYHeadRot(glanceYaw); // chỉ quay đầu, thân vẫn đi theo đường
    }

    private boolean inRoam(BlockPos p) {
        boolean roamOk = rules.roam <= 0 || bot.home().distSqr(p) <= (double) rules.roam * rules.roam;
        // Đi cùng nhóm: chỉ chọn việc trong vòng 32 khối quanh tâm nhóm
        boolean groupOk = groupCenter == null || Vec3.atCenterOf(p).distanceToSqr(groupCenter) <= 32.0D * 32.0D;
        return roamOk && groupOk;
    }

    /** Cập nhật danh sách đồng đội trong 64 khối, đồng đội gần nhất và tâm nhóm. */
    private void updateAllies() {
        allies.clear();
        nearestAlly = null;
        nearestAllyDist = Double.MAX_VALUE;
        groupCenter = null;
        if (!rules.coop) return;
        double sx = 0, sy = 0, sz = 0;
        for (net.minecraft.world.entity.player.Player p : bot.serverLevel().players()) {
            if (p == bot || !p.isAlive() || p.isSpectator()) continue;
            double d = p.distanceTo(bot);
            if (d > 64.0D || !social.isAlly(p)) continue;
            allies.add(p);
            sx += p.getX();
            sy += p.getY();
            sz += p.getZ();
            if (d < nearestAllyDist) {
                nearestAllyDist = d;
                nearestAlly = p;
            }
        }
        if (!allies.isEmpty()) {
            // tâm nhóm tính cả bản thân để không bị kéo hẳn về phía người khác
            int n = allies.size() + 1;
            groupCenter = new Vec3((sx + bot.getX()) / n, (sy + bot.getY()) / n, (sz + bot.getZ()) / n);
        }
    }

    /** Tìm đồng đội cần đồ (đói mà hết đồ ăn, hoặc chưa có cuốc) mà mình có dư để cho. */
    private void findShareTarget() {
        shareWith = null;
        shareKind = ItemStack.EMPTY;
        if (!rules.coop) return;
        Inventory inv = bot.getInventory();
        int myPicks = BotItems.count(inv, st -> st.getItem() instanceof PickaxeItem);
        for (net.minecraft.world.entity.player.Player p : allies) {
            if (p.distanceTo(bot) > 24.0D || shareCooldown.getOrDefault(p.getUUID(), -1) > age) continue;
            Inventory theirs = p.getInventory();
            boolean hungry = p.getFoodData().getFoodLevel() <= 10 && BotItems.count(theirs, ItemStack::isEdible) == 0;
            if (hungry && foodCount >= 3) {
                ItemStack food = BotItems.bestFood(bot, false);
                if (!food.isEmpty()) {
                    shareWith = p;
                    shareKind = food;
                    return;
                }
            }
            if (myPicks >= 2 && BotItems.rank(theirs, PickaxeItem.class) == 0) {
                for (int i = 0; i < inv.getContainerSize(); i++) { // cho cái cuốc yếu nhất
                    ItemStack st = inv.getItem(i);
                    if (st.getItem() instanceof PickaxeItem pk && (shareKind.isEmpty()
                            || pk.getTier().getLevel() < ((PickaxeItem) shareKind.getItem()).getTier().getLevel())) {
                        shareKind = st;
                    }
                }
                shareWith = p;
                return;
            }
        }
    }

    private void think() {
        rules = BotRulesStore.get(bot.getServer()).effective(bot.getGameProfile().getName());
        for (int i = 0; i < 6 && BotCrafting.step(bot); i++) {
            // chế liên tục tới khi hết việc
        }
        snapshotInventory();

        updateAllies();
        boolean inShelter = sheltered && isNight() && shelter != null;
        senses.update(bot, age, !inShelter, rules.scanBudget / (BotManager.reduced() ? 2 : 1));

        float hp = bot.getHealth() / bot.getMaxHealth();
        int food = bot.getFoodData().getFoodLevel();
        LivingEntity h = senses.hostile;
        double hd = senses.hostileDist;
        boolean night = isNight();
        boolean dusk = isDusk();
        // Độ trễ: đã chạy trốn thì phải an toàn hơn mới thôi, tránh đổi qua lại ở sát ngưỡng
        boolean fleeing = goal == Goal.FLEE;
        LivingEntity cr = senses.creeper;
        boolean creeper = cr != null && senses.creeperDist < (fleeing ? 9.5D : 6.5D);
        boolean canFlee = age >= cannotFleeUntil;
        ItemStack foodStack = BotItems.bestFood(bot, food <= 6);
        float fleeHp = rules.fleeHpPct / 100.0F;
        boolean lowHp = hp < (fleeing ? fleeHp + 0.15F : fleeHp);

        // Quan hệ: người/bot đang bị coi là thù, hoặc đang tạm lùi xa sau khi bị đánh
        social.tick(age);
        net.minecraft.world.entity.player.Player grudge = social.grudge();
        net.minecraft.world.entity.player.Player avoid = social.avoiding(age);
        // Kẻ thù không gây sát thương được (creative...) thì chỉ né, không đánh
        boolean grudgeUntouchable = grudge != null && (grudge.isCreative() || grudge.isInvulnerable());
        net.minecraft.world.entity.player.Player fleeFrom = avoid != null ? avoid
                : grudge != null && (lowHp || grudgeUntouchable || !rules.fight) ? grudge : null;

        // Thính giác: tiếng nguy hiểm gần (creeper xì, TNT, nổ) -> chạy; tiếng quái -> quay đầu nhìn
        boolean heardDanger = senses.heardKind == BotSenses.SOUND_DANGER && age - senses.heardAt < 40
                && senses.heardPos.distanceToSqr(bot.position()) < 10.0D * 10.0D;
        if (senses.heardKind == BotSenses.SOUND_ALERT && age - senses.heardAt < 20 && age >= glanceUntil
                && goal != Goal.FIGHT) {
            glanceAt(senses.heardPos, 25);
        }
        // Sát thương gần đây: môi trường (lửa, dung nham, xương rồng, nổ) -> nhảy ra chỗ an toàn;
        // bị quái đánh -> đánh trả (máu thấp thì chạy)
        boolean recentHurt = age - lastDamageAt < 30;
        boolean hazard = recentHurt && lastDamageHazard;
        LivingEntity revenge = recentHurt && lastAttacker != null && lastAttacker.isAlive()
                && !(lastAttacker instanceof net.minecraft.world.entity.player.Player) ? lastAttacker : null;

        LivingEntity threat = creeper ? cr : fleeFrom != null ? fleeFrom : revenge != null && lowHp ? revenge : h;
        double threatDist = threat == null ? 0.0D : threat.distanceTo(bot);
        Vec3 dangerFrom = hazard ? lastDamagePos : heardDanger ? senses.heardPos
                : threat != null ? threat.position() : null;
        LivingEntity help = helpTarget != null && helpTarget.isAlive() && age < helpUntil
                && helpTarget.distanceTo(bot) < 20.0D ? helpTarget : null;
        fightTarget = grudge != null && fleeFrom == null ? grudge : revenge != null ? revenge : help != null ? help : h;

        float[] sc = new float[Goal.values().length];
        if (hazard || heardDanger) {
            sc[Goal.FLEE.ordinal()] = 0.98F; // nguy hiểm môi trường/âm thanh: ưu tiên tuyệt đối (kể cả trong chỗ trú)
        }
        if (inShelter) {
            // Đang trong hầm: kẻ đứng sát (đánh qua lỗ) thì đánh trả tại chỗ; bị đánh lúc máu thấp thì bỏ hầm chạy
            LivingEntity close = revenge != null ? revenge : h;
            if (recentHurt && lowHp) {
                abandonShelter("bị đánh lúc máu thấp");
                sc[Goal.FLEE.ordinal()] = 0.95F;
            } else if (rules.fight && close != null && close.distanceTo(bot) < 3.5D) {
                fightTarget = close;
                sc[Goal.FIGHT.ordinal()] = 0.9F;
            }
        }
        if (!inShelter) {
            if (canFlee && (creeper || fleeFrom != null || ((h != null || revenge != null) && lowHp))) {
                sc[Goal.FLEE.ordinal()] = Math.max(sc[Goal.FLEE.ordinal()], 0.95F);
            } else if (rules.fight && revenge != null) {
                sc[Goal.FIGHT.ordinal()] = 0.88F; // bị quái đánh (kể cả từ sau lưng): quay lại đánh
            } else if (rules.fight && help != null && !lowHp) {
                sc[Goal.FIGHT.ordinal()] = 0.86F; // đồng đội bị tấn công: đánh giúp
            } else if (rules.fight && grudge != null && fleeFrom == null) {
                sc[Goal.FIGHT.ordinal()] = 0.9F; // đánh trả người/bot đã thành thù
            } else if (rules.fight && h != null && hd <= 16.0D) {
                sc[Goal.FIGHT.ordinal()] = hd < 6.0D ? 0.85F : 0.6F;
            }
        }
        if (!foodStack.isEmpty() && (food <= rules.eatBelow || (hp < 0.7F && food < 20)) && (h == null || hd > 5.0D || inShelter)) {
            sc[Goal.EAT.ordinal()] = food <= 6 ? 1.0F : 0.8F;
        }
        if (!inShelter) {
            if (rules.hunt && senses.prey != null && inRoam(senses.prey.blockPosition()) && foodCount < 2 && food <= 16) {
                sc[Goal.HUNT.ordinal()] = 0.6F + (20 - food) / 80.0F;
            }
            if (rules.collect && senses.item != null && inRoam(senses.item.blockPosition())) sc[Goal.COLLECT.ordinal()] = 0.65F;
            if (rules.mine) {
                BlockPos log = senses.best(BotSenses.LOG);
                BlockPos stone = senses.best(BotSenses.STONE);
                BlockPos dirt = senses.best(BotSenses.DIRT);
                boolean needWood = logs * 4 + planks < 20 || pickRank == 0;
                if (log != null && inRoam(log) && needWood) sc[Goal.WOOD.ordinal()] = pickRank == 0 ? 0.65F : 0.5F;
                boolean shortOnBlocks = placeable < 9 && (dusk || night);
                if (pickRank >= 1 && stone != null && inRoam(stone) && (cobble < 24 || shortOnBlocks)) {
                    sc[Goal.STONE.ordinal()] = shortOnBlocks ? 0.75F : 0.45F;
                }
                if (shortOnBlocks && dirt != null && inRoam(dirt)) sc[Goal.DIRT.ordinal()] = 0.7F;
            }
            if (rules.explore) sc[Goal.EXPLORE.ordinal()] = 0.2F;
            if (rules.cook && h == null) {
                int fs = furnaceState();
                boolean spareWood = logs * 4 + planks > 12;
                boolean hasFuel = !BotItems.findFuel(bot.getInventory(), spareWood).isEmpty() || fs == FURNACE_BUSY;
                boolean canGetFurnace = furnacePos != null || cobble >= 8;
                if (fs == FURNACE_READY) sc[Goal.COOK.ordinal()] = 0.6F;                 // đồ đã chín: đi lấy
                else if (rawFood > 0 && hasFuel && canGetFurnace) sc[Goal.COOK.ordinal()] = 0.5F; // bỏ đồ sống vào lò
                else if (fs == FURNACE_BUSY) sc[Goal.COOK.ordinal()] = 0.3F;             // đang nung: chờ gần lò
            }
            if (rules.roam > 0) { // ra xa quá bán kính thì quay về, có độ trễ để khỏi đổi qua lại
                double d2 = bot.home().distSqr(bot.blockPosition());
                double lim = goal == Goal.HOME ? rules.roam * 0.6D : rules.roam;
                if (d2 > lim * lim) sc[Goal.HOME.ordinal()] = 0.7F;
            }
        }
        boolean groupShelter = rules.coop && BotManager.sharedShelterNear(bot) != null;
        boolean canShelter = shelter != null || placeable >= 9 || groupShelter || (allyBotNear(16.0D) && placeable >= 6);
        if (rules.shelter && night && !shelterDone && !shelterAbandoned && canShelter) sc[Goal.SHELTER.ordinal()] = 0.8F;
        else if (!night && shelter != null) sc[Goal.SHELTER.ordinal()] = 0.9F; // phải mở hầm để ra
        if (!night && !dusk && shelter == null) {
            shelterDone = false;
            shelterAbandoned = false;
        }
        // Hợp tác: xa nhóm quá thì quay về; đồng đội thiếu đồ thì mang tới cho
        if (!inShelter && rules.coop) {
            double lim = goal == Goal.GROUP ? 12.0D : 24.0D; // độ trễ: về tới 12 khối mới thôi
            if (nearestAlly != null && nearestAllyDist > lim) sc[Goal.GROUP.ordinal()] = 0.55F;
            if (age % 100 == 0 || goal == Goal.SHARE) findShareTarget();
            if (shareWith != null && h == null) {
                // đồng đội sắp chết đói thì ưu tiên hơn cả việc trú ẩn
                sc[Goal.SHARE.ordinal()] = shareWith.getFoodData().getFoodLevel() <= 6 ? 0.85F : 0.7F;
            }
        }

        lastScores = sc;
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
        if (goal == Goal.FLEE && fleeTarget == null && dangerFrom != null) {
            final String why = hazard ? "môi trường nguy hiểm" : heardDanger ? "âm thanh nguy hiểm"
                    : threat.getType().getDescriptionId() + " cách " + (int) threatDist;
            fleeTarget = hazard ? safeSpotNear() : awayFrom(dangerFrom);
            if (fleeTarget == null && !hazard) { // không có đường lùi (hang cụt...): quay lại đánh
                cannotFleeUntil = age + 100;
                BotLog.log(bot, "FLEE", () -> "không có chỗ để chạy khỏi " + why + ", chuyển sang đánh");
                switchGoal(Goal.FIGHT);
            } else if (fleeTarget != null) {
                BotLog.log(bot, "FLEE", () -> "tránh " + why + " -> " + fleeTarget.toShortString());
            }
        }

        // Đang đánh nhau (hoặc chạy) với một bot khác: đánh lâu hoặc mất nửa máu thì đề nghị làm hòa
        BotPlayer foe = goal == Goal.FIGHT && fightTarget instanceof BotPlayer b ? b
                : goal == Goal.FLEE && threat instanceof BotPlayer b2 ? b2 : null;
        if (foe == null) {
            fightSince = -1;
        } else {
            if (fightSince < 0) fightSince = age;
            if ((hp < 0.5F || age - fightSince > 300) && age >= nextPeaceOffer) {
                nextPeaceOffer = age + 200;
                if (social.offerPeaceInFight(foe, age)) {
                    fightSince = -1;
                    rethink = true;
                }
            }
        }
    }

    private void switchGoal(Goal next) {
        final Goal from = goal;
        final int spent = age - goalSince;
        BotLog.log(bot, "GOAL", () -> from + " -> " + next + " (sau " + spent + " tick) điểm=" + scores());
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

    private String scores() {
        StringBuilder sb = new StringBuilder("{");
        for (Goal g : Goal.values()) {
            float v = lastScores[g.ordinal()];
            if (v > 0.0F) sb.append(g).append('=').append(String.format("%.2f", v)).append(' ');
        }
        return sb.append('}').toString();
    }

    private void snapshotInventory() {
        Inventory inv = bot.getInventory();
        logs = BotItems.count(inv, s -> s.is(ItemTags.LOGS));
        planks = BotItems.count(inv, s -> s.is(ItemTags.PLANKS));
        cobble = BotItems.count(inv, s -> s.is(Items.COBBLESTONE));
        placeable = BotItems.count(inv, BotItems::isPlaceable);
        foodCount = BotItems.count(inv, s -> s.isEdible() && !s.is(Items.ROTTEN_FLESH) && !s.is(Items.SPIDER_EYE));
        pickRank = BotItems.rank(inv, PickaxeItem.class);
        ServerLevel lvl = bot.serverLevel();
        rawFood = BotItems.count(inv, st -> BotItems.isRawFood(lvl, st));
    }

    private boolean isNight() {
        long t = bot.level().getDayTime() % 24000L;
        return t >= 12700L && t < 23000L;
    }

    private boolean isDusk() {
        long t = bot.level().getDayTime() % 24000L;
        return t >= 11000L && t < 12700L;
    }

    /** Cấp cứu: đầu/chân kẹt trong khối đặc (cát rơi, khối đặt nhầm...) thì phá ngay khối đó. */
    private void unstick() {
        ServerLevel level = bot.serverLevel();
        BlockPos feet = bot.blockPosition();
        BlockPos eye = BlockPos.containing(bot.getEyePosition());
        for (BlockPos p : new BlockPos[] {eye, feet}) {
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && s.getDestroySpeed(level, p) >= 0.0F && s.isSuffocating(level, p)) {
                BotLog.log(bot, "UNSTICK", () -> "kẹt trong " + s.getBlock().getDescriptionId() + " tại " + p.toShortString() + ", phá ra");
                level.destroyBlock(p, true, bot);
            }
        }
    }

    // ===== Hành động =====

    private void act() {
        switch (goal) {
            case FLEE -> actFlee();
            case FIGHT -> chase(fightTarget);
            case HUNT -> chase(senses.prey);
            case EAT -> actEat();
            case COLLECT -> actCollect();
            case SHELTER -> actShelter();
            case WOOD -> actMine(BotSenses.LOG);
            case STONE -> actMine(BotSenses.STONE);
            case DIRT -> actMine(BotSenses.DIRT);
            case EXPLORE -> actExplore();
            case COOK -> actCook();
            case GROUP -> actGroup();
            case SHARE -> actShare();
            case HOME -> nav.moveTo(bot.home(), age, false);
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

    /**
     * Chỗ để chạy: thử nhiều hướng quanh hướng ngược với mối đe dọa, ở cùng độ cao với bot
     * (không nhảy lên mặt đất khi bot đang ở dưới hang). Trả null nếu không có chỗ nào xa hơn.
     */
    private BlockPos awayFrom(Vec3 danger) {
        Vec3 dir = bot.position().subtract(danger).multiply(1.0D, 0.0D, 1.0D);
        if (dir.lengthSqr() < 1.0E-4D) dir = new Vec3(1.0D, 0.0D, 0.0D);
        double base = Math.atan2(dir.z, dir.x);
        double[] turns = {0.0D, 0.7D, -0.7D, 1.4D, -1.4D};
        double[] dists = {14.0D, 10.0D, 6.0D};
        BlockPos here = bot.blockPosition();
        BlockPos from = BlockPos.containing(danger);
        for (double d : dists) {
            for (double t : turns) {
                int x = Mth.floor(bot.getX() + Math.cos(base + t) * d);
                int z = Mth.floor(bot.getZ() + Math.sin(base + t) * d);
                BlockPos p = standable(x, here.getY(), z);
                if (p != null && p.distSqr(from) > here.distSqr(from)) return p;
            }
        }
        return null;
    }

    /** Chỗ đứng an toàn gần nhất (trong 4 khối): không lửa, không dung nham, không xương rồng/bụi gai, sàn không nóng. */
    private BlockPos safeSpotNear() {
        ServerLevel level = bot.serverLevel();
        BlockPos here = bot.blockPosition();
        for (int r = 1; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue; // chỉ xét vòng ngoài của bán kính r
                    BlockPos p = standable(here.getX() + dx, here.getY(), here.getZ() + dz);
                    if (p != null && safe(level, p)) return p;
                }
            }
        }
        return null;
    }

    private static boolean safe(ServerLevel level, BlockPos p) {
        for (BlockPos q : new BlockPos[] {p, p.above(), p.below()}) {
            BlockState s = level.getBlockState(q);
            if (s.is(net.minecraft.tags.BlockTags.FIRE) || s.is(Blocks.LAVA) || s.is(Blocks.MAGMA_BLOCK)
                    || s.is(Blocks.CACTUS) || s.is(Blocks.SWEET_BERRY_BUSH) || s.is(Blocks.CAMPFIRE)) return false;
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(p.relative(d)).is(Blocks.CACTUS)) return false;
        }
        return true;
    }

    /** Ô đứng được gần độ cao nearY: chân và đầu trống, không chất lỏng, dưới chân là khối đặc. */
    private BlockPos standable(int x, int nearY, int z) {
        ServerLevel level = bot.serverLevel();
        if (!level.hasChunk(x >> 4, z >> 4)) return null;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy : new int[] {0, -1, 1, -2, 2, -3, 3}) {
            m.set(x, nearY + dy, z);
            BlockPos head = m.above();
            BlockPos floor = m.below();
            if (level.getBlockState(m).getCollisionShape(level, m).isEmpty()
                    && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                    && level.getFluidState(m).isEmpty()
                    && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                return m.immutable();
            }
        }
        return null;
    }

    private void chase(LivingEntity e) {
        if (e == null || !e.isAlive()) {
            nav.stop();
            return;
        }
        double d = bot.distanceTo(e);
        boolean holdPosition = sheltered && shelter != null && isNight(); // trong hầm: đánh tại chỗ, không chạy ra
        if (d > 2.6D && !holdPosition) nav.moveTo(e.blockPosition(), age, true);
        else nav.stop();
        if (d <= 3.2D) {
            bot.lookAt(EntityAnchorArgument.Anchor.EYES, e.getEyePosition());
            if (bot.getAttackStrengthScale(0.5F) >= 1.0F) {
                bot.attack(e);
                BotLog.log(bot, "ATTACK", () -> e.getType().getDescriptionId() + " hp=" + (int) e.getHealth());
                bot.swing(InteractionHand.MAIN_HAND, true);
            }
        }
    }

    private void actEat() {
        nav.stop();
        if (--eatTimer > 0) return;
        eatTimer = 32;
        ItemStack food = BotItems.bestFood(bot, bot.getFoodData().getFoodLevel() <= 6);
        if (!food.isEmpty()) {
            BotLog.log(bot, "EAT", () -> food.getItem().getDescriptionId() + " (đói " + bot.getFoodData().getFoodLevel() + ")");
            bot.eat(bot.level(), food);
        }
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
            BotLog.log(bot, "COLLECT", () -> "bỏ qua món không nhặt được " + item.getItem().getItem().getDescriptionId());
            senses.ignoreItem(item, age + 1200);
            collectSince = 0;
            return;
        }
        nav.moveTo(item.blockPosition(), age, false);
        if (nav.deadEnd) { // đường cụt: không cần chờ hết 160 tick mới bỏ
            BotLog.log(bot, "COLLECT", () -> "bỏ qua sớm (đường cụt) " + item.getItem().getItem().getDescriptionId()
                    + " tại " + item.blockPosition().toShortString());
            senses.ignoreItem(item, age + 1200);
            collectSince = 0;
            nav.stop();
            rethink = true; // chọn lại mục tiêu ngay tick sau
        }
    }

    private void actExplore() {
        if (exploreTarget == null) {
            exploreTarget = pickExploreTarget();
            BotLog.log(bot, "EXPLORE", () -> "đích mới " + (exploreTarget == null ? "không có (quay đầu)" : exploreTarget.toShortString()));
        }
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
            if (inRoam(p) && level.getFluidState(p.below()).isEmpty() && level.getFluidState(p).isEmpty()) return p;
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
            final BlockState seen = state;
            BotLog.log(bot, "BAN", () -> target.toShortString() + " không phá được/đã mất (" + seen.getBlock().getDescriptionId() + ")");
            senses.ban(target, age + 2400);
            return true;
        }
        Vec3 center = Vec3.atCenterOf(target);
        if (bot.getEyePosition().distanceToSqr(center) > REACH * REACH) {
            boolean arrived = nav.moveTo(target, age, false);
            if (nav.failed || (arrived && ++reachFails >= 3)) {
                BotLog.log(bot, "BAN", () -> target.toShortString() + " không tới được (navFail=" + nav.failed + ")");
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
                final BlockPos blocked = hitPos;
                BotLog.log(bot, "BAN", () -> target.toShortString() + " bị chắn bởi khối không phá được " + blocked.toShortString());
                senses.ban(target, age + 2400);
                return true;
            }
            state = obstacle;
        }

        if (!hitPos.equals(mineTarget)) {
            mineTarget = hitPos;
            mineStartAge = age;
            final BlockPos startPos = hitPos;
            final BlockState startState = state;
            BotLog.log(bot, "MINE_START", () -> startState.getBlock().getDescriptionId() + " tại " + startPos.toShortString()
                    + (startPos.equals(target) ? "" : " (chắn đường tới " + target.toShortString() + ")"));
            mineProgress = 0.0F;
            BotItems.equipBest(bot, state);
        }
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(hitPos));
        mineProgress += state.getDestroyProgress(bot, level, hitPos);
        if (age % 4 == 0) bot.swing(InteractionHand.MAIN_HAND, true);
        if (mineProgress >= 1.0F) {
            final BlockPos donePos = hitPos;
            final BlockState doneState = state;
            BotLog.log(bot, "MINE_DONE", () -> doneState.getBlock().getDescriptionId() + " tại " + donePos.toShortString()
                    + " mất " + (age - mineStartAge) + " tick");
            bot.gameMode.destroyBlock(hitPos);
            level.destroyBlockProgress(bot.getId(), hitPos, -1);
            mineTarget = null;
            mineProgress = 0.0F;
            return hitPos.equals(target);
        }
        level.destroyBlockProgress(bot.getId(), hitPos, (int) (mineProgress * 10.0F));
        return false;
    }

    // ===== Nấu ăn bằng lò =====

    private static final int FURNACE_NONE = 0, FURNACE_IDLE = 1, FURNACE_BUSY = 2, FURNACE_READY = 3;

    /** Trạng thái lò đã đặt; quên lò nếu bị phá hoặc ở quá xa (> 64 khối). */
    private int furnaceState() {
        if (furnacePos == null) return FURNACE_NONE;
        ServerLevel level = bot.serverLevel();
        if (!level.isLoaded(furnacePos) || furnacePos.distSqr(bot.blockPosition()) > 64 * 64) {
            furnacePos = null;
            return FURNACE_NONE;
        }
        if (!(level.getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity f)) {
            furnacePos = null;
            return FURNACE_NONE;
        }
        if (!f.getItem(2).isEmpty()) return FURNACE_READY;
        if (!f.getItem(0).isEmpty()) return FURNACE_BUSY;
        return FURNACE_IDLE;
    }

    private void actCook() {
        ServerLevel level = bot.serverLevel();
        if (furnacePos == null && !placeFurnace()) {
            rethink = true;
            return;
        }
        Vec3 center = Vec3.atCenterOf(furnacePos);
        if (bot.getEyePosition().distanceToSqr(center) > REACH * REACH) {
            nav.moveTo(furnacePos, age, false);
            if (nav.failed) {
                BotLog.log(bot, "COOK", () -> "không tới được lò " + furnacePos.toShortString() + ", bỏ lò này");
                furnacePos = null;
            }
            return;
        }
        nav.stop();
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, center);
        if (age % 10 != 0) return; // thao tác lò 2 lần/giây là đủ
        if (!(level.getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity f)) {
            furnacePos = null;
            return;
        }
        Inventory inv = bot.getInventory();
        boolean changed = false;

        // 1) Lấy đồ đã chín
        ItemStack out = f.getItem(2);
        if (!out.isEmpty()) {
            final int count = out.getCount();
            final String what = out.getItem().getDescriptionId();
            BotItems.give(bot, out.copy()); // give() chuyển hẳn stack vào túi, nên ghi log từ số đã lưu
            f.setItem(2, ItemStack.EMPTY);
            BotLog.log(bot, "COOK", () -> "lấy " + count + " " + what);
            changed = true;
        }
        // 2) Bỏ đồ sống vào ô nguyên liệu (cùng loại với đồ đang nung nếu có)
        ItemStack in = f.getItem(0);
        if (in.isEmpty() || in.getCount() < in.getMaxStackSize()) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack st = inv.getItem(i);
                if (!BotItems.isRawFood(level, st) || (!in.isEmpty() && !ItemStack.isSameItemSameTags(st, in))) continue;
                int move = in.isEmpty() ? st.getCount() : Math.min(st.getCount(), in.getMaxStackSize() - in.getCount());
                if (in.isEmpty()) {
                    f.setItem(0, st.split(move));
                } else {
                    in.grow(move);
                    st.shrink(move);
                }
                final int moved = move;
                final ItemStack kind = f.getItem(0);
                BotLog.log(bot, "COOK", () -> "bỏ " + moved + " " + kind.getItem().getDescriptionId() + " vào lò");
                changed = true;
                break;
            }
        }
        // 3) Thêm nhiên liệu vừa đủ cho số đồ trong lò
        ItemStack inNow = f.getItem(0);
        boolean lit = level.getBlockState(furnacePos).getValue(AbstractFurnaceBlock.LIT);
        if (f.getItem(1).isEmpty() && !inNow.isEmpty() && !lit) { // đang cháy thì chưa thêm, tránh phí than
            ItemStack fuel = BotItems.findFuel(inv, logs * 4 + planks > 12);
            float per = BotItems.smeltsPerFuel(fuel);
            if (per > 0.0F) {
                int need = Math.min(fuel.getCount(), Math.max(1, (int) Math.ceil(inNow.getCount() / per)));
                final ItemStack used = fuel.split(need);
                f.setItem(1, used);
                BotLog.log(bot, "COOK", () -> "thêm " + used.getCount() + " " + used.getItem().getDescriptionId() + " làm nhiên liệu");
                changed = true;
            }
        }
        if (changed) {
            f.setChanged();
            bot.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    /** Đặt lò cạnh chân bot: dùng lò có sẵn trong túi, không thì chế từ 8 đá cuội. */
    private boolean placeFurnace() {
        ServerLevel level = bot.serverLevel();
        Inventory inv = bot.getInventory();
        boolean hasItem = BotItems.count(inv, st -> st.is(Items.FURNACE)) > 0;
        if (!hasItem && cobble < 8) return false;
        BlockPos feet = bot.blockPosition();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos p = feet.relative(d);
            BlockPos below = p.below();
            if (!level.getBlockState(p).canBeReplaced() || !level.getFluidState(p).isEmpty()) continue;
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (bot.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(p))) continue;
            if (hasItem) BotItems.take(inv, st -> st.is(Items.FURNACE), 1);
            else BotItems.take(inv, st -> st.is(Items.COBBLESTONE), 8);
            BlockState furnace = Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, d.getOpposite());
            level.setBlock(p, furnace, 3);
            level.playSound(null, p, furnace.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
            bot.swing(InteractionHand.MAIN_HAND, true);
            furnacePos = p.immutable();
            final boolean crafted = !hasItem;
            BotLog.log(bot, "COOK", () -> (crafted ? "chế lò từ 8 đá cuội, " : "") + "đặt lò tại " + furnacePos.toShortString());
            return true;
        }
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

    private boolean allyBotNear(double range) {
        for (net.minecraft.world.entity.player.Player p : allies) {
            if (p instanceof BotPlayer && p.distanceTo(bot) <= range) return true;
        }
        return false;
    }

    private void abandonShelter(String why) {
        if (shelter == null || shelterAbandoned) return;
        BotLog.log(bot, "SHELTER", () -> "bỏ hầm: " + why);
        shelterAbandoned = true;
        sheltered = false;
        shelter = null;
        myCell = null;
    }

    private void actShelter() {
        ServerLevel level = bot.serverLevel();
        String name = bot.getGameProfile().getName();
        if (isNight()) {
            if (shelter == null) {
                shelter = BotManager.shelterFor(bot, rules.coop && allyBotNear(16.0D));
                myCell = shelter.cellFor(name);
                final ShelterPlan sp = shelter;
                BotLog.log(bot, "SHELTER", () -> (sp.shared ? "vào hầm nhóm tại " : "dựng hầm riêng tại ")
                        + sp.origin.toShortString() + ", ô " + myCell.toShortString());
            }
            // Về đúng ô của mình (có thể bị đẩy ra khi đánh nhau)
            if (!bot.blockPosition().equals(myCell)) {
                sheltered = false;
                boolean arrived = nav.moveTo(myCell, age, false);
                if ((arrived || nav.failed) && bot.blockPosition().distSqr(myCell) > 2) {
                    if (shelterStuckSince < 0) shelterStuckSince = age;
                    if (age - shelterStuckSince > 100) abandonShelter("không vào được ô của mình");
                }
                return;
            }
            shelterStuckSince = -1;
            nav.stop();
            // Kiểm tra vỏ hầm: thiếu/bị phá thì vá (cả nhóm cùng vá)
            BlockPos missing = shelter.nextMissing(level, bot, 4.5D);
            if (missing != null) {
                sheltered = false;
                if (age % 3 == 0 && !placeBlock(missing)) {
                    // hết đất/đá: hầm nhóm thì chờ đồng đội vá; hầm riêng thì bỏ
                    if (!shelter.shared || !allyBotNear(4.0D)) abandonShelter("hết vật liệu để vá");
                }
                return;
            }
            if (!sheltered && shelter.complete(level)) {
                sheltered = true;
                BotLog.log(bot, "SHELTER", () -> "hầm đã kín");
            } else if (sheltered && age % 20 == 0 && !shelter.complete(level)) {
                sheltered = false; // có lỗ ngoài tầm với (hoặc đang có sinh vật chắn): chờ vá
                BotLog.log(bot, "SHELTER", () -> "hầm bị thủng");
            }
            return;
        }
        // Trời sáng: mở lối ra (hầm riêng: phá hết để lấy lại vật liệu; hầm nhóm: một lối cạnh ô mình)
        sheltered = false;
        if (shelter == null) {
            shelterDone = true;
            return;
        }
        for (BlockPos p : shelter.exitFor(myCell)) {
            BlockState st = level.getBlockState(p);
            if (st.is(Blocks.DIRT) || st.is(Blocks.COBBLESTONE) || st.is(Blocks.COARSE_DIRT)) {
                mineTick(p);
                return;
            }
        }
        BotLog.log(bot, "SHELTER", () -> "đã mở hầm, ra ngoài");
        shelter = null;
        myCell = null;
        shelterDone = true;
    }

    // ===== Hợp tác =====

    private void actGroup() {
        if (nearestAlly == null) {
            nav.stop();
            return;
        }
        nav.moveTo(nearestAlly.blockPosition(), age, nearestAllyDist > 32.0D);
    }

    private void actShare() {
        net.minecraft.world.entity.player.Player to = shareWith;
        if (to == null || !to.isAlive() || shareKind.isEmpty()) {
            nav.stop();
            return;
        }
        if (bot.distanceTo(to) > 3.0D) {
            nav.moveTo(to.blockPosition(), age, false);
            return;
        }
        nav.stop();
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, to.getEyePosition());
        Inventory inv = bot.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty() || !ItemStack.isSameItem(st, shareKind)) continue;
            ItemStack give = st.split(st.isEdible() ? Math.min(2, st.getCount()) : 1);
            final String what = give.getCount() + " " + give.getItem().getDescriptionId();
            bot.drop(give, false, true); // ném về phía đang nhìn, ghi tên người ném để người nhận biết là quà
            BotLog.log(bot, "COOP", () -> "cho " + to.getGameProfile().getName() + " " + what);
            break;
        }
        shareCooldown.put(to.getUUID(), age + 1200);
        shareWith = null;
        shareKind = ItemStack.EMPTY;
        rethink = true;
    }

    /** Đặt một khối đất/đá. Trả false nếu hết vật liệu. */
    private boolean placeBlock(BlockPos pos) {
        ServerLevel level = bot.serverLevel();
        if (!level.getBlockState(pos).canBeReplaced()) return true;
        if (!level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(pos)).isEmpty()) {
            return true; // có sinh vật (kể cả chính mình) đứng ở đó: không chôn ai trong khối
        }
        ItemStack stack = BotItems.findPlaceable(bot.getInventory());
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return false;
        BlockState place = bi.getBlock().defaultBlockState();
        level.setBlock(pos, place, 3);
        BotLog.log(bot, "PLACE", () -> place.getBlock().getDescriptionId() + " tại " + pos.toShortString());
        level.playSound(null, pos, place.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
        stack.shrink(1);
        bot.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }
}
