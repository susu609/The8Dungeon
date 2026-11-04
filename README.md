# The8Dungeon

Tài liệu hướng dẫn phát triển dự án **The8Dungeon**.

> **Trạng thái:** Khởi tạo tài liệu. Repository hiện chưa có mã nguồn, cấu hình build hoặc thông tin phiên bản; các mục phụ thuộc công nghệ cụ thể cần được cập nhật khi dự án được thiết lập.

## 1. Giới thiệu

The8Dungeon là tên dự án. Tài liệu này định hướng cách chuẩn bị môi trường, tổ chức quá trình phát triển, kiểm thử và quản lý mã nguồn.

Nếu đây là dự án mod Minecraft, hãy bổ sung tại đây:
- Phiên bản Minecraft mục tiêu.
- Mod loader: Forge, NeoForge hay Fabric.
- Phiên bản Java yêu cầu.
- Công cụ tạo dự án: MCreator hay cấu hình Gradle tự quản lý.
- Mô tả gameplay, dungeon, mob, vật phẩm và cơ chế chính.

## 2. Yêu cầu trước khi bắt đầu

Cài đặt các công cụ phù hợp với cấu hình thực tế của dự án:

- **Git:** quản lý mã nguồn.
- **Java JDK:** chọn đúng phiên bản theo Minecraft và mod loader.
- **MCreator:** dùng nếu dự án được tạo và quản lý bằng MCreator.
- **IntelliJ IDEA:** dùng khi cần chỉnh sửa mã Java hoặc làm việc trực tiếp với dự án Gradle.
- **Minecraft và mod loader tương ứng:** dùng để chạy thử mod.

Không tự chọn phiên bản Java, Minecraft hoặc mod loader trước khi xác nhận cấu hình dự án.

## 3. Thiết lập môi trường

### Trường hợp A — Dự án dùng MCreator

1. Cài MCreator tương thích với phiên bản Minecraft mục tiêu.
2. Mở workspace của The8Dungeon bằng MCreator.
3. Chờ workspace tải và kiểm tra các dependency.
4. Dùng chức năng chạy client/server tích hợp để kiểm tra thay đổi.
5. Sao lưu workspace và commit các tệp cần thiết bằng Git.

### Trường hợp B — Dự án Java/Gradle dùng IntelliJ IDEA

1. Clone repository:

   ```bash
   git clone https://github.com/susu609/The8Dungeon.git
   cd The8Dungeon
   ```

2. Mở thư mục dự án bằng IntelliJ IDEA.
3. Nếu repository đã có `build.gradle` hoặc `build.gradle.kts`, chọn tải Gradle project.
4. Cấu hình Project SDK theo yêu cầu của dự án.
5. Chạy task build/test được khai báo trong Gradle.

> Repository hiện chưa có tệp Gradle. Các bước Gradle chỉ áp dụng sau khi cấu hình build được thêm vào.

## 4. Quy trình phát triển

1. Cập nhật nhánh `main` trước khi bắt đầu.
2. Tạo nhánh riêng cho từng tính năng hoặc bản sửa lỗi.
3. Thực hiện thay đổi theo từng phần nhỏ.
4. Chạy build và kiểm thử trong môi trường mục tiêu.
5. Kiểm tra `git diff`, loại bỏ tệp sinh tự động hoặc dữ liệu không cần thiết.
6. Tạo Pull Request hoặc merge sau khi kiểm tra.

Quy ước tên nhánh gợi ý:

- `feature/ten-tinh-nang`
- `fix/mo-ta-loi`
- `docs/noi-dung-tai-lieu`

## 5. Cấu trúc thư mục dự kiến

Cấu trúc dưới đây chỉ là gợi ý cho dự án mod Java/Gradle; không phải cấu trúc hiện có của repository.

```text
The8Dungeon/
├── README.md
├── .gitignore
├── build.gradle             # nếu dùng Gradle
├── settings.gradle          # nếu dùng Gradle
├── gradle/                  # nếu dùng Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/            # mã nguồn Java
│   │   └── resources/       # assets, metadata, data
│   └── test/                # kiểm thử nếu có
└── docs/                    # tài liệu thiết kế và phát triển
```

Với workspace MCreator, cần giữ cấu trúc do MCreator tạo ra và cập nhật hướng dẫn này theo cấu trúc thực tế.

## 6. Kiểm thử và xử lý lỗi

Trước khi gửi thay đổi:

- Kiểm tra dự án có khởi động được hay không.
- Kiểm tra log khi khởi động và khi vào thế giới.
- Kiểm tra các tính năng vừa thay đổi trong game.
- Kiểm tra tương thích với phiên bản Minecraft và mod loader đã công bố.
- Ghi lại các bước tái hiện lỗi, log liên quan và kết quả mong đợi.

Chỉ ghi lệnh build/test cụ thể sau khi repository có cấu hình tương ứng.

## 7. Quy ước Git

- Không commit API key, token, mật khẩu hoặc thông tin cá nhân.
- Không commit thư mục build/cache và tệp IDE không cần thiết.
- Không xóa hoặc ghi đè workspace/tài nguyên của người khác khi chưa kiểm tra.
- Dùng commit message ngắn, mô tả đúng thay đổi.

Ví dụ:

```text
feat: add dungeon generation
fix: correct dungeon spawn condition
docs: update setup instructions
refactor: reorganize dungeon procedures
```

## 8. Thông tin cần hoàn thiện

Cập nhật các thông tin sau khi dự án được cấu hình:

| Hạng mục | Giá trị |
|---|---|
| Phiên bản Minecraft | Chưa xác định |
| Mod loader | Chưa xác định |
| Phiên bản Java | Chưa xác định |
| Công cụ phát triển | Chưa xác định |
| Mod ID / namespace | Chưa xác định |
| Cách build và chạy | Chưa xác định |
| Tính năng chính | Chưa xác định |

## 9. Tài nguyên

- Repository: https://github.com/susu609/The8Dungeon
- GitHub Issues: https://github.com/susu609/The8Dungeon/issues
- GitHub Pull Requests: https://github.com/susu609/The8Dungeon/pulls

---

**Ghi chú:** README này là tài liệu khởi đầu. Hãy cập nhật hướng dẫn theo mã nguồn, mod loader, phiên bản Minecraft và quy trình build thực tế khi các thành phần đó được thêm vào repository.
