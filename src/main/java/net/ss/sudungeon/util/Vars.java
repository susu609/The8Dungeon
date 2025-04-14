package net.ss.sudungeon.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.ss.sudungeon.network.SsModVariables;

import java.util.function.Consumer;

public class Vars {

    // ================================
    // 📦 GLOBAL MAP (toàn server, giữ khi thoát game)
    // ================================
    public static SsModVariables.MapVariables GM(LevelAccessor world) {
        return SsModVariables.MapVariables.get(world);
    }

    public static void setGM(LevelAccessor world, Consumer<SsModVariables.MapVariables> action) {
        SsModVariables.MapVariables vars = GM(world);
        action.accept(vars);
        vars.syncData(world);
    }

    // ================================
    // 🌍 GLOBAL WORLD (theo dimension)
    // ================================
    public static SsModVariables.WorldVariables GW(LevelAccessor world) {
        return SsModVariables.WorldVariables.get(world);
    }

    public static void setGW(LevelAccessor world, Consumer<SsModVariables.WorldVariables> action) {
        SsModVariables.WorldVariables vars = GW(world);
        action.accept(vars);
        vars.syncData(world);
    }

    // ================================
    // 🧠 GLOBAL SESSION (biến RAM tạm thời)
    // ================================
    public static SsModVariables gs() {
        return new SsModVariables(); // dùng static trực tiếp ngoài
    }

    // ================================
    // 🙍 PLAYER VARIABLES - PP (Persistent)
    // ================================
    public static SsModVariables.PlayerVariables PP(Entity entity) {
        return entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new SsModVariables.PlayerVariables());
    }

    public static void setPP(Entity entity, Consumer<SsModVariables.PlayerVariables> action) {
        entity.getCapability(SsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(pv -> {
            action.accept(pv);
            pv.syncPlayerVariables(entity); // tự đồng bộ
        });
    }

    // ================================
    // 🙍 PLAYER VARIABLES - PL (Life-time, mất khi chết)
    // ⚠ Tạm dùng chung PlayerVariables nếu chưa tách riêng
    // ================================
    public static SsModVariables.PlayerVariables PL(Entity entity) {
        return PP(entity); // Nếu sau này có tách riêng thì đổi lại
    }

    public static void setPL(Entity entity, Consumer<SsModVariables.PlayerVariables> action) {
        setPP(entity, action); // Tạm dùng chung
    }
}
