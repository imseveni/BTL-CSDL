# Quản lý Câu lạc bộ – Bài tập lớn môn Cơ sở dữ liệu

Hệ thống quản lý Câu lạc bộ sinh viên: thành viên, ban chuyên môn, quyền hạn, khóa học, dự án,
sự kiện, thu–chi, đánh giá, cơ sở vật chất và mượn–trả.

**Công nghệ:** Java 17 · Maven · MySQL 8.0.16+ · JDBC · JUnit 5
**Giao diện:** menu console cho 10 module; Khóa học và Dự án có giao diện đồ họa Swing, mở từ menu chính (lựa chọn 4 và 5).

## Thành viên nhóm
| Thành viên | Vai trò | Module |
|---|---|---|
| Tú | Trưởng nhóm: khung code, chia việc, merge, xử lý conflict | 01 + tích hợp |
| Đông | Nhân sự | 02, 03, 08 |
| Chiến | Hoạt động đào tạo | 04, 05 |
| Minh | Sự kiện & tài chính | 06, 07 |
| Trường | Cơ sở vật chất | 09, 10 |

**Bắt đầu từ đây:** [`docs/HUONG_DAN_THUC_HIEN.md`](docs/HUONG_DAN_THUC_HIEN.md) (cách làm phần việc của từng người) ·
Chi tiết nhiệm vụ: [`docs/PHAN_CONG.md`](docs/PHAN_CONG.md) · Quy ước: [`CONTRIBUTING.md`](CONTRIBUTING.md) ·
Điểm cần chốt: [`docs/DECISIONS.md`](docs/DECISIONS.md)

## Cấu trúc dự án
```
quan-ly-clb/
├── pom.xml
├── README.md
├── CONTRIBUTING.md
├── docs/
│   ├── HUONG_DAN_THUC_HIEN.md  hướng dẫn từng người làm phần của mình
│   ├── PHAN_CONG.md          phân công + checklist từng người
│   ├── DECISIONS.md          các điểm mâu thuẫn cần chốt
│   ├── bao-cao/              báo cáo từng module + TONG_HOP.md (trạng thái toàn dự án)
│   └── erd/                  ERD, mô hình quan hệ đầy đủ (Mo_hinh_quan_he_day_du.png/.svg)
├── sql/
│   ├── 00_schema/schema.sql      tạo bảng (chỉ Tú sửa)
│   ├── 01_seed_master/           dữ liệu nền dùng chung
│   ├── 02_security/phan_quyen.sql  vai trò và quyền MySQL (chạy riêng, cần quyền root)
│   ├── modules/NN_<module>/      seed.sql, queries.sql, tests.sql của từng module
│   ├── run_all.sql               chạy toàn bộ theo thứ tự
│   └── reset_db.sql              xóa DB để dựng lại
├── src/main/
│   ├── app/            Main, MainMenu, SwingApp (đăng ký module)
│   ├── config/         DBConnection (dùng chung cấu hình với common.Database)
│   ├── common/         AppModule, Database, Validation, ConsoleUtil, DataAccessException
│   ├── thanhvien/      ThanhVienModule + (Dao, Service, Ui... do chủ module thêm)
│   ├── ... (10 thư mục module, mỗi thư mục một package phẳng)
│   └── resources/      db.properties.example
└── src/test/           test JUnit của từng module
```

## Cách chạy
1. Cài **JDK 17+**, **Maven 3.8+**, **MySQL 8.0.16+**.
2. Dựng CSDL (chạy trong thư mục `sql/`):
   ```bash
   cd sql
   mysql -u root -p < run_all.sql
   ```
   Dựng lại từ đầu: `mysql -u root -p < reset_db.sql` rồi chạy lại `run_all.sql`.
3. Cấu hình kết nối:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   # sửa db.user, db.password cho máy của bạn (file này không được commit)
   ```
4. Chạy ứng dụng:
   ```bash
   mvn compile exec:java
   ```
5. Chạy test: `mvn test` (không cần MySQL); thêm test tích hợp: `mvn test -Dclb.mysql.tests=true` (cần DB đã chạy `run_all.sql`).
6. Giao diện đồ họa chạy riêng: `mvn compile exec:java -Dexec.mainClass=app.SwingApp`.
7. Phân quyền MySQL (tùy chọn): `mysql -u root -p < sql/02_security/phan_quyen.sql`.

Trạng thái toàn dự án và việc còn lại: [`docs/bao-cao/TONG_HOP.md`](docs/bao-cao/TONG_HOP.md).

> **Chữ tiếng Việt bị hiện thành `?`:** chương trình đã tự đổi console sang UTF-8 khi chạy trong Command Prompt, PowerShell, Windows Terminal. Nếu chạy trong IDE thì đặt encoding của cửa sổ chạy thành UTF-8: IntelliJ IDEA thêm `-Dfile.encoding=UTF-8` vào VM options của cấu hình Run; Eclipse chọn Run Configurations, tab Common, mục Encoding, chọn UTF-8; VS Code chạy `chcp 65001` trong terminal trước. Nên dùng phông chữ hỗ trợ tiếng Việt cho console (ví dụ Consolas hoặc Cascadia Mono).

## Thêm chức năng vào module của bạn
1. Viết SQL (seed, truy vấn, thủ tục, trigger) trong `sql/modules/NN_<module>/`.
2. Viết Java trong thư mục `src/main/<module>/`: thực thể → `Dao` → `Service` → `Ui`.
3. Gọi menu của bạn trong `<Ten>Module.run(...)`.
4. Viết test, cập nhật `docs/bao-cao/`, mở Pull Request vào `develop`.
