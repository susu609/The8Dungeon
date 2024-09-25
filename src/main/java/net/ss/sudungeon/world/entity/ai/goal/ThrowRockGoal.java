package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.ss.sudungeon.world.entity.ColossalZombie;

public class ThrowRockGoal extends Goal {
    private final ColossalZombie colossalZombie;
    private final double range;
    private final int delay;
    private int timer;

    public ThrowRockGoal (ColossalZombie colossalZombie, double range, int delay) {
        this.colossalZombie = colossalZombie;
        this.range = range;
        this.delay = delay;
        this.timer = 0;
    }

    @Override
    public boolean canUse () {
        Player nearestPlayer = this.colossalZombie.level().getNearestPlayer(this.colossalZombie, range);
        return nearestPlayer != null
                && this.colossalZombie.hasLineOfSight(nearestPlayer)
                && this.colossalZombie.getHealth() <= 50;
    }

    @Override
    public void start () {
        this.timer = 0;
    }

    @Override
    public void tick () {
        if (this.timer >= this.delay) {
            Player nearestPlayer = this.colossalZombie.level().getNearestPlayer(this.colossalZombie, range);
            if (nearestPlayer != null && this.colossalZombie.hasLineOfSight(nearestPlayer)) {
                throwRockAt(nearestPlayer);
            }
            this.timer = 0;
        } else {
            this.timer++;
        }
    }

    private void throwRockAt (Player target) {
        ItemStack rock = new ItemStack(Blocks.STONE.asItem());
        Vec3 colossalZombiePos = this.colossalZombie.position();
        Vec3 playerPos = target.position();

        Vec3 direction = playerPos.subtract(colossalZombiePos).normalize().scale(0.5);

        BlockPos spawnPos = new BlockPos((int) colossalZombiePos.x, (int) (colossalZombiePos.y + 1), (int) colossalZombiePos.z);
        ItemEntity rockEntity = new ItemEntity(this.colossalZombie.level(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), rock);
        rockEntity.setDeltaMovement(direction.x, direction.y + 0.3, direction.z);

        this.colossalZombie.level().addFreshEntity(rockEntity);
    }

    @Override
    public boolean canContinueToUse () {
        Player nearestPlayer = this.colossalZombie.level().getNearestPlayer(this.colossalZombie, range);
        return nearestPlayer != null
                && this.colossalZombie.hasLineOfSight(nearestPlayer)
                && this.colossalZombie.getHealth() <= 50;
    }
}
