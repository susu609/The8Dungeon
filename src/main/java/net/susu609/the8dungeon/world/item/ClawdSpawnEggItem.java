package net.susu609.the8dungeon.world.item;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.susu609.the8dungeon.init.ModEntitiesRegistry;
import net.susu609.the8dungeon.world.entity.ClawdEntity;

/** Trứng triệu hồi Clawd. Mỗi người chơi chỉ một Clawd: dùng lại trứng sẽ thay con cũ. */
public class ClawdSpawnEggItem extends ForgeSpawnEggItem {

    private static final String KEY = "susu609_clawd";

    public ClawdSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type,
                             int background, int highlight, Properties props) {
        super(type, background, highlight, props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (!(level instanceof ServerLevel server) || player == null) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockPos spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                ? pos : pos.relative(ctx.getClickedFace());

        removeOldClawd(server, player);

        ClawdEntity clawd = ModEntitiesRegistry.CLAWD_AGENT.get().create(server);
        if (clawd == null) return InteractionResult.FAIL;
        clawd.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                player.getYRot() + 180F, 0F);
        clawd.tame(player);
        clawd.setPersistenceRequired();
        server.addFreshEntity(clawd);
        clawd.playHappySound();

        persisted(player).putUUID(KEY, clawd.getUUID());
        if (!player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    /** UUID của Clawd đang hoạt động của người chơi, hoặc null nếu chưa có. */
    public static UUID activeClawdOf(Player player) {
        CompoundTag data = persisted(player);
        return data.hasUUID(KEY) ? data.getUUID(KEY) : null;
    }

    private static void removeOldClawd(ServerLevel server, Player player) {
        UUID old = activeClawdOf(player);
        if (old == null) return;
        for (ServerLevel lvl : server.getServer().getAllLevels()) {
            Entity e = lvl.getEntity(old);
            if (e != null) {
                e.discard();
                return;
            }
        }
        // Không tìm thấy (vùng chưa tải): Clawd cũ tự biến mất trong tick() khi được tải lại.
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
