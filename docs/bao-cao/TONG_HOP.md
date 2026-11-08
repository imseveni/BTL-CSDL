# Báo cáo tổng hợp – Quản lý Câu lạc bộ

Tài liệu của Tú: trạng thái toàn dự án, cách dựng và kiểm tra, và những việc còn lại trước khi nộp.

## 1. Kiến trúc

- **CSDL:** MySQL 8.0.16 trở lên, 16 bảng (`sql/00_schema/schema.sql`), hình mô hình quan hệ đầy đủ ở `docs/erd/Mo_hinh_quan_he_day_du.png`.
- **Ứng dụng:** Java 17, Maven, JDBC. Mặc định mở cửa sổ Swing 10 tab (mỗi module một tab); module nào chưa có màn hình thì hiện màn hình tạm. Menu dòng lệnh cũ chạy bằng tham số `--console`. Tiến độ chuyển sang Swing: xem [PHAN_CONG_SWING.md](../PHAN_CONG_SWING.md).
- **Cấu hình kết nối:** một nơi duy nhất, `common.Database`. Thứ tự đọc: biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`, rồi `db.properties`, `database.properties`, `database-local.properties`, hoặc cấu hình nhập trong cửa sổ Swing.
- **Phân quyền MySQL:** `sql/02_security/phan_quyen.sql` (vai trò Thành viên, Ban Chủ nhiệm, Quản trị).

## 2. Trạng thái từng module

| Module | Người làm | Báo cáo | Đã chạy trên MySQL thật |
|---|---|---|---|
| 01 Thành viên | Tú | [01_thanhvien.md](01_thanhvien.md) | Chưa |
| 02 Ban chuyên môn | Đông | [02_banchuyenmon.md](02_banchuyenmon.md) | Có (8.0.40) |
| 03 Quyền hạn | Đông | [03_quyenhan.md](03_quyenhan.md) | Có (8.0.40) |
| 04 Khóa học | Chiến | [04_khoahoc.md](04_khoahoc.md) | Có (8.0.46) |
| 05 Dự án | Chiến | [05_duan.md](05_duan.md) | Có (8.0.46) |
| 06 Sự kiện | Minh | [06_sukien.md](06_sukien.md) | Chưa ghi nhận |
| 07 Thu – Chi | Minh | [07_thuchi.md](07_thuchi.md) | Chưa ghi nhận |
| 08 Đánh giá | Đông | [08_danhgia.md](08_danhgia.md) | Có (8.0.40) |
| 09 Cơ sở vật chất | Trường | [09_cosovatchat.md](09_cosovatchat.md) | Chưa (theo biên bản bàn giao) |
| 10 Mượn – Trả | Trường | [10_muontra.md](10_muontra.md) | Chưa (theo biên bản bàn giao) |

Các module đã chạy riêng lẻ trên MySQL, nhưng **chưa có bằng chứng cả dự án chạy liền từ DB trống** sau khi gộp. Đây là việc phải làm trước khi nộp (mục 4).

## 3. Các điểm tích hợp đã xử lý (Tú)

- **MSVQuanLy:** schema từng bị chuyển cột người quản lý sang `ChiTietMuon` (theo khung kết tập trong ERD) làm lệch với module 09 và 10 đã hoàn thành. Đã đưa lại về `CoSoVatChat`; xem điểm #4 trong [DECISIONS.md](../DECISIONS.md).
- **Cấu hình DB:** module console đọc `db.properties` còn module Swing đọc `database.properties`/biến môi trường; nay cùng đi qua `common.Database`.
- **Menu chính:** `SwingApp` và `ClubWorkspace` nằm ở package mặc định nên không gọi được từ menu; đã chuyển vào package `app` và thêm `SwingLauncherModule`. Menu có đủ 10 module, một module lỗi không làm sập chương trình.
- **Dữ liệu nền:** `seed_master.sql` được viết. Module 06 tham chiếu `B21DCCN001..004` (có trong seed nền); module 03 dùng mã quyền riêng (dạng `QH03_...`) nên không trùng `QH01..QH05`. Đã kiểm tra bằng bản mô phỏng SQLite: schema, seed nền và seed của 10 module nạp liền không vi phạm khóa ngoại (riêng một câu `UPDATE` cú pháp MySQL ở cuối seed module 10 không chạy được trên SQLite nên chưa kiểm tra).

## 4. Cách dựng và kiểm tra (chạy trên MySQL của bạn)

```bash
cd sql
mysql -u root -p < reset_db.sql
mysql -u root -p < run_all.sql                 # phải thoát với mã 0
mysql -u root -p < 02_security/phan_quyen.sql  # tùy chọn, cần quyền CREATE ROLE
for m in 01_thanhvien 02_banchuyenmon 03_quyenhan 04_khoahoc 05_duan 06_sukien 07_thuchi 08_danhgia 09_cosovatchat 10_muontra; do
  mysql -u root -p < modules/$m/tests.sql      # mong đợi dòng PASS ... của từng module
