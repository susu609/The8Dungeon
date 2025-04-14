package util;

import net.ss.sudungeon.SsMod;

import java.io.IOException;
import java.nio.file.Path;

public class Log {

    /**
     * Lấy thông tin về vị trí gọi log (Tên tệp và Phương thức).
     *
     * @return tên tệp và phương thức dưới dạng "TênTệp.TênPhươngThức"
     */
    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        if (stackTrace.length > 3) { // Vị trí thứ 3 là nơi gọi phương thức log
            StackTraceElement caller = stackTrace[3];
            String className = caller.getFileName(); // Tên file (class + .java)

            if (className != null && className.endsWith(".java")) {
                className = className.substring(0, className.length() - 5); // Bỏ ".java"
            }

            String methodName = caller.getMethodName(); // Lấy tên phương thức
            return className + "." + methodName;        // Định dạng dưới dạng TênTệp.TênPhươngThức
        }
        return "Unknown.UnknownMethod";                 // Mặc định nếu không xác định được
    }

    // ===== LOG LEVEL METHODS =====

    /**
     * Thông báo ở mức INFO.
     * @param message Nội dung log
     */
    public static void i(String message) {
        SsMod.LOGGER.info("[{}]: {}", getCallerInfo(), message);
    }

    /**
     * Thông báo ở mức INFO với một tác vụ cụ thể.
     * @param message Nội dung log
     * @param taskName Tên tác vụ
     */
    public static void i(String message, String taskName) {
        SsMod.LOGGER.info("[{}]: {}", getCallerInfo(), message);
    }

    /**
     * Log ở mức DEBUG.
     * @param message Nội dung log
     */
    public static void d(String message) {
        SsMod.LOGGER.debug("[{}]: {}", getCallerInfo(), message);
    }

    /**
     * Cảnh báo (WARN).
     * @param message Nội dung log
     */
    public static void w(String message) {
        SsMod.LOGGER.warn("[{}]: {}", getCallerInfo(), message);
    }

    /**
     * Log lỗi (ERROR) kèm theo ngoại lệ.
     * @param message Nội dung log
     * @param e Ngoại lệ
     */
    public static void e(String message, Exception e) {
        SsMod.LOGGER.error("[{}]: {}", getCallerInfo(), message, e);
    }

    /**
     * Log lỗi (ERROR) không kèm ngoại lệ.
     * @param message Nội dung log
     */
    public static void e(String message) {
        SsMod.LOGGER.error("[{}]: {}", getCallerInfo(), message);
    }

    // ===== UNUSED METHODS =====

    /**
     * Warning (không sử dụng).
     * @param s Nội dung log
     * @param p Đường dẫn
     */
    public static void w(String s, Path p) {
        // Placeholder for actual implementation
    }

    /**
     * Cảnh báo (không sử dụng).
     * @param s Nội dung log
     * @param tempDataPackDir Thư mục tạm
     */
    public static void w(String s, Path tempDataPackDir, IOException ioException) {
        // Placeholder for actual implementation
    }
}