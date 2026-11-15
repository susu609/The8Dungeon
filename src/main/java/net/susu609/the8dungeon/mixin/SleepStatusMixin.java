package net.susu609.the8dungeon.mixin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.susu609.the8dungeon.ai.BotPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Bot nằm giường nhưng không được tính vào việc bỏ qua đêm: loại bot khỏi danh sách người chơi
 * mà vanilla dùng để đếm người đang ngủ. Đêm vẫn trôi qua khi người chơi thật ngủ như bình thường.
 */
@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
    private List<ServerPlayer> susu609$ignoreBotsInCount(List<ServerPlayer> players) {
        return players.stream().filter(p -> !(p instanceof BotPlayer)).toList();
    }

    @ModifyVariable(method = "areEnoughDeepSleeping", at = @At("HEAD"), argsOnly = true)
    private List<ServerPlayer> susu609$ignoreBotsInDeepSleep(List<ServerPlayer> players) {
        return players.stream().filter(p -> !(p instanceof BotPlayer)).toList();
    }
}
