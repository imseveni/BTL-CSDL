# Bàn giao phần việc của Đông

Phạm vi được đối chiếu với README, PHAN_CONG, HUONG_DAN_THUC_HIEN, CONTRIBUTING, DECISIONS, hai hình ERD và schema hiện có. Hoàn thành module **02, 03, 08** trên nhánh `feature/dong-nhan-su`.

## Chức năng đã làm

- [x] 02: thêm/sửa/xóa/tra cứu ban, số lượng và danh sách thành viên, phân/chuyển ban, gán/đổi chức vụ.
- [x] 03: thêm/sửa/xóa/tra cứu quyền, cấp/thu hồi, tra cứu quyền của thành viên và thành viên có quyền.
- [x] 08: tạo/cập nhật đủ thông tin đánh giá, lịch sử theo thành viên, tổng hợp số đánh giá và thành viên theo loại.
- [x] Ba bộ seed/queries/tests SQL; các lớp thực thể/Dao/Service/Ui; nối menu 2, 3, 8.
- [x] Báo cáo [02](02_banchuyenmon.md), [03](03_quyenhan.md), [08](08_danhgia.md).
- [x] Bổ sung mô hình quan hệ Quyền hạn, TV–Quyền hạn, Đánh giá: [SVG](../erd/Mo_hinh_quan_he_nhan_su.svg), [PNG](../erd/Mo_hinh_quan_he_nhan_su.png), [mô tả](../erd/Mo_hinh_quan_he_nhan_su.md).

## Biên bản kiểm thử thực tế – 25/10/2026

Môi trường: MySQL Community **8.0.40**, Connector/J **8.3.0**, Java/Maven theo pom hiện có. MySQL được chạy trên một instance tạm riêng trong target, địa chỉ loopback cổng 33317; không dùng cơ sở dữ liệu cá nhân sẵn có. Thư viện kiểm thử là JUnit 5.10.2. Mã nguồn được kiểm tra tương thích API Java 17 bằng `javac --release 17`.

| Kiểm tra | Kết quả |
|---|---|
| Thực thi toàn bộ nội dung run_all.sql theo đúng thứ tự SOURCE từ DB trống | Đạt, exit 0 |
| Chạy lại seed và queries của 02/03/08 | Đạt, không trùng mã/đối tượng |
| Seed đánh giá khi chưa có/có thành viên, và chạy lại | Đạt: 0 dòng khi thiếu MSV; 1 bản ghi khi có MSV; không chèn trùng |
| tests.sql module 02 | PASS |
| tests.sql module 03 | PASS |
| tests.sql module 08 | PASS |
| Chạy lại cả ba tests.sql | PASS |
| Dữ liệu fixture T_DONG_* sau kiểm thử | 0 thành viên, 0 ban, 0 quyền, 0 đánh giá |
| JUnit toàn dự án, bật kiểm thử MySQL | 18 ca, 0 lỗi, 0 thất bại, 0 bỏ qua |

Trích output thật:

```text
PASS module 02: dữ liệu thử đã rollback
PASS module 03: dữ liệu thử đã rollback
PASS module 08: dữ liệu thử đã rollback

ThanhVienThu  BanThu  QuyenThu  DanhGiaThu
0            0       0         0

Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Trong 18 ca JUnit có 1 ca menu chính đã có sẵn, 9 ca service mới, 5 ca menu mới và 3 ca tích hợp MySQL mới. Mỗi ca tích hợp thực hiện một chuỗi nghiệp vụ đầy đủ qua Service → Dao → JDBC → thủ tục/view, kiểm tra dữ liệu trả về và lỗi thực tế. Ca tích hợp dùng mã riêng ngẫu nhiên và dọn đúng các bản ghi thử sau khi chạy.

## Cách chạy lại

1. Theo README để dựng MySQL và cấu hình db.properties. Khi chạy kiểm thử tích hợp, dùng **DB thử riêng**, vì các ca này chèn rồi xóa fixture.
2. Từ thư mục sql, chạy run_all.sql trên DB thử trống như README. Trong mysql client:

```sql
SOURCE modules/02_banchuyenmon/tests.sql;
SOURCE modules/03_quyenhan/tests.sql;
SOURCE modules/08_danhgia/tests.sql;
```

3. Trong thư mục dự án:

```text
mvn test
mvn "-Dclb.mysql.tests=true" test
mvn compile exec:java
```

`mvn test` mặc định chạy 15 ca không cần DB và bỏ qua 3 ca MySQL. Lệnh thứ hai bật cả 18 ca, cần cấu hình kết nối trên classpath và DB đã có thủ tục/view. Ứng dụng dùng các mục **2**, **3**, **8**; nhập **0** để quay lại. Khi chạy lần đầu, cần copy db.properties.example theo README, không đưa mật khẩu vào Git.

## Điểm cần Tú tích hợp

- `seed_master.sql` hiện còn TODO: chưa có thành viên nền hoặc mã MSV được nhóm chốt. Module 02/03 vẫn có dữ liệu danh mục; seed đánh giá chèn 0 dòng nếu chưa có thành viên. Sau khi Tú điền seed nền, chạy lại seed đánh giá để có ví dụ. Chức năng nhận MSV cần thành viên đã tồn tại.
- LoaiDanhGia chưa có danh mục được xác nhận. Hiện gợi ý Khen thưởng/Kỷ luật, cho nhập loại khác theo VARCHAR của schema. Cần thống nhất danh mục/cách ghi trước khi tổng hợp số liệu chính thức.
- Dùng sơ đồ bổ sung nhân sự trong báo cáo tổng hợp; việc bổ sung ba bảng cơ sở vật chất/mượn trả thuộc Trường.
- Chưa commit/push/mở PR. Checkout hiện chỉ có main và origin/main, chưa có develop; việc mở PR vào develop cần Tú tạo nhánh tích hợp theo quy trình nhóm.

Các tệp schema, seed_master, run_all, pom.xml và các package app/config/common được giữ nguyên theo phạm vi phân công.
