package net.ss.sudungeon.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.network.SsModVariables;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = SsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEnterDungeonEvent extends Event {

    private final ServerPlayer player;
    private final BlockPos dungeonEntrancePos;

    public PlayerEnterDungeonEvent (ServerPlayer player, BlockPos dungeonEntrancePos) {
        this.player = player;
        this.dungeonEntrancePos = dungeonEntrancePos;
    }

    // Getter cho player
    public ServerPlayer getPlayer () {
        return player;
    }

    // Getter cho dungeonEntrancePos
    public BlockPos getDungeonEntrancePos () {
        return dungeonEntrancePos;
    }

}