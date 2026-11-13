package net.susu609.the8dungeon.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/** Mẫu JSON mà model lập kế hoạch (Opus) bắt buộc phải trả về. */
public record ClawdPlan(
        @JsonPropertyDescription("Một câu ngắn tiếng Việt Clawd nói khi bắt đầu làm.")
        String say,
        @JsonPropertyDescription("Các thao tác theo thứ tự thực hiện, tối đa 64. Tọa độ tương đối so với Clawd, trong khoảng -8..8.")
        List<Step> steps) {

    public record Step(
            @JsonPropertyDescription("PLACE để đặt khối, BREAK để phá khối.")
            Op op,
            int dx,
            int dy,
            int dz,
            @JsonPropertyDescription("Id khối Minecraft, ví dụ minecraft:stone_bricks. Để rỗng với BREAK.")
            String block) {}

    public enum Op { PLACE, BREAK }
}
