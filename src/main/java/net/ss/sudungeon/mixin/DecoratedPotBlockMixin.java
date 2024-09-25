package net.ss.sudungeon.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DecoratedPotBlock.class)
public class DecoratedPotBlockMixin {

    @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true)
    private void modifyDrops (BlockState state, LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        Player player = (Player) builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        List<ItemStack> drops = new ArrayList<>(cir.getReturnValue()); // Giữ lại các item đã được định nghĩa

        // Kiểm tra và thêm vũ khí nếu không có trong inventory
        if (player != null && !ss$hasWeaponInInventory(player)) {
            if (RandomSource.create().nextDouble() < 0.50) {
                drops.add(new ItemStack(Items.WOODEN_SWORD));
            }
        }

        cir.setReturnValue(drops); // Set lại các drops bao gồm cả item mặc định và vũ khí
    }

    @Unique
    private boolean ss$hasWeaponInInventory (Player player) {
        return ss$isWeapon(player.getMainHandItem()) || ss$isWeapon(player.getOffhandItem()) ||
                player.getInventory().items.stream().anyMatch(this::ss$isWeapon);
    }

    @Unique
    private boolean ss$isWeapon (ItemStack itemStack) {
        return itemStack.is(Items.WOODEN_SWORD) || itemStack.is(Items.STONE_SWORD) ||
                itemStack.is(Items.IRON_SWORD) || itemStack.is(Items.GOLDEN_SWORD) ||
                itemStack.is(Items.DIAMOND_SWORD) || itemStack.is(Items.NETHERITE_SWORD) ||
                itemStack.is(Items.BOW) || itemStack.is(Items.CROSSBOW) ||
                itemStack.is(Items.TRIDENT) || itemStack.is(Items.WOODEN_AXE);
    }
}
