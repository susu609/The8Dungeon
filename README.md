# The8Dungeons

Mod Minecraft được phát triển bằng Java, Forge và Gradle.

## Công nghệ hiện tại

- Minecraft: `1.20.1`
- Minecraft Forge: `47.4.28`
- Java: `17`
- Mod ID: `the8dungeons`
- Gradle project name: `The8Dungeons`

## Lớp khởi tạo chính

Lớp khởi tạo mod được đề xuất:

`net.susu609.the8dungeons.The8DungeonsMod`

Tệp: `src/main/java/net/susu609/the8dungeons/The8DungeonsMod.java`

Lớp này dùng `@Mod(The8DungeonsMod.MODID)` để Forge nhận diện mod. Đây không phải `main()` của một ứng dụng Java độc lập; Forge là bên khởi tạo lớp mod.

## Cấu trúc mã nguồn đề xuất

```text
src/main/java/net/susu609/the8dungeons/
├── The8DungeonsMod.java   # Điểm khởi tạo và đăng ký thành phần chính
├── Config.java            # Khai báo và nạp cấu hình
├── registry/              # Đăng ký block, item, entity, menu...
├── block/                 # Các block riêng
├── item/                  # Các item riêng
├── entity/                # Sinh vật và thực thể
├── world/                 # Sinh thế giới, cấu trúc, biome, dungeon
├── event/                 # Xử lý sự kiện Forge
└── client/                # Renderer, màn hình và xử lý riêng phía client
```

Chỉ tạo các package khi có nội dung thực tế; không cần tạo toàn bộ ngay từ đầu.

## Chạy dự án

Mở thư mục dự án bằng IntelliJ IDEA, đồng bộ Gradle, sau đó chạy cấu hình `runClient`.

Trên Windows:

```bat
  gradle build runClient
```

Trên Linux/macOS:

```bash
  gradle build runClient
```

## Ghi chú

- `mod_id` phải viết thường và phải trùng với hằng `MODID` trong lớp `@Mod`.
- Package Java nên viết thường toàn bộ.
- Không dịch tên class, tên biến, tên phương thức, ID tài nguyên hoặc khóa cấu hình nếu chúng được dùng làm định danh trong code.
- Có thể Việt hóa comment và nội dung hiển thị/log dành cho người dùng.
