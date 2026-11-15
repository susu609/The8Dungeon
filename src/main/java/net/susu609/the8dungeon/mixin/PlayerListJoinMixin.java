package net.susu609.the8dungeon.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.susu609.the8dungeon.ai.BotPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Bỏ dòng "xxx joined the game" khi bot hồi sinh sau khi chết (lần spawn đầu vẫn báo). */
@Mixin(PlayerList.class)
public abstract class PlayerListJoinMixin {

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void susu609$quietBotRespawn(PlayerList list, Component message, boolean overlay) {
        if (BotPlayer.quietJoin) return;
        list.broadcastSystemMessage(message, overlay);
    }
}
