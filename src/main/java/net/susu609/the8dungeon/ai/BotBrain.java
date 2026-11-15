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

    private enum Goal { IDLE, FLEE, FIGHT, EAT, HUNT, COLLECT, SHELTER, WOOD, STONE, DIRT, EXPLORE, HOME, COOK, GROUP, SHARE, SLEEP, CRAFT, BUILD, BASE, STAY, FOLLOW, SCOUT, FARM, MINE }

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
    private BlockPos tableUse;   // bàn chế tạo đang dùng
    private boolean tableTemp;   // bàn tạm mình vừa đặt: chế xong thì thu lại
    private boolean wantFurnace;
    private int craftBlockedUntil; // không có chỗ đặt bàn: tạm bỏ việc chế
    // Lệnh từ chat ("su đứng lại", "su chặt gỗ"...)
    private Goal order;               // việc được giao (STAY/FOLLOW hoặc một việc thường), null = tự do
    private int orderUntil;           // việc thường hết hạn sau ~5 phút; đứng/đi theo thì tới khi hủy
    private java.util.UUID orderFrom; // ai ra lệnh (để đi theo / mang đồ tới)
    private BlockPos stayAt;
    // Trí nhớ khám phá
    private BotMemory.Mind mind;
    private int scoutSince = -1;       // bắt đầu dò chỗ làm base lúc nào
    private BlockPos scoutTarget;      // chỗ hợp làm base đã biết, đang đi tới
    private int nextShareMemory;
    // Trồng trọt và khai khoáng
    private BotCrafting.Wants wants = BotCrafting.Wants.NONE;
    private BlockPos farmWater;        // nguồn nước cạnh ruộng
    private BlockPos farmTask;         // ô ruộng đang xử lý
    private int farmTaskKind;          // 1 thu hoạch, 2 gieo, 3 xới, 4 nhặt hạt (đập cỏ)
    private int mineBlockedUntil;      // bậc thang gặp dung nham/nước: tạm ngưng
    private BlockPos digTask;          // khối đang đào ở lối xuống
    // Tự gỡ kẹt
    private BlockPos digOut;          // khối chắn phía trước cần đào để đi tiếp
    private BlockPos pillarFeet;      // đang kê khối dưới chân để leo ra khỏi hố
    private int escapeSince;
    private int escapeCooldown;
    private Vec3 stillPos = Vec3.ZERO; // đo kẹt thực tế: muốn đi mà không dịch chuyển được
    private int stillCount;
    // Base của nhóm
    private BaseStore.Base base;
    private BaseWork.Status baseStatus;
    private java.util.Map<Blueprint.Mat, Integer> baseNeed = java.util.Map.of();
    private int baseCheckAt;
    private BlockPos allowBasePos;    // khối base mà bản vẽ yêu cầu phá (được phép đào)
    private final java.util.Map<Long, Integer> buildSkip = new java.util.HashMap<>(); // ô không với tới: bỏ qua một lúc
    private LivingEntity huntTarget;

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
    private int bedRetryAt;           // giường vừa không nằm được: thử lại sau
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

    /** Nhãn tiếng Việt của việc đang làm (để trả lời trong chat). */
    private static String goalLabel(Goal g) {
        return switch (g) {
            case FLEE -> "chạy trốn";
            case FIGHT -> "đánh nhau";
            case EAT -> "ăn";
            case HUNT -> "đi săn";
            case COLLECT -> "nhặt đồ";
            case SHELTER -> "trú ẩn";
            case WOOD -> "chặt gỗ";
            case STONE -> "đào đá";
            case DIRT -> "đào đất";
            case EXPLORE -> "đi khám phá";
            case HOME, BASE -> "về nhà";
            case COOK -> "nấu ăn";
            case GROUP -> "về với nhóm";
            case SHARE -> "mang đồ cho bạn";
            case SLEEP -> "ngủ";
            case CRAFT -> "chế đồ";
            case BUILD -> "xây nhà";
            case STAY -> "đứng chờ";
            case FOLLOW -> "đi theo";
            default -> "nghỉ";
        };
    }

    /** Có ai gọi tên bot trong chat. text đã bỏ dấu, chữ thường. */
    void onCommand(net.minecraft.server.level.ServerPlayer from, String text) {
        String who = from.getGameProfile().getName();
        if (!social.isAlly(from)) {
            social.speak("Mình không nghe lời " + who + " đâu.", age);
            return;
        }
        Goal g = null;
        String reply;
        if (has(text, "sao roi", "the nao", "trang thai", "dang lam gi", "status", "how are you")) {
            social.speak("Mình đang " + goalLabel(goal) + ", máu " + (int) bot.getHealth() + "/20, đói "
                    + bot.getFoodData().getFoodLevel() + "/20, gỗ " + (logs + planks / 4) + ", đá " + cobble
                    + (order != null ? " (đang làm theo lệnh: " + goalLabel(order) + ")" : "") + ".", age);
            return;
        } else if (has(text, "tu do", "tiep tuc", "lam viec di", "thoi", "free", "resume", "carry on")) {
            order = null;
            reply = "Ok, mình tự lo tiếp nhé.";
        } else if (has(text, "dung lai", "dung yen", "dung im", "o yen", "o day", "cho day", "stop", "wait", "stay")) {
            g = Goal.STAY;
            reply = "Mình đứng đây chờ " + who + ".";
        } else if (has(text, "lai day", "di theo", "theo minh", "theo toi", "follow", "come")) {
            g = Goal.FOLLOW;
            reply = "Mình theo " + who + " đây!";
        } else if (has(text, "cho do an", "do an", "dua do an", "doi qua", "food", "feed")) {
            ItemStack f = BotItems.bestFood(bot, false);
            if (f.isEmpty()) {
                social.speak("Mình hết đồ ăn rồi " + who + " ơi.", age);
                return;
            }
            shareWith = from;
            shareKind = f;
            g = Goal.SHARE;
            reply = "Mình mang đồ ăn tới cho " + who + ".";
        } else if (has(text, "chat go", "lay go", "kiem go", "wood", "chop")) {
            g = Goal.WOOD;
            reply = "Ok, đi chặt gỗ.";
        } else if (has(text, "dao da", "lay da", "dao mo", "mine", "stone")) {
            g = Goal.STONE;
            reply = "Ok, đi đào đá.";
        } else if (has(text, "san", "hunt")) {
            g = Goal.HUNT;
            reply = "Ok, đi săn.";
        } else if (has(text, "xay", "build")) {
            g = Goal.BUILD;
            reply = base != null ? "Ok, đi xây nhà." : "Nhóm mình chưa có nhà, để mình kiếm chỗ đã.";
        } else if (has(text, "ve nha", "home")) {
            g = Goal.BASE;
            reply = base != null ? "Mình về nhà đây." : "Mình chưa có nhà để về.";
        } else if (has(text, "di ngu", "ngu", "sleep")) {
            g = Goal.SLEEP;
            reply = "Mình đi ngủ.";
        } else {
            social.speak(who + " gọi mình à? Thử: đứng lại, lại đây, chặt gỗ, đào đá, săn, xây, về nhà, ngủ, cho đồ ăn, sao rồi, tự do.", age);
            return;
        }
        if (g != null) {
            order = g;
            orderFrom = from.getUUID();
            orderUntil = (g == Goal.STAY || g == Goal.FOLLOW) ? Integer.MAX_VALUE : age + 6000;
            stayAt = g == Goal.STAY ? bot.blockPosition() : null;
        }
        final String said = text;
        BotLog.log(bot, "ORDER", () -> who + ": \"" + said + "\" -> " + (order == null ? "tự do" : order));
        social.speak(reply, age);
        rethink = true;
    }

    private static boolean has(String text, String... keys) {
        String padded = " " + text + " ";
        for (String k : keys) if (padded.contains(" " + k + " ")) return true;
        return false;
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
        if (age % 20 == 0) { // mỗi giây: có đích mà không nhúc nhích nửa khối -> đếm kẹt
            boolean wantsMove = nav.goal() != null && bot.blockPosition().distSqr(nav.goal()) > 4;
            stillCount = wantsMove && bot.position().distanceToSqr(stillPos) < 0.25D ? stillCount + 1 : 0;
            stillPos = bot.position();
        }
        if (!escape()) act();
        // Rảnh thì thỉnh thoảng nhìn quanh (tầm nhìn chỉ có phía trước)
        if (age >= glanceUntil && age % 80 == 0 && goal != Goal.FIGHT && goal != Goal.FLEE) {
            glanceYaw = bot.getYRot() + (bot.getRandom().nextFloat() - 0.5F) * 220.0F;
            glanceUntil = age + 15;
        }
        if (age < glanceUntil) bot.setYHeadRot(glanceYaw); // chỉ quay đầu, thân vẫn đi theo đường
    }

    // ===== Lệnh từ chat =====

    /** Việc được giao đè lên lựa chọn bình thường; việc khẩn (chạy, nhảy khỏi lửa, ăn khi sắp chết đói, đánh sát) vẫn hơn. */
    private void applyOrder(float[] sc) {
        if (order == null) return;
        if (age > orderUntil) {
            order = null;
            return;
        }
        switch (order) {
            case STAY -> sc[Goal.STAY.ordinal()] = 0.84F;     // dưới đánh sát (0.85+) và chạy trốn
            case FOLLOW -> sc[Goal.FOLLOW.ordinal()] = 0.8F;
            case SHARE -> {
                if (shareWith == null || shareKind.isEmpty()) order = null;
                else sc[Goal.SHARE.ordinal()] = 0.9F;
            }
            default -> {
                // việc thường: nếu đang làm được thì ưu tiên hẳn lên
                if (sc[order.ordinal()] > 0.0F) sc[order.ordinal()] = Math.max(sc[order.ordinal()], 0.82F);
                else if (order == Goal.BASE && base != null) sc[Goal.BASE.ordinal()] = 0.82F;
                else if (order == Goal.SLEEP && senses.best(BotSenses.BED) != null && isNight()) sc[Goal.SLEEP.ordinal()] = 0.86F;
                else if (order == Goal.WOOD || order == Goal.STONE || order == Goal.HUNT) {
                    sc[Goal.EXPLORE.ordinal()] = Math.max(sc[Goal.EXPLORE.ordinal()], 0.5F); // chưa thấy: đi tìm
                }
            }
        }
    }

    private void actStay() {
        if (stayAt != null && bot.blockPosition().distSqr(stayAt) > 2) nav.moveTo(stayAt, age, false); // bị đẩy đi thì về chỗ cũ
        else nav.stop();
    }

    private void actFollow() {
        net.minecraft.world.entity.player.Player leader = orderFrom == null ? null : bot.serverLevel().getPlayerByUUID(orderFrom);
        if (leader == null || !leader.isAlive() || leader.isSpectator()) {
            nav.stop();
            if (leader == null) order = null; // người gọi đã rời đi
            return;
        }
        double d = bot.distanceTo(leader);
        if (d > 3.0D) nav.moveTo(leader.blockPosition(), age, d > 10.0D);
        else {
            nav.stop();
            bot.lookAt(EntityAnchorArgument.Anchor.EYES, leader.getEyePosition());
        }
    }

    // ===== Tự gỡ kẹt (dốc, vách, hố) =====

    private boolean solidAt(BlockPos p) {
        ServerLevel level = bot.serverLevel();
        return !level.getBlockState(p).getCollisionShape(level, p).isEmpty();
    }

    /** Khối nên đào để đi tiếp theo hướng đang đi: vách ngang đầu, hoặc trần chắn không cho nhảy lên bậc. */
    private BlockPos blockingAhead() {
        Vec3 h = nav.heading();
        Direction d = Direction.getNearest(h.x, 0.0D, h.z);
        BlockPos feet = bot.blockPosition();
        BlockPos f0 = feet.relative(d), f1 = f0.above();
        if (solidAt(f1)) return f1;                              // vách cao 2 khối: đào phần ngang đầu trước
        if (solidAt(f0) && solidAt(feet.above(2))) return feet.above(2); // trần thấp trên đầu: không nhảy lên bậc được
        if (solidAt(f0) && solidAt(f0.above(2))) return f0.above(2);     // trên bậc bị chắn
        return null;
    }

    /**
     * Bị kẹt hơn 2 giây khi đang đi: đào khối chắn phía trước, hoặc (đích ở cao hơn) nhảy và kê khối dưới chân
     * để leo lên. Trả true khi tick này dành cho việc gỡ kẹt.
     */
    private boolean escape() {
        if (digOut != null) {
            if (mineTick(digOut) || age - escapeSince > 200) {
                final BlockPos was = digOut;
                if (solidAt(was)) escapeCooldown = age + 100; // không đào được: thôi, chờ cách khác
                digOut = null;
                nav.resetStuck();
                BotLog.log(bot, "ESCAPE", () -> "xong đào khối chắn " + was.toShortString());
            }
            return true;
        }
        if (pillarFeet != null) {
            if (age - escapeSince > 40) { // quá lâu: hủy
                pillarFeet = null;
                escapeCooldown = age + 100;
                return false;
            }
            nav.stop();
            if (bot.getY() < pillarFeet.getY() + 0.05D) {
                bot.setJumping(true);
            } else if (bot.getY() >= pillarFeet.getY() + 1.0D && placeBlock(pillarFeet)) {
                final BlockPos was = pillarFeet;
                BotLog.log(bot, "ESCAPE", () -> "kê khối leo lên tại " + was.toShortString());
                pillarFeet = null;
                nav.resetStuck();
            }
            return true;
        }
        if (age < escapeCooldown || nav.goal() == null || (nav.stuckLevel() < 2 && stillCount < 3)) return false;
        // Đích ở cao hơn và trên đầu còn trống (hố, giếng, vách dốc): kê khối dưới chân leo lên
        boolean goalAbove = nav.goal().getY() > bot.blockPosition().getY();
        if (goalAbove && placeable > 0 && bot.onGround() && !solidAt(bot.blockPosition().above(2))) {
            pillarFeet = bot.blockPosition();
            escapeSince = age;
            stillCount = 0;
            BotLog.log(bot, "ESCAPE", () -> "kẹt, đích ở cao hơn: kê khối leo lên");
            return true;
        }
        BlockPos b = blockingAhead();
        if (b == null && solidAt(bot.blockPosition().above(2)) && goalAbove) b = bot.blockPosition().above(2); // trần ngay trên đầu
        if (b != null) {
            final BlockPos dig = b;
            digOut = dig;
            escapeSince = age;
            stillCount = 0;
            BotLog.log(bot, "ESCAPE", () -> "kẹt, đào khối chắn " + dig.toShortString());
            return true;
        }
        escapeCooldown = age + 60; // không có cách nào: để bộ tìm đường thử hướng khác
        return false;
    }

    private boolean inRoam(BlockPos p) {
        boolean roamOk = rules.roam <= 0 || bot.home().distSqr(p) <= (double) rules.roam * rules.roam;
        // Đi cùng nhóm: chỉ chọn việc trong vòng 32 khối quanh tâm nhóm
        boolean groupOk = groupCenter == null || Vec3.atCenterOf(p).distanceToSqr(groupCenter) <= 32.0D * 32.0D;
        // Có base: mọi việc chọn trong vòng 64 khối quanh base
        boolean baseOk = base == null || !base.dim.equals(bot.level().dimension().location().toString())
                || base.origin.distSqr(p) <= 64 * 64;
        return roamOk && groupOk && baseOk;
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
        snapshotInventory();
        boolean baseDone = base != null && baseStatus != null && baseStatus.structureDone;
        wants = new BotCrafting.Wants(baseDone && rules.mine
                && BotItems.count(bot.getInventory(), st -> st.getItem() instanceof net.minecraft.world.item.HoeItem) == 0,
                BotItems.count(bot.getInventory(), st -> st.is(Items.BREAD)) < 6);
        wantFurnace = rules.cook && rawFood > 0 && furnacePos == null
                && BotItems.count(bot.getInventory(), st -> st.is(Items.FURNACE)) == 0;
        for (int i = 0; i < 6 && BotCrafting.stepInventory(bot, wantFurnace, tableUse != null, baseNeed, wants); i++) {
            // công thức 2x2 chế liền trong túi
        }
        snapshotInventory();

        updateAllies();
        updateMemory();
        if (age >= baseCheckAt) updateBase();
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
            huntTarget = senses.prey;
            if (rules.hunt && senses.prey != null && inRoam(senses.prey.blockPosition()) && foodCount < 2 && food <= 16) {
                sc[Goal.HUNT.ordinal()] = 0.6F + (20 - food) / 80.0F;
            }
            // Base cần giường: giết cừu lấy len (3 len / giường)
            int beds = baseNeed.getOrDefault(Blueprint.Mat.BED, 0);
            int wool = BotItems.count(bot.getInventory(), st -> st.is(net.minecraft.tags.ItemTags.WOOL));
            if (rules.hunt && beds > 0 && wool < 3 * beds && senses.sheep != null && inRoam(senses.sheep.blockPosition())) {
                huntTarget = senses.sheep;
                sc[Goal.HUNT.ordinal()] = Math.max(sc[Goal.HUNT.ordinal()], 0.52F);
            }
            // Xây base ban ngày khi có việc làm được ngay
            if (base != null && baseStatus != null && baseStatus.next != null && !dusk && !night
                    && base.origin.distSqr(bot.blockPosition()) < 96 * 96) {
                sc[Goal.BUILD.ordinal()] = 0.58F;
            }
            if (rules.collect && senses.item != null && inRoam(senses.item.blockPosition())) sc[Goal.COLLECT.ordinal()] = 0.65F;
            if (rules.mine) {
                BlockPos log = senses.best(BotSenses.LOG);
                BlockPos stone = senses.best(BotSenses.STONE);
                BlockPos dirt = senses.best(BotSenses.DIRT);
                int baseWood = baseNeed.getOrDefault(Blueprint.Mat.PLANKS, 0) + 4 * baseNeed.getOrDefault(Blueprint.Mat.LOG, 0)
                        + (baseNeed.containsKey(Blueprint.Mat.DOOR) ? 6 : 0) + 8 * baseNeed.getOrDefault(Blueprint.Mat.CHEST, 0)
                        + 3 * baseNeed.getOrDefault(Blueprint.Mat.BED, 0) + 4 * baseNeed.getOrDefault(Blueprint.Mat.FENCE, 0) / 3;
                boolean needWood = logs * 4 + planks < Math.max(20, baseWood) || pickRank == 0;
                if (log != null && inRoam(log) && needWood) sc[Goal.WOOD.ordinal()] = pickRank == 0 ? 0.65F : 0.5F;
                boolean shortOnBlocks = placeable < 9 && (dusk || night);
                if (pickRank >= 1 && stone != null && inRoam(stone) && (cobble < 24 || shortOnBlocks
                        || cobble < baseNeed.getOrDefault(Blueprint.Mat.COBBLE, 0) + 8)) {
                    sc[Goal.STONE.ordinal()] = shortOnBlocks ? 0.75F : 0.45F;
                }
                if (shortOnBlocks && dirt != null && inRoam(dirt)) sc[Goal.DIRT.ordinal()] = 0.7F;
            }
            if (rules.explore) sc[Goal.EXPLORE.ordinal()] = 0.2F;
            // Có món 3x3 chế được: ra bàn (bàn có sẵn gần đó, hoặc đặt bàn mang theo)
            boolean haveTable = BotItems.count(bot.getInventory(), st -> st.is(Items.CRAFTING_TABLE)) > 0
                    || senses.best(BotSenses.TABLE) != null;
            if (tableUse != null || (haveTable && age >= craftBlockedUntil && BotCrafting.needsTable(bot, wantFurnace, baseNeed, wants))) {
                sc[Goal.CRAFT.ordinal()] = 0.7F;
            }
            if (rules.cook && h == null) {
                int fs = furnaceState();
                boolean spareWood = logs * 4 + planks > 12;
                boolean hasFuel = !BotItems.findFuel(bot.getInventory(), spareWood).isEmpty() || fs == FURNACE_BUSY;
                boolean canGetFurnace = furnacePos != null || BotItems.count(bot.getInventory(), st -> st.is(Items.FURNACE)) > 0;
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
        // Về nhà chung: chiều tối lên đường, đêm vào trong (giường > nhà > hầm chung > hộp 1x1)
        boolean baseReady = base != null && baseStatus != null && baseStatus.structureDone;
        if (rules.shelter && baseReady && shelter == null) {
            if (night) sc[Goal.BASE.ordinal()] = 0.83F;
            else if (dusk && base.origin.distSqr(bot.blockPosition()) > 12 * 12) sc[Goal.BASE.ordinal()] = 0.6F;
        }
        if (base != null && !night && !inShelter && base.dim.equals(bot.level().dimension().location().toString())) {
            double d2 = base.origin.distSqr(bot.blockPosition());
            double lim = goal == Goal.BASE ? 40.0D : 64.0D; // đi quá xa base thì quay về (có độ trễ)
            if (d2 > lim * lim) sc[Goal.BASE.ordinal()] = Math.max(sc[Goal.BASE.ordinal()], 0.56F);
        }
        boolean groupShelter = rules.coop && BotManager.sharedShelterNear(bot) != null;
        boolean canShelter = shelter != null || placeable >= 9 || groupShelter || (allyBotNear(16.0D) && placeable >= 6);
        if (rules.shelter && night && !shelterDone && !shelterAbandoned && canShelter && !baseReady) {
            sc[Goal.SHELTER.ordinal()] = groupShelter ? 0.8F : 0.75F; // tự nhốt trong hộp là lựa chọn cuối
        }
        // Có giường trống (nhà trong làng...) thì ngủ giường tốt hơn tự dựng hầm
        BlockPos bed = senses.best(BotSenses.BED);
        if (rules.shelter && night && shelter == null && age >= bedRetryAt && (bot.isSleeping()
                || (bed != null && inRoam(bed) && bed.distSqr(bot.blockPosition()) < 48 * 48))) {
            sc[Goal.SLEEP.ordinal()] = 0.85F;
        }
        else if (!night && shelter != null) sc[Goal.SHELTER.ordinal()] = 0.9F; // phải mở hầm để ra
        if (!night && !dusk && shelter == null) {
            shelterDone = false;
            shelterAbandoned = false;
        }
        // Hợp tác: xa nhóm quá thì quay về; đồng đội thiếu đồ thì mang tới cho
        if (!inShelter && rules.coop) {
            double lim = goal == Goal.GROUP ? 12.0D : 24.0D; // độ trễ: về tới 12 khối mới thôi
            if (nearestAlly != null && nearestAllyDist > lim) sc[Goal.GROUP.ordinal()] = 0.55F;
            if ((age % 100 == 0 || goal == Goal.SHARE) && order != Goal.SHARE) findShareTarget();
            if (shareWith != null && h == null) {
                // đồng đội sắp chết đói thì ưu tiên hơn cả việc trú ẩn
                sc[Goal.SHARE.ordinal()] = shareWith.getFoodData().getFoodLevel() <= 6 ? 0.85F : 0.7F;
            }
        }

        if (scoutTarget != null && !night && !inShelter) sc[Goal.SCOUT.ordinal()] = 0.52F; // đi tới chỗ hợp làm base
        // Sau khi nhà đã kín: trồng trọt, khai khoáng (việc dài hạn)
        if (!inShelter && !night && !dusk && base != null && baseStatus != null && baseStatus.structureDone) {
            if (rules.mine && age % 40 == 0) planFarm();
            if (farmTask != null) sc[Goal.FARM.ordinal()] = farmTaskKind == 1 ? 0.5F : 0.46F;
            if (rules.mine && pickRank >= 1 && age >= mineBlockedUntil) {
                BlockPos ore = senses.best(BotSenses.ORE);
                boolean oreOk = ore != null && inRoamLoose(ore)
                        && pickRank >= BotSenses.pickNeeded(bot.level().getBlockState(ore));
                if (oreOk) sc[Goal.MINE.ordinal()] = 0.5F;
                else if (planDig()) sc[Goal.MINE.ordinal()] = 0.42F; // không thấy quặng: đào lối xuống
            }
        }
        applyOrder(sc);
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
        rawFood = BotItems.count(inv, st -> BotItems.isSmeltable(lvl, st)); // đồ sống + quặng thô
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
        if (bot.isSleeping() && goal != Goal.SLEEP) { // có việc gấp (bị đánh, chạy...) thì dậy
            bot.stopSleepInBed(false, true);
            BotLog.log(bot, "SLEEP", () -> "thức dậy vì " + goal);
        }
        switch (goal) {
            case FLEE -> actFlee();
            case FIGHT -> chase(fightTarget);
            case HUNT -> chase(huntTarget);
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
            case SLEEP -> actSleep();
            case CRAFT -> actCraft();
            case BUILD -> actBuild();
            case BASE -> actBase();
            case STAY -> actStay();
            case FOLLOW -> actFollow();
            case FARM -> actFarm();
            case MINE -> actMine();
            case SCOUT -> {
                if (scoutTarget == null || nav.moveTo(scoutTarget, age, false) || nav.failed) {
                    baseCheckAt = age; // tới nơi (hoặc không tới được): kiểm tra lại để lập base
                    rethink = true;
                }
            }
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

    /**
     * Chọn đích khám phá: thử 12 hướng x 2 khoảng cách, chấm điểm theo trí nhớ: chunk chưa từng tới được điểm
     * cao nhất, lâu rồi chưa quay lại cũng được, vừa đi qua thì bị trừ. Chưa có base thì ưu tiên đồng bằng.
     */
    private BlockPos pickExploreTarget() {
        var rng = bot.getRandom();
        ServerLevel level = bot.serverLevel();
        BotMemory.Mind m = mind();
        long now = level.getGameTime();
        BlockPos best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int i = 0; i < 12; i++) {
            double ang = heading + i * Math.PI / 6.0D;
            for (double dist : new double[] {20.0D, 32.0D}) {
                double x = bot.getX() + Math.cos(ang) * dist;
                double z = bot.getZ() + Math.sin(ang) * dist;
                if (!level.hasChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4)) continue;
                BlockPos p = surface(x, z);
                if (!inRoam(p) || !level.getFluidState(p.below()).isEmpty() || !level.getFluidState(p).isEmpty()) continue;
                double score = rng.nextDouble(); // chút ngẫu nhiên cho khỏi máy móc
                long last = m == null ? -1L : m.lastVisit(p);
                if (last < 0) score += 10.0D;                       // chưa từng tới
                else if (now - last > 24000L) score += 5.0D;        // hơn một ngày chưa quay lại
                else if (now - last < 6000L) score -= 6.0D;         // vừa đi qua
                if (base == null && BaseWork.isPlainsLike(level, p)) score += 3.0D;
                if (i == 0) score += 1.0D;                          // giữ hướng cũ một chút cho đi xa được
                if (score > bestScore) {
                    bestScore = score;
                    best = p;
                    if (score > 9.0D) heading = ang;
                }
            }
        }
        if (best == null) heading += Math.PI; // bí đường thì quay đầu
        return best;
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
        // Dọn ô theo bản vẽ base: đào thẳng (khối chắn có thể là nội thất base không được phá)
        if (!target.equals(allowBasePos) && hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(target)) {
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
                if (!BotItems.isSmeltable(level, st) || (!in.isEmpty() && !ItemStack.isSameItemSameTags(st, in))) continue;
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
        if (!hasItem) return false; // lò phải chế ở bàn chế tạo (CRAFT)
        BlockPos feet = bot.blockPosition();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos p = feet.relative(d);
            BlockPos below = p.below();
            if (!level.getBlockState(p).canBeReplaced() || !level.getFluidState(p).isEmpty()) continue;
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (bot.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(p))) continue;
            BotItems.take(inv, st -> st.is(Items.FURNACE), 1);
            BlockState furnace = Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, d.getOpposite());
            level.setBlock(p, furnace, 3);
            level.playSound(null, p, furnace.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
            bot.swing(InteractionHand.MAIN_HAND, true);
            furnacePos = p.immutable();
            BotLog.log(bot, "COOK", () -> "đặt lò tại " + furnacePos.toShortString());
            return true;
        }
        return false;
    }

    /** Không phá thứ nguy hiểm hoặc quan trọng: đáy chân, chất lỏng kề, khối có dữ liệu, bedrock... */
    private boolean breakable(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        if (pos.equals(allowBasePos)) return state.getFluidState().isEmpty(); // bản vẽ base yêu cầu dọn ô này
        if (BaseWork.protects(BaseStore.get(bot.getServer()), level, pos)) return false; // không phá nhà của nhóm
        if (state.hasBlockEntity() || state.is(Blocks.SPAWNER) || !state.getFluidState().isEmpty()) return false;
        if (state.is(Blocks.CRAFTING_TABLE) && !(tableTemp && pos.equals(tableUse))) return false; // bàn của người khác/base
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

    // ===== Chế đồ ở bàn chế tạo =====

    /** Ô trống cạnh chân để đặt một khối (bàn, lò): sàn đặc, không chất lỏng, không có sinh vật. */
    private BlockPos freeSpotBeside() {
        ServerLevel level = bot.serverLevel();
        BlockPos feet = bot.blockPosition();
        // tìm từ gần ra xa (tối đa 3 khối, lệch cao 1), miễn còn với tới
        for (int r = 1; r <= 3; r++) {
            for (int dy = 0; dy >= -1; dy--) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        BlockPos p = feet.offset(dx, dy, dz);
                        BlockPos below = p.below();
                        if (!level.getBlockState(p).canBeReplaced() || !level.getFluidState(p).isEmpty()) continue;
                        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
                        if (bot.getEyePosition().distanceToSqr(Vec3.atCenterOf(p)) > REACH * REACH) continue;
                        if (!level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(p)).isEmpty()) continue;
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private void actCraft() {
        ServerLevel level = bot.serverLevel();
        if (tableUse == null) {
            BlockPos known = senses.best(BotSenses.TABLE);
            if (known != null && known.distSqr(bot.blockPosition()) < 24 * 24) {
                tableUse = known;
                tableTemp = false;
                BotLog.log(bot, "CRAFT", () -> "dùng bàn có sẵn tại " + known.toShortString());
            } else {
                BlockPos spot = freeSpotBeside();
                if (spot == null || !BotItems.take(bot.getInventory(), st -> st.is(Items.CRAFTING_TABLE), 1)) {
                    craftBlockedUntil = age + 200; // chỗ này chật quá: đi việc khác rồi thử lại
                    BotLog.log(bot, "CRAFT", () -> "không có chỗ đặt bàn quanh đây, để sau");
                    rethink = true;
                    return;
                }
                level.setBlock(spot, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
                level.playSound(null, spot, Blocks.CRAFTING_TABLE.defaultBlockState().getSoundType().getPlaceSound(),
                        SoundSource.BLOCKS, 1.0F, 0.8F);
                bot.swing(InteractionHand.MAIN_HAND, true);
                tableUse = spot.immutable();
                tableTemp = true;
                BotLog.log(bot, "CRAFT", () -> "đặt bàn tạm tại " + spot.toShortString());
            }
        }
        if (!level.getBlockState(tableUse).is(Blocks.CRAFTING_TABLE)) { // bàn bị phá mất
            tableUse = null;
            tableTemp = false;
            return;
        }
        Vec3 center = Vec3.atCenterOf(tableUse);
        if (bot.getEyePosition().distanceToSqr(center) > REACH * REACH) {
            nav.moveTo(tableUse, age, false);
            if (nav.failed) {
                senses.ban(tableUse, age + 2400);
                tableUse = null;
            }
            return;
        }
        nav.stop();
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, center); // nhìn vào bàn khi chế
        if (age % 10 != 0) return; // mỗi món nửa giây
        if (BotCrafting.stepTable(bot, wantFurnace, baseNeed, wants)) {
            bot.swing(InteractionHand.MAIN_HAND, true);
            snapshotInventory();
            return;
        }
        // Hết món để chế: bàn tạm thì phá thu lại, bàn có sẵn thì để nguyên
        if (tableTemp) {
            if (mineTick(tableUse)) {
                BotLog.log(bot, "CRAFT", () -> "thu lại bàn tạm");
                tableUse = null;
                tableTemp = false;
                rethink = true;
            }
        } else {
            tableUse = null;
            rethink = true;
        }
    }

    // ===== Base của nhóm =====

    // ===== Trí nhớ khám phá =====

    private BotMemory.Mind mind() {
        if (mind == null && bot.getServer() != null) mind = BotMemory.get(bot.getServer()).of(bot.getGameProfile().getName());
        return mind;
    }

    /** Ghi nhớ chunk đang đứng; mỗi 5 giây khảo sát quanh: nước, chỗ hợp làm base, hang; gặp đồng đội thì kể cho nhau. */
    private void updateMemory() {
        BotMemory.Mind m = mind();
        if (m == null) return;
        ServerLevel level = bot.serverLevel();
        long now = level.getGameTime();
        if (age % 40 == 0) m.markVisited(bot.blockPosition(), now);
        if (age % 100 == 0) {
            BlockPos here = bot.blockPosition();
            BlockPos water = BaseWork.waterNear(level, here, 16);
            if (water != null && m.remember(BotMemory.PoiType.WATER, water, now)) {
                BotLog.log(bot, "MEMORY", () -> "nhớ nguồn nước tại " + water.toShortString());
            }
            // chỗ hợp làm base: đồng bằng, gần nước, đủ phẳng
            if (base == null && age % 400 == 0 && water != null && BaseWork.isPlainsLike(level, here)) { // tìm chỗ phẳng khá nặng: thưa thôi
                BlockPos site = BaseWork.findSite(level, here);
                if (site != null && BaseWork.isPlainsLike(level, site) && BaseWork.waterNear(level, site, 12) != null
                        && m.remember(BotMemory.PoiType.SITE, site, now)) {
                    BotLog.log(bot, "MEMORY", () -> "nhớ chỗ hợp làm base (đồng bằng, gần nước) tại " + site.toShortString());
                }
            }
            BlockPos ore = senses.best(BotSenses.ORE);
            if (ore != null) m.remember(BotMemory.PoiType.ORE, ore, now);
            // đang ở dưới mặt đất mà xung quanh trống: cửa hang
            int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, here.getX(), here.getZ());
            if (here.getY() < surfaceY - 6 && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, here) == 0
                    && m.remember(BotMemory.PoiType.CAVE, here, now)) {
                BotLog.log(bot, "MEMORY", () -> "nhớ hang tại " + here.toShortString());
            }
        }
        // Gặp đồng đội (bot) trong 8 khối: kể cho nhau những địa điểm mình biết
        if (age >= nextShareMemory) {
            for (net.minecraft.world.entity.player.Player p : allies) {
                if (!(p instanceof BotPlayer other) || p.distanceTo(bot) > 8.0D) continue;
                nextShareMemory = age + 600;
                int learned = other.learnFrom(m.pois);
                if (learned > 0) {
                    social.speak(other.getGameProfile().getName() + ", mình biết " + learned + " chỗ hay (nước, đất đẹp, hang...), kể cậu nghe nhé.", age);
                }
                break;
            }
        }
    }

    /** Nghe đồng đội kể: ghi nhớ những địa điểm mình chưa biết. Trả về số địa điểm mới. */
    int learnFrom(java.util.List<BotMemory.Poi> told) {
        BotMemory.Mind m = mind();
        if (m == null) return 0;
        int n = 0;
        for (BotMemory.Poi p : java.util.List.copyOf(told)) if (m.remember(p.type(), p.pos(), p.time())) n++;
        final int learned = n;
        if (n > 0) BotLog.log(bot, "MEMORY", () -> "được kể " + learned + " địa điểm mới");
        return n;
    }

    /** Mỗi 2 giây: vào base của nhóm (hoặc lập base mới), kiểm tra tiến độ, nâng cấp khi đủ người. */
    private void updateBase() {
        baseCheckAt = age + 40;
        net.minecraft.server.MinecraftServer server = bot.getServer();
        if (server == null) return;
        BaseStore store = BaseStore.get(server);
        String name = bot.getGameProfile().getName();
        ServerLevel level = bot.serverLevel();
        String dim = level.dimension().location().toString();
        base = store.baseOf(name);
        if (base == null && rules.coop) { // vào base của đồng đội gần đó
            for (BaseStore.Base b : store.all()) {
                if (!b.dim.equals(dim) || b.origin.distSqr(bot.blockPosition()) > 96 * 96) continue;
                boolean friendly = b.members.stream().noneMatch(BotManager::isOnlineBot);
                for (String m : b.members) {
                    net.minecraft.server.level.ServerPlayer pm = server.getPlayerList().getPlayerByName(m);
                    if (pm != null && social.isAlly(pm)) friendly = true;
                }
                if (friendly) {
                    store.join(b, name);
                    base = b;
                    final BaseStore.Base jb = b;
                    BotLog.log(bot, "BASE", () -> "vào base chung #" + jb.id + " tại " + jb.origin.toShortString());
                    break;
                }
            }
        }
        long t = level.getDayTime() % 24000L;
        scoutTarget = null;
        if (base == null && t < 9000L && pickRank >= 1 && logs * 4 + planks >= 16) { // đủ đồ nghề thì lập base
            BotMemory.Mind m = mind();
            BotMemory.Poi known = m == null ? null : m.nearest(BotMemory.PoiType.SITE, bot.blockPosition(), 256.0D);
            BlockPos site = null;
            if (known != null && known.pos().distSqr(bot.blockPosition()) < 16 * 16) {
                site = BaseWork.findSite(level, known.pos()); // tới nơi: kiểm tra lại rồi lập
                if (site == null) m.forget(BotMemory.PoiType.SITE, known.pos());
            } else if (known != null) {
                scoutTarget = known.pos(); // đã biết chỗ đẹp: đi tới đó
            } else {
                if (scoutSince < 0) scoutSince = age;
                if (age - scoutSince > 6000) { // dò 5 phút chưa thấy: lập ở chỗ phẳng gần nhất
                    BlockPos center = groupCenter != null ? BlockPos.containing(groupCenter) : bot.blockPosition();
                    site = BaseWork.findSite(level, center);
                }
            }
            if (site != null) {
                final BlockPos s0 = site;
                base = store.create(dim, site, name);
                scoutSince = -1;
                BotLog.log(bot, "BASE", () -> "lập base mới tại " + s0.toShortString()
                        + (BaseWork.isPlainsLike(level, s0) ? " (đồng bằng)" : "")
                        + (BaseWork.waterNear(level, s0, 12) != null ? " (gần nước)" : ""));
            }
        }
        if (base == null || !base.dim.equals(dim)) {
            baseStatus = null;
            baseNeed = java.util.Map.of();
            return;
        }
        buildSkip.values().removeIf(until -> until <= age);
        baseStatus = BaseWork.status(level, base, bot, p -> buildSkip.containsKey(p.asLong()));
        baseNeed = BaseWork.deficit(baseStatus, bot.getInventory());
        // Nâng cấp: xong cấp 1 và nhóm >= 2 bot -> cấp 2; xong cấp 2 và >= 3 bot -> cấp 3
        if (baseStatus.tierDone && base.tier < Blueprint.MAX_TIER) {
            long members = base.members.stream().filter(BotManager::isOnlineBot).count();
            if (members >= base.tier + 1) {
                base.tier++;
                store.changed();
                final int tier = base.tier;
                BotLog.log(bot, "BASE", () -> "nâng nhà chung lên cấp " + tier);
                server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal(
                        "<" + name + "> Nhà chung lên cấp " + tier + " rồi, mọi người cùng xây nhé!"), false);
                baseStatus = BaseWork.status(level, base, bot, p -> buildSkip.containsKey(p.asLong()));
                baseNeed = BaseWork.deficit(baseStatus, bot.getInventory());
            }
        }
    }

    private void actBuild() {
        ServerLevel level = bot.serverLevel();
        if (base == null || baseStatus == null || baseStatus.next == null) {
            nav.stop();
            rethink = true;
            return;
        }
        Blueprint.Entry e = baseStatus.next;
        BlockPos p = base.origin.offset(e.dx(), e.dy(), e.dz());
        if (BaseWork.satisfied(level, p, e.mat())) { // đã xong (có thể do đồng đội làm)
            baseCheckAt = age;
            rethink = true;
            return;
        }
        BlockState cur = level.getBlockState(p);
        if (!cur.canBeReplaced() || !cur.getFluidState().isEmpty()) { // phải dọn khối đang chiếm chỗ trước
            allowBasePos = p;
            if (mineTick(p)) {
                allowBasePos = null;
                BlockState after = level.getBlockState(p);
                if (!after.canBeReplaced() || !after.getFluidState().isEmpty()) {
                    buildSkip.put(p.asLong(), age + 600); // không dọn được: tạm bỏ qua, làm ô khác
                    BotLog.log(bot, "BUILD", () -> "tạm bỏ qua ô " + p.toShortString() + " (không dọn được)");
                }
                baseCheckAt = age;
                rethink = true;
            }
            return;
        }
        Vec3 center = Vec3.atCenterOf(p);
        if (bot.getEyePosition().distanceToSqr(center) > 4.5D * 4.5D) {
            boolean arrived = nav.moveTo(p, age, false);
            if (nav.failed || (arrived && bot.getEyePosition().distanceToSqr(center) > 4.5D * 4.5D)) {
                buildSkip.put(p.asLong(), age + 600); // không với tới: làm ô khác trước
                baseCheckAt = age;
                rethink = true;
            }
            return;
        }
        // Đang đứng chiếm ô cần đặt (hoặc ô đầu giường / nửa trên cửa): bước sang bên cạnh trước
        BlockPos extra = e.mat() == Blueprint.Mat.BED ? p.relative(e.facing())
                : e.mat() == Blueprint.Mat.DOOR ? p.above() : p;
        net.minecraft.world.phys.AABB need = new net.minecraft.world.phys.AABB(p).minmax(new net.minecraft.world.phys.AABB(extra));
        if (bot.getBoundingBox().intersects(need)) {
            BlockPos aside = stepAside(need);
            if (aside != null) nav.moveTo(aside, age, false);
            else buildSkip.put(p.asLong(), age + 200);
            return;
        }
        nav.stop();
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, center);
        if (age % 4 != 0) return;
        if (BaseWork.place(level, bot, e, p)) {
            bot.swing(InteractionHand.MAIN_HAND, true);
            BotLog.log(bot, "BUILD", () -> e.mat() + " tại " + p.toShortString() + " (base #" + base.id + " cấp " + base.tier + ")");
        } else {
            buildSkip.put(p.asLong(), age + 100); // có ai đứng chắn / thiếu chỗ: làm ô khác trước
        }
        baseCheckAt = age; // cập nhật việc tiếp theo
        rethink = true;
    }

    /** Ô đứng gần (1-3 khối) không chạm vùng box, để bước ra khỏi chỗ cần đặt khối. */
    private BlockPos stepAside(net.minecraft.world.phys.AABB box) {
        BlockPos here = bot.blockPosition();
        for (int r = 1; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    BlockPos q = standable(here.getX() + dx, here.getY(), here.getZ() + dz);
                    if (q != null && !box.intersects(new net.minecraft.world.phys.AABB(q).expandTowards(0, 1, 0))) return q;
                }
            }
        }
        return null;
    }

    /** Ban đêm: vào trong nhà chung, đứng ở ô giữa và đóng cửa. */
    private void actBase() {
        if (base == null) {
            nav.stop();
            return;
        }
        ServerLevel level = bot.serverLevel();
        int[] c = Blueprint.insideCell(base.tier);
        BlockPos cell = base.origin.offset(c[0], c[1], c[2]);
        if (bot.blockPosition().distSqr(cell) > 2) {
            nav.moveTo(cell, age, bot.blockPosition().distSqr(cell) > 48 * 48); // xa thì chạy nhanh
            if (!isNight() && bot.blockPosition().distSqr(cell) < 24 * 24) rethink = true; // ban ngày: về gần là đủ
            return;
        }
        nav.stop();
        if (!isNight() || age % 20 != 0) return;
        for (Blueprint.Entry e : Blueprint.tier(base.tier)) { // đóng cửa nhà khi đã vào trong
            if (e.mat() != Blueprint.Mat.DOOR) continue;
            BlockPos dp = base.origin.offset(e.dx(), e.dy(), e.dz());
            BlockState ds = level.getBlockState(dp);
            if (ds.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door
                    && ds.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)) {
                door.setOpen(bot, level, ds, dp, false);
            }
        }
    }

    // ===== Trồng trọt: lúa mì cạnh nguồn nước gần base =====

    /** Khu khai thác/ruộng được phép xa base hơn một chút (96 khối). */
    private boolean inRoamLoose(BlockPos p) {
        return base == null || base.origin.distSqr(p) <= 96 * 96;
    }

    /** Ô đất trồng được quanh nguồn nước (bán kính 4 = vùng nước tưới tới), không thuộc khung nhà. */
    private java.util.List<BlockPos> farmCells(ServerLevel level) {
        java.util.List<BlockPos> cells = new java.util.ArrayList<>();
        if (farmWater == null) return cells;
        BaseStore store = BaseStore.get(bot.getServer());
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos c = farmWater.offset(dx, 0, dz);
                BlockState s = level.getBlockState(c);
                boolean soil = s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.FARMLAND);
                if (!soil || BaseWork.protects(store, level, c)) continue;
                BlockState up = level.getBlockState(c.above());
                if (!(up.isAir() || up.is(Blocks.WHEAT) || up.is(Blocks.GRASS) || up.is(Blocks.FERN))) continue;
                cells.add(c);
                if (cells.size() >= 24) return cells;
            }
        }
        return cells;
    }

    /** Chọn việc ruộng: thu hoạch lúa chín > gieo hạt > xới đất > đập cỏ lấy hạt. */
    private void planFarm() {
        farmTask = null;
        ServerLevel level = bot.serverLevel();
        if (farmWater == null || !level.getFluidState(farmWater).is(net.minecraft.tags.FluidTags.WATER)) {
            farmWater = BaseWork.waterNear(level, base.origin.offset(3, 0, 3), 16);
            if (farmWater == null) return;
        }
        Inventory inv = bot.getInventory();
        int seeds = BotItems.count(inv, st -> st.is(Items.WHEAT_SEEDS));
        boolean hoe = BotItems.count(inv, st -> st.getItem() instanceof net.minecraft.world.item.HoeItem) > 0;
        int bestKind = 99;
        double bestD = Double.MAX_VALUE;
        for (BlockPos c : farmCells(level)) {
            BlockState s = level.getBlockState(c), up = level.getBlockState(c.above());
            int kind;
            if (up.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && crop.isMaxAge(up)) kind = 1;
            else if (s.is(Blocks.FARMLAND) && up.isAir() && seeds > 0) kind = 2;
            else if (!s.is(Blocks.FARMLAND) && hoe) kind = 3;
            else continue;
            double d = c.distSqr(bot.blockPosition());
            if (kind < bestKind || (kind == bestKind && d < bestD)) {
                bestKind = kind;
                bestD = d;
                farmTask = kind == 1 ? c.above() : c;
                farmTaskKind = kind;
            }
        }
        if (farmTask == null && seeds < 4) { // chưa có hạt: đập cỏ quanh đây
            BlockPos grass = senses.best(BotSenses.GRASS);
            if (grass != null && inRoamLoose(grass)) {
                farmTask = grass;
                farmTaskKind = 4;
            }
        }
    }

    private void actFarm() {
        ServerLevel level = bot.serverLevel();
        if (farmTask == null) {
            nav.stop();
            rethink = true;
            return;
        }
        BlockPos p = farmTask;
        if (farmTaskKind == 1 || farmTaskKind == 4) { // thu hoạch / đập cỏ: phá khối (rơi lúa mì, hạt)
            allowBasePos = p;
            if (mineTick(p)) {
                allowBasePos = null;
                final int k = farmTaskKind;
                BotLog.log(bot, "FARM", () -> (k == 1 ? "thu hoạch lúa mì tại " : "đập cỏ lấy hạt tại ") + p.toShortString());
                farmTask = null;
                planFarm();
            }
            return;
        }
        Vec3 c = Vec3.atCenterOf(p);
        if (bot.getEyePosition().distanceToSqr(c) > REACH * REACH) {
            nav.moveTo(p, age, false);
            if (nav.failed) farmTask = null;
            return;
        }
        nav.stop();
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, c);
        if (age % 6 != 0) return;
        Inventory inv = bot.getInventory();
        if (farmTaskKind == 3) { // xới đất bằng cuốc xới
            BotItems.equipBest(bot, Blocks.FARMLAND.defaultBlockState());
            for (int i = 0; i < 9; i++) {
                if (inv.getItem(i).getItem() instanceof net.minecraft.world.item.HoeItem) inv.selected = i;
            }
            ItemStack hoe = bot.getMainHandItem();
            if (!(hoe.getItem() instanceof net.minecraft.world.item.HoeItem)) {
                farmTask = null;
                return;
            }
            BlockState above = level.getBlockState(p.above());
            if (!above.isAir()) level.destroyBlock(p.above(), true, bot); // cỏ trên mặt đất
            level.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 11);
            level.playSound(null, p, net.minecraft.sounds.SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            hoe.hurtAndBreak(1, bot, b -> b.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            BotLog.log(bot, "FARM", () -> "xới đất tại " + p.toShortString());
        } else if (farmTaskKind == 2 && BotItems.take(inv, st -> st.is(Items.WHEAT_SEEDS), 1)) { // gieo hạt
            level.setBlock(p.above(), Blocks.WHEAT.defaultBlockState(), 3);
            level.playSound(null, p, net.minecraft.sounds.SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            BotLog.log(bot, "FARM", () -> "gieo hạt tại " + p.toShortString());
        }
        bot.swing(InteractionHand.MAIN_HAND, true);
        planFarm();
    }

    // ===== Khai khoáng: quặng lộ ra, không có thì đào lối xuống bậc thang ngoài base =====

    private static final int MINE_BOTTOM_Y = 16; // tầng sắt

    /** Bậc thang đi xuống phía tây base: bậc i ở entrance + (-i, -i, 0), cần trống 3 khối để đi xuống. */
    private BlockPos stairStep(int i) {
        BlockPos entrance = base.origin.offset(-5, 0, 3);
        int depth = Math.min(i, entrance.getY() - MINE_BOTTOM_Y);
        int tunnel = Math.max(0, i - depth); // tới đáy thì đào hầm ngang tiếp
        return entrance.offset(-depth - tunnel, -depth, 0);
    }

    /** Tìm khối cần đào tiếp ở lối xuống. Trả false nếu đã xong (hầm ngang đủ dài) hoặc không có base. */
    private boolean planDig() {
        if (base == null) return false;
        ServerLevel level = bot.serverLevel();
        int maxSteps = (base.origin.getY() - MINE_BOTTOM_Y) + 48;
        for (int i = 0; i <= maxSteps; i++) {
            BlockPos st = stairStep(i);
            boolean bottom = st.getY() <= MINE_BOTTOM_Y;
            int height = bottom ? 2 : 3; // hầm ngang cao 2, bậc thang cần 3 để bước xuống
            for (int h = height - 1; h >= 0; h--) { // đào từ trên xuống
                BlockPos q = st.above(h);
                if (!level.isLoaded(q)) return false;
                BlockState s = level.getBlockState(q);
                if (!s.getCollisionShape(level, q).isEmpty()) {
                    digTask = q.immutable();
                    return true;
                }
            }
        }
        digTask = null;
        return false;
    }

    private void actMine() {
        ServerLevel level = bot.serverLevel();
        BlockPos ore = senses.best(BotSenses.ORE);
        BlockPos target = ore != null && pickRank >= BotSenses.pickNeeded(level.getBlockState(ore)) ? ore : digTask;
        if (target == null) {
            nav.stop();
            rethink = true;
            return;
        }
        if (mineTick(target)) {
            BlockState after = level.getBlockState(target);
            if (target.equals(digTask) && !after.getCollisionShape(level, target).isEmpty()) {
                mineBlockedUntil = age + 2400; // không đào được (dung nham/nước kề bên): tạm ngưng
                BotLog.log(bot, "MINE", () -> "lối xuống bị chặn tại " + target.toShortString() + ", tạm ngưng");
            } else if (target.equals(ore)) {
                BotLog.log(bot, "MINE", () -> "đào quặng tại " + target.toShortString());
                BotMemory.Mind m = mind();
                if (m != null) m.forget(BotMemory.PoiType.ORE, target);
            }
            // cứ 6 bậc đặt một ngọn đuốc nếu có
            if (target.equals(digTask) && BotItems.count(bot.getInventory(), st -> st.is(Items.TORCH)) > 0) {
                BlockPos floor = target.below();
                if (Math.floorMod(target.getX(), 6) == 0 && level.getBlockState(target).isAir()
                        && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                    BotItems.take(bot.getInventory(), st -> st.is(Items.TORCH), 1);
                    level.setBlock(target, Blocks.TORCH.defaultBlockState(), 3);
                }
            }
            planDig();
            rethink = true;
        }
    }

    // ===== Ngủ giường =====

    private void actSleep() {
        if (bot.isSleeping()) {
            nav.stop();
            return;
        }
        BlockPos bed = senses.best(BotSenses.BED);
        if (bed == null) {
            nav.stop();
            rethink = true;
            return;
        }
        BlockState bedState = bot.level().getBlockState(bed);
        if (!BotManager.claimBed(bot.getGameProfile().getName(), bed)
                || (bedState.hasProperty(net.minecraft.world.level.block.BedBlock.OCCUPIED)
                && bedState.getValue(net.minecraft.world.level.block.BedBlock.OCCUPIED))) {
            senses.ban(bed, age + 2400); // giường của bot khác / đã có người nằm: tìm giường khác
            BotLog.log(bot, "SLEEP", () -> "giường " + bed.toShortString() + " đã có người, tìm giường khác");
            rethink = true;
            return;
        }
        if (bot.position().distanceToSqr(Vec3.atBottomCenterOf(bed)) > 2.0D * 2.0D) {
            nav.moveTo(bed, age, false);
            if (nav.failed) {
                senses.ban(bed, age + 2400);
                BotLog.log(bot, "SLEEP", () -> "không tới được giường " + bed.toShortString());
            }
            return;
        }
        nav.stop();
        bot.startSleepInBed(bed).ifLeft(problem -> {
            BotLog.log(bot, "SLEEP", () -> "không ngủ được ở " + bed.toShortString() + ": " + problem);
            senses.ban(bed, age + 1200);
            bedRetryAt = age + 200;
        }).ifRight(ok -> BotLog.log(bot, "SLEEP", () -> "nằm ngủ tại " + bed.toShortString()));
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
        if (order == Goal.SHARE) order = null;
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
