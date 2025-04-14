package net.ss.sudungeon.util;

import net.ss.sudungeon.SsMod;

import java.util.HashMap;
import java.util.Map;

public class Log {

    // Bộ lưu trữ log đã ghi
    private static final Map<String, Long> logCache = new HashMap<>();
    private static final long LOG_INTERVAL_MS = 10000; // Thời gian tối thiểu giữa các log trùng lặp (ms)

    /**
     * Lấy thông tin về vị trí gọi log (Tên tệp và Phương thức).
     *
     * @return tên tệp và phương thức dưới dạng "TênTệp.TênPhươngThức"
     */
    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            String className = caller.getFileName();

            if (className != null && className.endsWith(".java")) {
                className = className.substring(0, className.length() - 5);
            }

            String methodName = caller.getMethodName();
            return className + "." + methodName;
        }
        return "Unknown.UnknownMethod";
    }

    /**
     * Kiểm tra xem log có được ghi không
     *
     * @param message Nội dung log
     * @return true nếu log nên được ghi, false nếu bị ngăn do trùng lặp
     */
    private static boolean shouldLog(String message) {
        long currentTime = System.currentTimeMillis();
        synchronized (logCache) {
            if (logCache.containsKey(message)) {
                long lastLoggedTime = logCache.get(message);
                if (currentTime - lastLoggedTime < LOG_INTERVAL_MS) {
                    // Nếu log trước đó cách chưa đủ thời gian LOG_INTERVAL_MS, bỏ qua log
                    return false;
                }
            }
            // Lưu thời gian log hiện tại
            logCache.put(message, currentTime);
        }
        return true;
    }

    // ===== LOG LEVEL METHODS =====

    /**
     * Thông báo ở mức INFO.
     * @param message Nội dung log
     */
    public static void i(String message) {
        if (shouldLog(message)) {
            SsMod.LOGGER.info("[{}]: {}", getCallerInfo(), message);
        }
    }

    /**
     * Thông báo ở mức INFO với một tác vụ cụ thể.
     * @param message Nội dung log
     * @param taskName Tên tác vụ
     */
    public static void i(String message, String taskName) {
        if (shouldLog(message)) {
            SsMod.LOGGER.info("[{}]: {} - Task: {}", getCallerInfo(), message, taskName);
        }
    }

    /**
     * Log ở mức DEBUG.
     * @param message Nội dung log
     */
    public static void d(String message) {
        if (shouldLog(message)) {
            SsMod.LOGGER.debug("[{}]: {}", getCallerInfo(), message);
        }
    }

    /**
     * Cảnh báo (WARN).
     * @param message Nội dung log
     */
    public static void w(String message) {
        if (shouldLog(message)) {
            SsMod.LOGGER.warn("[{}]: {}", getCallerInfo(), message);
        }
    }
    /**
     * Cảnh báo (WARN).
     * @param message Nội dung log
     */
    public static void w(String message, String taskName) {
        if (shouldLog(message)) {
            SsMod.LOGGER.warn("[{}]: {}", getCallerInfo(), message);
        }
    }

    /**
     * Log lỗi (ERROR) kèm theo ngoại lệ.
     * @param message Nội dung log
     * @param e Ngoại lệ
     */
    public static void e(String message, Exception e) {
        if (shouldLog(message)) {
            SsMod.LOGGER.error("[{}]: {}", getCallerInfo(), message, e);
        }
    }

    /**
     * Log lỗi (ERROR) không kèm ngoại lệ.
     * @param message Nội dung log
     */
    public static void e(String message) {
        if (shouldLog(message)) {
            SsMod.LOGGER.error("[{}]: {}", getCallerInfo(), message);
        }
    }
}