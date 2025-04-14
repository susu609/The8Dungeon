package net.ss.sudungeon.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class BaseMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    Minecraft mc = MinecraftUtils.getMinecraft();
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    public IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private Entity boundEntity = null;
    private BlockEntity boundBlockEntity = null;

    protected BaseMenu(@NotNull MenuType<? extends BaseMenu> type, int id, Inventory inv, boolean skipInventory) {
        super(type, id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.internal = new ItemStackHandler(0);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !bound || isBoundValid();
    }

    private boolean isBoundValid() {
        return (boundItemMatcher != null && boundItemMatcher.get()) ||
                (boundBlockEntity != null && AbstractContainerMenu.stillValid(access, entity, boundBlockEntity.getBlockState().getBlock())) ||
                (boundEntity != null && boundEntity.isAlive());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            movedStack = originalStack.copy();

            // 🔹 Nếu slot là của container, di chuyển sang kho đồ player
            if (slotIndex < this.customSlots.size()) {
                if (!this.moveItemStackTo(originalStack, this.customSlots.size(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            // 🔹 Nếu slot là của kho đồ player, di chuyển vào container
            else if (!this.moveItemStackTo(originalStack, 0, this.customSlots.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (originalStack.getCount() == movedStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, originalStack);
        }
        return movedStack;
    }

    @Override
    protected boolean moveItemStackTo(@NotNull ItemStack stackToMove, int startIndex, int endIndex, boolean reverse) {
        boolean moved = false;
        int index = reverse ? endIndex - 1 : startIndex;

        // 🔹 Gộp các stack nếu có thể
        if (stackToMove.isStackable()) {
            while (!stackToMove.isEmpty()) {
                if (reverse ? index < startIndex : index >= endIndex) break;

                Slot slot = this.slots.get(index);
                ItemStack slotStack = slot.getItem();

                if (slot.mayPlace(slotStack) && !slotStack.isEmpty() && ItemStack.isSameItemSameTags(stackToMove, slotStack)) {
                    int combinedCount = slotStack.getCount() + stackToMove.getCount();
                    int maxStackSize = Math.min(slot.getMaxStackSize(), stackToMove.getMaxStackSize());

                    if (combinedCount <= maxStackSize) {
                        stackToMove.setCount(0);
                        slotStack.setCount(combinedCount);
                        slot.set(slotStack);
                        moved = true;
                    } else if (slotStack.getCount() < maxStackSize) {
                        stackToMove.shrink(maxStackSize - slotStack.getCount());
                        slotStack.setCount(maxStackSize);
                        slot.set(slotStack);
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }

        // 🔹 Di chuyển stack vào slot trống nếu còn dư
        if (!stackToMove.isEmpty()) {
            index = reverse ? endIndex - 1 : startIndex;
            while (true) {
                if (reverse ? index < startIndex : index >= endIndex) break;

                Slot slot = this.slots.get(index);
                ItemStack slotStack = slot.getItem();

                if (slotStack.isEmpty() && slot.mayPlace(stackToMove)) {
                    slot.setByPlayer(stackToMove.split(Math.min(stackToMove.getCount(), slot.getMaxStackSize())));
                    slot.setChanged();
                    moved = true;
                    break;
                }
                index += reverse ? -1 : 1;
            }
        }
        return moved;
    }

    @Override
    public void removed (@NotNull Player playerIn) {
        super.removed(playerIn);
        if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }

    public Map<Integer, Slot> get () {
        return customSlots;
    }
}