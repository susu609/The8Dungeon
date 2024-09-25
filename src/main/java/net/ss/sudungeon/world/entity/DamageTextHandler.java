package net.ss.sudungeon.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageTextHandler {
    private static final int LIFETIME = 40; // Số tick (40 = 2 giây)

    @SubscribeEvent
    public static void onEntityHurt (LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        float damageAmount = event.getAmount();

        // Chỉ thực hiện khi ở server-side
        if (!entity.level().isClientSide()) {
            // Tạo một thực thể TextDisplay để hiển thị sát thương
            DamageTextDisplay textDisplay = new DamageTextDisplay(EntityType.TEXT_DISPLAY, entity.level());
            textDisplay.setPos(entity.getX(), entity.getY() + 1.5, entity.getZ());
            textDisplay.setCustomName(Component.literal(String.format("%.1f", damageAmount)));
            textDisplay.setCustomNameVisible(true);

            // Thêm TextDisplay vào thế giới
            entity.level().addFreshEntity(textDisplay);
        }
    }

    public static class DamageTextDisplay extends TextDisplay {

        private int lifetime = LIFETIME; // Thời gian tồn tại của văn bản

        public DamageTextDisplay (EntityType<? extends TextDisplay> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        public void tick () {
            super.tick();

            // Di chuyển văn bản lên trên mỗi tick
            this.setDeltaMovement(new Vec3(0, 0.03, 0));

            // Giảm thời gian tồn tại
            lifetime--;

            // Nếu thời gian tồn tại đã hết, loại bỏ thực thể
            if (lifetime <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }
}
