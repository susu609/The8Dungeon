package net.ss.sudungeon.init;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_IS_TEMPLATE = GameRules.register("isTemplate", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
}

