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
- Nếu người dùng muốn ghi nhận đóng góp AI bằng tài khoản Codex trên GitHub, có thể sử dụng trailer `Co-authored-by: Codex <199175422+chatgpt-codex-connector[bot]@users.noreply.github.com>` cho những commit thực sự có AI hỗ trợ. Trailer không làm thay đổi Git Author hoặc Git Committer.
- GitHub chỉ tính đóng góp trên nhánh mặc định hoặc các nhánh đủ điều kiện; không merge `gpt/draft` chỉ để làm tăng số Contributors.

## Phạm vi commit thử nghiệm

Tài liệu này dùng để kiểm tra việc tạo nhánh và commit riêng, không thay đổi gameplay, build, tài nguyên hoặc cấu hình của mod.
