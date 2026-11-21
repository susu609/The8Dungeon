# GPT draft Git workflow

Repository: `susu609/The8Dungeon`

## Nhánh làm việc

- Nhánh dành cho các thử nghiệm của GPT: `gpt/draft`.
- Tạo nhánh từ `main` khi chưa tồn tại.
- Không commit trực tiếp vào `main`.
- Không tự merge hoặc tạo Pull Request nếu người dùng chưa yêu cầu.
- Trước khi thay đổi, kiểm tra diff và không ghi đè công việc chưa hoàn tất của người khác.

## Commit và ghi nhận tác giả

- Chỉ tạo commit và push theo yêu cầu rõ ràng của người dùng.
- Ưu tiên ghi `GPT` là Git author khi công cụ cho phép thiết lập tên và email hợp lệ.
- Nếu công cụ GitHub không hỗ trợ tùy chỉnh Git author, phải thông báo danh tính tác giả thực tế; không được nhận là `GPT` khi metadata không đúng.
- Có thể ghi nhận hỗ trợ của Codex qua commit trailer `Co-authored-by: Codex <codex@openai.com>` khi phù hợp.

## Phạm vi commit thử nghiệm

Tài liệu này dùng để kiểm tra việc tạo nhánh và commit riêng, không thay đổi gameplay, build, tài nguyên hoặc cấu hình của mod.
