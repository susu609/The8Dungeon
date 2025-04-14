/*
package net.ss.sudungeon.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.ss.sudungeon.network.SsModVariables;

public class VariableTest {
    public static void execute (LevelAccessor world, Entity entity) {
        if (entity == null)
            return;

        Log.i(
                SsModVariables.MapVariables.get(world).blockstate_GM
                        + "\n" +
                        SsModVariables.blockstate_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).blockstate_GW
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).blockstate_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).blockstate_PP);

        Log.i(
                SsModVariables.MapVariables.get(world).direction_GM
                        + "\n" +
                        SsModVariables.direction_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).direction_GW
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).direction_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).direction_PP);

        Log.i(
                SsModVariables.MapVariables.get(world).itemstack_GM
                        + "\n" +
                        SsModVariables.itemstack_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).itemstack_GW
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).itemstack_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).itemstack_PP);

        Log.i(
                SsModVariables.MapVariables.get(world).logic_GM
                        + "\n" +
                        SsModVariables.logic_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).logic_GW + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).logic_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).logic_PP);

        Log.i(
                SsModVariables.MapVariables.get(world).number_GM
                        + "\n" +
                        SsModVariables.number_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).number_GW
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).number_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).number_PP);

        Log.i(
                SsModVariables.MapVariables.get(world).string_GM
                        + "\n" +
                        SsModVariables.string_GS
                        + "\n" +
                        SsModVariables.WorldVariables.get(world).string_GW
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).string_PL
                        + "\n" +
                        (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).string_PP);
    }

    public static void execute1 (LevelAccessor world, Entity entity) {
        if (entity == null)
            return;
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).blockstate_GM = SsModVariables.MapVariables.get(world).blockstate_GM;
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.blockstate_GS = SsModVariables.blockstate_GS;
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).blockstate_GW = SsModVariables.WorldVariables.get(world).blockstate_GW;
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            BlockState _setval = ((entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).blockstate_PL);
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.blockstate_PL = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //PLAYER PERSITENT
        {
            BlockState _setval = ((entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).blockstate_PP);
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.blockstate_PP = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).direction_GM = SsModVariables.MapVariables.get(world).direction_GM;
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.direction_GS = SsModVariables.direction_GS;
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).direction_GW = SsModVariables.WorldVariables.get(world).direction_GW;
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            Direction _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).direction_PL;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.direction_PL = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //PLAYER PERSITENT
        {
            Direction _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).direction_PP;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.direction_PP = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).itemstack_GM = SsModVariables.MapVariables.get(world).itemstack_GM.copy();
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.itemstack_GS = SsModVariables.itemstack_GS.copy();
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).itemstack_GW = SsModVariables.WorldVariables.get(world).itemstack_GW.copy();
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            ItemStack _setval = ((entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).itemstack_PL);
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.itemstack_PL = _setval.copy();
                capability.syncPlayerVariables(entity);
            });
        }
        //PLAYER PERSITENT
        {
            ItemStack _setval = ((entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).itemstack_PP);
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.itemstack_PP = _setval.copy();
                capability.syncPlayerVariables(entity);
            });
        }
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).logic_GM = SsModVariables.MapVariables.get(world).logic_GM;
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.logic_GS = SsModVariables.logic_GS;
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).logic_GW = SsModVariables.WorldVariables.get(world).logic_GW;
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            boolean _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).logic_PL;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.logic_PL = _setval;
                capability.syncPlayerVariables(entity);
            });
        }

        {
            boolean _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).logic_PP;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.logic_PP = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).number_GM = SsModVariables.MapVariables.get(world).number_GM;
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.number_GS = SsModVariables.number_GS;
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).number_GW = SsModVariables.WorldVariables.get(world).number_GW;
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            double _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).number_PL;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.number_PL = _setval;
                capability.syncPlayerVariables(entity);
            });
        }

        {
            double _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).number_PP;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.number_PP = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //GLOBAL MAP
        SsModVariables.MapVariables.get(world).string_GM = SsModVariables.MapVariables.get(world).string_GM;
        SsModVariables.MapVariables.get(world).syncData(world);
        //GLOBAL SESSION
        SsModVariables.string_GS = SsModVariables.string_GS;
        //GLOBAL WORLD
        SsModVariables.WorldVariables.get(world).string_GW = SsModVariables.WorldVariables.get(world).string_GW;
        SsModVariables.WorldVariables.get(world).syncData(world);
        //PLAYER LIFT TIME
        {
            String _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).string_PL;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.string_PL = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
        //PLAYER PERSITENT
        {
            String _setval = (entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SsModVariables.PlayerVariables())).string_PP;
            entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.string_PP = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
    }

}
*/
