package net.ss.sudungeon.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.ss.sudungeon.world.inventory.FillToolMenu;
import net.ss.sudungeon.util.*;

public class FillTool extends Item {

    private static BlockPos firstPosition = null;
    private static BlockPos secondPosition = null;

    public FillTool(Properties properties) {
        super(properties);
    }
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Khớp với phương thức trong lớp cha
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        BlockPos pos = context.getClickedPos(); // block bị click
        BlockState state = context.getLevel().getBlockState(pos);
        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

        // Lưu blockId vào GUI state
        FillToolMenu.guistate.put("blockId", blockId);

        // Mở GUI
        GuiOpener.openGui(player, "Fill Tool", FillToolMenu::new);

        return InteractionResult.SUCCESS;
    }


    private void fillRegion(ServerLevel world, Player player, BlockPos pos1, BlockPos pos2, BlockState blockState) {
        int x1 = Math.min(pos1.getX(), pos2.getX());
        int y1 = Math.min(pos1.getY(), pos2.getY());
        int z1 = Math.min(pos1.getZ(), pos2.getZ());

        int x2 = Math.max(pos1.getX(), pos2.getX());
        int y2 = Math.max(pos1.getY(), pos2.getY());
        int z2 = Math.max(pos1.getZ(), pos2.getZ());

        // Bắt đầu đặt khối
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    world.setBlock(currentPos, blockState, 3); // Đặt khối với flag cập nhật 3
                }
            }
        }

        // Gửi phản hồi cho người chơi
        player.displayClientMessage(Component.nullToEmpty("Filled region from " + pos1 + " to " + pos2), false);
    }
}