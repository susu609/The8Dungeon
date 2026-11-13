package net.susu609.the8dungeon.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** Mẫu JSON mà model giao tiếp (Haiku) bắt buộc phải trả về. */
public record ClawdReply(
        @JsonPropertyDescription("Câu trả lời của Clawd, tiếng Việt, tối đa 2 câu ngắn.")
        String say,
        @JsonPropertyDescription("NONE nếu chỉ trò chuyện; FOLLOW/STAY cho đi theo/đứng; TASK nếu được nhờ xây, đào, đặt hoặc phá khối.")
        Action action,
        @JsonPropertyDescription("Chỉ khi action là TASK: mô tả việc cần làm thật rõ trong một câu. Còn lại để rỗng.")
        String task) {

    public enum Action { NONE, FOLLOW, STAY, TASK }
}
