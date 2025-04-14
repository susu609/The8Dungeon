package net.ss.sudungeon.util;

import net.minecraftforge.eventbus.api.Event;

public class EventUtil {

    /**
     * Huỷ hoặc từ chối sự kiện nếu có thể.
     *
     * @param event Sự kiện cần huỷ
     */
    public static void cancelOrDeny (Event event) {
        if (event == null) return;

        if (event.isCancelable()) {
            event.setCanceled(true);
        } else if (event.hasResult()) {
            event.setResult(Event.Result.DENY);
        }
    }
}
