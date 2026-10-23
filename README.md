# Quản lý Câu lạc bộ – Bài tập lớn môn Cơ sở dữ liệu

Hệ thống quản lý Câu lạc bộ sinh viên: thành viên, ban chuyên môn, quyền hạn, khóa học, dự án,
sự kiện, thu–chi, đánh giá, cơ sở vật chất và mượn–trả.

**Công nghệ:** Java 17 · Maven · MySQL 8.0.16+ · JDBC · JUnit 5
**Giao diện:** menu console (khung ban đầu; có thể nâng cấp sau mà không đổi tầng dao/service).

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
│   ├── bao-cao/              báo cáo theo module
│   └── erd/                  hình ERD và mô hình quan hệ (2 file .png)
├── sql/
│   ├── 00_schema/schema.sql      tạo bảng (chỉ Tú sửa)
│   ├── 01_seed_master/           dữ liệu nền dùng chung
│   ├── modules/NN_<module>/      seed.sql, queries.sql, tests.sql của từng module
│   ├── run_all.sql               chạy toàn bộ theo thứ tự
│   └── reset_db.sql              xóa DB để dựng lại
├── src/main/
│   ├── app/            Main, MainMenu (đăng ký module)
│   ├── config/         DBConnection
│   ├── common/         AppModule, ConsoleUtil, DataAccessException
│   ├── thanhvien/      ThanhVienModule + (Dao, Service, Ui... do chủ module thêm)
│   ├── ... (10 thư mục module, mỗi thư mục một package phẳng)
│   └── resources/      db.properties.example
└── src/test/           test JUnit (app/MainMenuTest.java)
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
5. Chạy test: `mvn test`

## Thêm chức năng vào module của bạn
1. Viết SQL (seed, truy vấn, thủ tục, trigger) trong `sql/modules/NN_<module>/`.
2. Viết Java trong thư mục `src/main/<module>/`: thực thể → `Dao` → `Service` → `Ui`.
3. Gọi menu của bạn trong `<Ten>Module.run(...)`.
4. Viết test, cập nhật `docs/bao-cao/`, mở Pull Request vào `develop`.
