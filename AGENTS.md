# The8Dungeon — Agent Instructions

Đây là repository chính của dự án: https://github.com/susu609/The8Dungeon.

## Môi trường mục tiêu

- Minecraft Java 1.20.1
- Minecraft Forge 47.4.10+ (kiểm tra phiên bản thực tế trong `build.gradle`)
- Official mappings 1.20.1
- Java package gốc: `net.susu609.sudungeon`
- Mod ID: `susu609`

## Quy tắc bắt buộc

**Trước khi phân tích source, đọc [docs/agents/repo-reading.md](docs/agents/repo-reading.md) và tuân thủ thứ tự ưu tiên nguồn code trong đó.**

- Mặc định đọc branch chính hiện hành của `susu609/The8Dungeon` (hiện là `main`), trừ khi người dùng chỉ định branch hoặc commit.
- Ưu tiên file local và diff/patch mới hơn source GitHub.
- Chỉ đọc những file liên quan cùng caller/dependency cần thiết; không quét toàn bộ repository nếu không cần.
- Không coi `The8Dungeon_2027s` là repository mặc định.
- Không khẳng định tình trạng code hiện tại nếu chưa xác minh. Nếu không truy cập được GitHub, phải nói rõ.
- Khi đề xuất sửa, nêu rõ **đường dẫn file, class, method và vị trí cần sửa**.
- Chỉ viết hoặc sửa code khi người dùng yêu cầu rõ ràng.
