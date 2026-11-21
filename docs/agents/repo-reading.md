# Quy tắc đọc repository The8Dungeon

Tài liệu này quy định cách AI và cộng tác viên kiểm tra source, phân tích, đề xuất thay đổi và chỉnh sửa repository chính.

## 1. Repository và môi trường mục tiêu

- **Repository chính:** https://github.com/susu609/The8Dungeon
- **Branch mặc định:** Branch mặc định hiện hành của repository (hiện là `main`); nếu yêu cầu chỉ định branch hoặc commit thì dùng chính xác ref đó.
- **Minecraft:** Java Edition 1.20.1
- **Forge:** 47.4.10 trở lên (mức cấu hình cụ thể phải kiểm tra trong source hiện tại).
- **Mappings:** Official mappings cho Minecraft 1.20.1.
- **Base package:** `net.susu609.sudungeon`.
- **Mod ID:** `susu609`.

Không mặc định chuyển sang repository `The8Dungeon_2027s`.

## 2. Khi nào phải kiểm tra source

Khi người dùng yêu cầu xem repo, nghiên cứu tính năng, tìm lỗi, đề xuất kiến trúc, xác định nơi sửa hoặc sửa code:

1. Truy cập repository `susu609/The8Dungeon` để xác minh source liên quan trước khi đưa ra kết luận về code hiện tại.
2. Trước khi phân tích source, đọc tài liệu này (`docs/agents/repo-reading.md`) nếu tồn tại.
3. Dùng branch mặc định hiện tại, trừ khi người dùng chỉ định branch hoặc commit khác.
4. Chỉ đọc file trực tiếp liên quan và các caller, dependency, cấu hình cần thiết để hiểu luồng xử lý.
5. Không quét toàn bộ repository khi câu hỏi chỉ cần một phần source.
6. Không dựa vào code hoặc mô tả từ lịch sử trò chuyện để khẳng định trạng thái code hiện tại khi chưa xác minh.
7. Nếu không truy cập được GitHub, nêu rõ giới hạn và không được nói hoặc ngụ ý rằng đã đọc source.

## 3. Thứ tự ưu tiên nguồn code

Ưu tiên nguồn đứng trên khi có xung đột:

1. **File local mới nhất** do người dùng vừa cung cấp.
2. **Diff hoặc patch local** do người dùng cung cấp.
3. **Branch hoặc commit GitHub được chỉ định** trong yêu cầu.
4. **Branch mặc định** của `susu609/The8Dungeon`.
5. **Code cũ trong lịch sử chat** (chỉ để tham khảo, không dùng làm bằng chứng về hiện trạng).

Nếu file local hoặc diff mới hơn phiên bản trên GitHub, phân tích theo bản local và nêu rõ nguồn đang dùng.

## 4. Quy tắc đề xuất và sửa đổi

- Khi đề xuất thay đổi, chỉ rõ **đường dẫn file → class → method → vị trí cần sửa → lý do**.
- Phân biệt **đã xác minh trong source**, **suy luận**, và **đề xuất chưa triển khai**.
- Không tạo, thay thế hoặc sửa code khi người dùng chưa yêu cầu rõ ràng. Yêu cầu phân tích không tự động cho phép chỉnh sửa.
- Khi được yêu cầu chỉnh sửa, giới hạn thay đổi ở phần cần thiết; tránh sửa lan sang phần không liên quan.
- Không tự ý thay đổi Minecraft version, Forge version, mappings, base package hoặc mod ID chỉ vì ví dụ trong lịch sử chat.

## 5. Kiểm tra nhanh trước mỗi lần phân tích

- [ ] Đã xác định đúng repository và ref cần đọc.
- [ ] Đã đọc `docs/agents/repo-reading.md` nếu có.
- [ ] Đã kiểm tra file local/diff mới hơn GitHub nếu được cung cấp.
- [ ] Đã chỉ đọc các source, caller và dependency liên quan.
- [ ] Kết luận bám vào source vừa kiểm tra; nêu rõ giới hạn khi không truy cập được.
- [ ] Nếu đề xuất sửa: có file, class, method và vị trí cụ thể.
- [ ] Nếu thực hiện thay đổi: người dùng đã yêu cầu chỉnh sửa rõ ràng.
