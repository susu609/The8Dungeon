package net.susu609.the8dungeon;

import java.io.InputStream;
import java.util.Properties;

/**
 * Thông tin bản build (nhánh, commit, thời gian), do Gradle ghi vào susu609_build.properties.
 * In một dòng khi mod khởi động để dán vào báo lỗi: biết chính xác đang chạy commit nào.
 */
public final class BuildInfo {

    private static final String LINE = load();

    private BuildInfo() {}

    /** Ví dụ: claude/player_ai @ a1b2c3d (clean) build 2026-11-15 13:40:02 */
    public static String line() {
        return LINE;
    }

    private static String load() {
        Properties p = new Properties();
        try (InputStream in = BuildInfo.class.getResourceAsStream("/susu609_build.properties")) {
            if (in == null) return "không rõ bản build (thiếu susu609_build.properties)";
            p.load(in);
        } catch (Exception e) {
            return "không đọc được thông tin build: " + e;
        }
        return p.getProperty("branch", "?") + " @ " + p.getProperty("commit", "?")
                + " (" + p.getProperty("state", "?") + ") build " + p.getProperty("time", "?");
    }
}
