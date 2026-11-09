# Module 08 – Đánh giá thành viên

Người phụ trách: **Đông**. Đã triển khai SQL, Java và nối vào lựa chọn **8** của menu chính.

## 1. Bảng và ràng buộc

| Cột DanhGia | Kiểu | Ý nghĩa/ràng buộc schema |
|---|---|---|
| `MaGhiNhan` | VARCHAR(20) | PK, mã quyết định/bản ghi |
| `LoaiDanhGia` | VARCHAR(100) | Loại đánh giá, có thể NULL theo schema |
| `HinhThuc` | VARCHAR(100) | Hình thức thực hiện, có thể NULL |
| `LyDo` | TEXT | Nội dung lý do, có thể NULL |
| `NgayQuyetDinh` | DATE | NOT NULL |
| `MSV` | VARCHAR(20) | NOT NULL, FK → ThanhVien.MSV, UPDATE CASCADE, DELETE RESTRICT |

Một thành viên có 0..N đánh giá; mỗi đánh giá thuộc đúng một thành viên. FK ngăn xóa thành viên đang có đánh giá để bảo toàn lịch sử.

Khi tạo/cập nhật qua service và thủ tục, bắt buộc nhập đầy đủ mã, MSV, loại, hình thức, lý do và ngày; kiểm tra độ dài VARCHAR và dung lượng TEXT (65.535 byte UTF-8). Ngày dùng ISO `yyyy-MM-dd`, phân tích nghiêm ngặt, giới hạn trong miền DATE của MySQL (1000-01-01 đến 9999-12-31). Không tự thêm quy tắc cấm ngày tương lai vì tài liệu chưa yêu cầu.

Danh mục LoaiDanhGia chưa được nhóm xác nhận: giữ VARCHAR, gợi ý **Khen thưởng**, **Kỷ luật**, vẫn cho phép loại khác. Dữ liệu nhập được bỏ khoảng trắng ở hai đầu; không tự gộp các tên khác nghĩa. Khi Tú chốt danh mục, cần thống nhất cách ghi giữa các người nhập. Không đánh dấu thay nhóm các mục xác nhận trong DECISIONS.md.

## 2. Thủ tục và thống kê

| Đối tượng SQL | Chức năng |
|---|---|
| `sp_ThemDanhGia(Ma, Loai, HinhThuc, LyDo, Ngay, MSV)` | Tạo bản ghi, kiểm tra MSV tồn tại |
| `sp_SuaDanhGia(Ma, Loai, HinhThuc, LyDo, Ngay, MSV)` | Cập nhật toàn bộ nội dung/đối tượng của bản ghi có sẵn |
| `sp_LichSuDanhGia(MSV)` | Lịch sử ngày quyết định giảm dần, cùng ngày sắp theo mã tăng dần |
| `vw_ThongKeDanhGia` | COUNT bản ghi và COUNT DISTINCT thành viên theo loại |

View hiển thị dữ liệu cũ có loại NULL/trống thành nhóm `(Chưa phân loại)`, nên không mất bản ghi khi tổng hợp. Thống kê tuân theo collation utf8mb4_unicode_ci của schema chung. Mã ghi nhận được giữ nguyên khi sửa. Chức năng xóa đánh giá không nằm trong phân công nên không thêm mục xóa lịch sử.

```sql
USE QuanLyCauLacBo;
-- Thay MSV_THUC_TE bằng mã thành viên có sẵn:
CALL sp_ThemDanhGia('DG08_DEMO', 'Khen thưởng', 'Giấy khen',
    'Đóng góp tổ chức sự kiện', '2026-10-01', 'MSV_THUC_TE');
CALL sp_SuaDanhGia('DG08_DEMO', 'Khen thưởng', 'Biểu dương',
    'Hoàn thành tốt nhiệm vụ sự kiện', '2026-10-02', 'MSV_THUC_TE');
CALL sp_LichSuDanhGia('MSV_THUC_TE');
SELECT * FROM vw_ThongKeDanhGia ORDER BY LoaiDanhGia;
```

## 3. Java và seed

`DanhGia`, `ThongKeDanhGia` là record dữ liệu; Dao chỉ truy cập JDBC, Service kiểm tra nghiệp vụ, Ui nhập/xuất. Menu gồm tạo, cập nhật, lịch sử theo MSV và thống kê theo loại. Lỗi ngày không tồn tại như 2026-02-30 bị chặn trước khi ghi DB.

Seed `DG08_01` tham chiếu MSV đầu tiên **thực sự tồn tại** trong ThanhVien, không tự tạo/suy đoán mã nền. Nếu seed_master chưa có thành viên, chèn 0 dòng; sau khi Tú bổ sung thành viên, có thể chạy lại seed.sql. Nếu mã DG08_01 đã có thì giữ nguyên. Đây là dữ liệu minh họa cho DB bài tập.

## 4. Kết quả kiểm thử

`tests.sql` kiểm tra tạo ba bản ghi cho hai người; thống kê 3 đánh giá/2 thành viên; cập nhật loại, hình thức, lý do, ngày và MSV; thống kê thay đổi theo dữ liệu mới; tra cứu lịch sử. Ca sai: mã trùng, MSV không tồn tại, loại/hình thức/lý do/ngày trống, sửa mã không có và xóa thành viên đã nhận đánh giá. Fixture tự rollback.

MySQL 8.0.40: `PASS module 08: dữ liệu thử đã rollback`. 3 test service, test menu ngày sai và ca tích hợp JDBC xác nhận thứ tự lịch sử, UTF-8, cập nhật và đếm thành viên phân biệt đều đạt. Xem [biên bản kiểm thử](DONG_BAN_GIAO.md).

## 5. Bổ sung mô hình quan hệ

Bảng DanhGia cùng liên kết bắt buộc đến ThanhVien có trong [sơ đồ nhân sự SVG](../erd/Mo_hinh_quan_he_nhan_su.svg) và [PNG](../erd/Mo_hinh_quan_he_nhan_su.png). Sơ đồ này bổ sung ba bảng nhân sự thiếu ở mô hình trang 3 theo DECISIONS #10; phần cơ sở vật chất và mượn trả vẫn thuộc Trường.
