package net.ss.sudungeon.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ss.sudungeon.init.SsModMenus;
import net.ss.sudungeon.util.BaseMenu;
import org.jetbrains.annotations.NotNull;

public class FillToolMenu extends BaseMenu {

    public FillToolMenu (int id, Inventory inv) {
        super(SsModMenus.FILL_TOOL_GUI.get(), id, inv, false); // Không thêm inventory!
    }
    public FillToolMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv); // Gọi constructor chính
    }
    @Override
    public @NotNull ItemStack quickMoveStack (@NotNull Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid (Player player) {
        return true;
    }
}