done
cd ..
mvn test                                       # test không cần MySQL
mvn test -Dclb.mysql.tests=true                # thêm test tích hợp, cần DB đã chạy run_all.sql
mvn compile exec:java                          # chạy ứng dụng (cửa sổ Swing; thêm -Dexec.args="--console" để dùng menu dòng lệnh)
```

Nếu `tests.sql` của module nào không in dòng PASS, dán lỗi vào Issue cho người phụ trách module đó.

## 5. Việc còn lại trước khi nộp

- [ ] Chạy đủ lệnh ở mục 4 trên MySQL thật; ghi kết quả vào bảng mục 2 (đặc biệt module 01, 06, 07, 09, 10).
- [ ] Cả nhóm xác nhận các điểm còn ☐ trong [DECISIONS.md](../DECISIONS.md), nhất là #4 (người quản lý thiết bị) và #11 (ThuChi.MSV, MaSK có bắt buộc không).
- [ ] Nếu báo cáo cần hình vẽ tay: chép `Mo_hinh_quan_he_day_du.png` vào trang 3 của draw.io.
- [ ] Bật bảo vệ nhánh `main`, `develop` trên GitHub (bắt buộc Pull Request).
- [ ] Rà lại tên tác giả trong lịch sử Git và README trước khi nộp.

## 6. Bản demo module Thành viên

Module Thành viên đã đủ các tiêu chí 1, 3, 4, 5, 6, 7 và báo cáo mục 8 (ngắn): xem [`docs/bao-cao-btl/thanh-vien/`](../bao-cao-btl/thanh-vien/BAO_CAO_THANH_VIEN.md) và kịch bản demo [`KICH_BAN_DEMO.md`](../bao-cao-btl/thanh-vien/KICH_BAN_DEMO.md). Tab Thành viên trong ứng dụng gồm hai tab con: *Quản lý thành viên* (có tìm kiếm, thêm, sửa, quyền hạn, lịch sử hoạt động) và *Báo cáo* (xem trên màn hình, xuất Excel).

## 7. Tiến độ giao diện Swing

| Module | Màn hình Swing | Người làm |
|---|---|---|
| 01 Thành viên | Xong (mẫu của nhóm; có tab Báo cáo và tab Quyền hạn) | Tú |
| 02 Ban chuyên môn | Chưa | Đông |
| 03 Quyền hạn | Chưa | Đông |
| 04 Khóa học | Xong | Chiến |
| 05 Dự án | Xong | Chiến |
| 06 Sự kiện | Chưa | Minh |
| 07 Thu – Chi | Chưa | Minh |
| 08 Đánh giá | Chưa | Đông |
| 09 Cơ sở vật chất | Chưa | Trường |
| 10 Mượn – Trả | Chưa | Trường |

Cập nhật bảng này khi PR của từng module được merge.
