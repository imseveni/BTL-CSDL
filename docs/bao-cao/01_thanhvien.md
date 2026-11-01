# Module 01 – Quản lý thành viên

Người phụ trách: **Tú**. Gồm SQL (`sql/modules/01_thanhvien/`), Java (`src/main/thanhvien/`) và dữ liệu nền dùng chung (`sql/01_seed_master/seed_master.sql`). Menu chính: lựa chọn **1**.

## 1. Bảng và ràng buộc

| Cột | Kiểu và ý nghĩa | Ràng buộc trong schema |
|---|---|---|
| `MSV` | VARCHAR(20), mã sinh viên | Khóa chính |
| `HoVaTen` | VARCHAR(100) | NOT NULL |
| `NgaySinh` | DATE | Có thể NULL |
| `GioiTinh` | VARCHAR(20) | Có thể NULL |
| `Email`, `SDT` | VARCHAR(100), VARCHAR(20); thuộc tính **đơn trị** theo ERD | Có thể NULL |
| `NgayGiaNhap` | DATE | NOT NULL; CHECK `NgayGiaNhap >= NgaySinh` khi có ngày sinh |
| `TrangThaiHD` | VARCHAR(50) | Có thể NULL |
| `MaBan` | VARCHAR(20) | NOT NULL, FK đến `BanChuyenMon`; UPDATE CASCADE, DELETE RESTRICT |
| `ChucVu` | VARCHAR(100), chức vụ trong ban | Có thể NULL |

Quan hệ: một ban có nhiều thành viên (1–N), mỗi thành viên thuộc đúng một ban. Thành viên được tham chiếu bởi các bảng tham gia khóa học, dự án, sự kiện, bảng đánh giá, phiếu mượn, thu chi, quyền hạn và người quản lý thiết bị.

Các quy tắc thêm ngoài schema (nằm trong thủ tục và Service, cùng một bộ quy tắc):

- MSV bắt buộc, tối đa 20 ký tự, không trùng; họ tên bắt buộc, tối đa 100 ký tự.
- Giới tính chỉ nhận `Nam`, `Nữ`, `Khác`. Email phải có dạng `a@b.c`. Số điện thoại gồm 9 đến 15 chữ số, có thể bắt đầu bằng `+`.
- Ngày sinh và ngày gia nhập không ở tương lai; ngày gia nhập không trước ngày sinh.
- Trạng thái chỉ nhận `Đang hoạt động`, `Tạm nghỉ`, `Đã rời CLB`; để trống khi thêm thì mặc định `Đang hoạt động`. Chức vụ để trống thì mặc định `Thành viên`.
- Ban phải tồn tại.

## 2. View và thủ tục

| Đối tượng | Chức năng |
|---|---|
| `vw_ThanhVienChiTiet` | Thành viên kèm tên ban |
| `vw_ThongKeTrangThaiThanhVien` | Số thành viên theo trạng thái |
| `vw_LichSuHoatDong` | **Liên module:** gộp khóa học, dự án, sự kiện, đánh giá, phiếu mượn, thu chi của mỗi thành viên |
| `vw_TongQuanThanhVien` | **Liên module:** số lần tham gia ở từng mảng hoạt động |
| `sp_ThemThanhVien(...)` | Thêm thành viên, kiểm tra toàn bộ quy tắc ở trên |
| `sp_CapNhatThanhVien(MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT)` | Cập nhật thông tin cá nhân; ban và chức vụ đổi ở module 02 |
| `sp_DoiTrangThaiThanhVien(MSV, TrangThai)` | Đổi trạng thái hoạt động (cách nên dùng thay cho xóa) |
| `sp_XoaThanhVien(MSV)` | Chỉ xóa khi chưa có lịch sử hoạt động; ngược lại báo lỗi và gợi ý đổi trạng thái |
| `sp_TimThanhVien(TuKhoa, MaBan, TrangThai)` | Tìm theo một phần MSV hoặc họ tên; ban và trạng thái là bộ lọc tùy chọn |
| `sp_ChiTietThanhVien(MSV)` | Xem chi tiết một thành viên |
| `sp_ThanhVienGiaNhap(TuNgay, DenNgay)` | Danh sách thành viên gia nhập trong khoảng ngày |
| `sp_LichSuHoatDongThanhVien(MSV)` | Lịch sử hoạt động của thành viên, mới nhất trước |
| `sp_TongQuanThanhVien(MSV)` | Tổng quan số hoạt động của thành viên |

`sp_KiemTraThongTinCaNhan` là thủ tục dùng nội bộ để thêm và cập nhật dùng chung một bộ kiểm tra. Mọi thông báo lỗi đều là tiếng Việt, tối đa 128 ký tự (giới hạn của `SIGNAL`).

Ví dụ:

```sql
USE QuanLyCauLacBo;
CALL sp_ThemThanhVien('B21DCCN901', 'Nguyễn Văn Mới', '2004-03-01', 'Nam', 'moi@example.com',
                      '0912345678', '2026-09-01', NULL, 'BAN02', NULL);
CALL sp_TimThanhVien('Mới', NULL, NULL);
CALL sp_DoiTrangThaiThanhVien('B21DCCN901', 'Tạm nghỉ');
CALL sp_LichSuHoatDongThanhVien('B21DCCN001');
CALL sp_XoaThanhVien('B21DCCN901');
```

## 3. Dữ liệu nền (`seed_master.sql`)

Dữ liệu giả dùng chung cho cả nhóm, chạy lại được nhờ `INSERT IGNORE`:

- 5 ban `BAN01..BAN05`; 20 thành viên `B21DCCN001..B21DCCN020` (B21DCCN001 là Chủ nhiệm); 5 quyền hạn `QH01..QH05` và một số phân quyền mẫu.
- Các module khác chỉ tham chiếu mã trong file này, không sửa.

## 4. Java và menu

`ThanhVien`, `HoatDong`, `TongQuanThanhVien`, `ThongKeTrangThai` là các record dữ liệu; `ThanhVienDao` gọi thủ tục và view bằng `PreparedStatement`; `ThanhVienService` kiểm tra dữ liệu trước khi xuống CSDL; `ThanhVienUi` là menu console 10 chức năng. Khi cập nhật, menu hiện giá trị cũ: Enter để giữ nguyên, nhập `-` để xóa giá trị tùy chọn. Xóa cần nhập `y`.

## 5. Kiểm thử

- `tests.sql`: dữ liệu thử mang mã `T_TU_*`, tạo trong một transaction rồi ROLLBACK. Có các ca đúng (thêm có và không có giá trị tùy chọn, cập nhật, đổi trạng thái, xóa) và 24 ca sai, mỗi ca đối chiếu đúng `MYSQL_ERRNO` 1644.
- Java: `ThanhVienServiceTest` (7 test, Dao giả) và `ThanhVienMenuTest` (7 test, gồm xác nhận xóa, ngày không có thật, thêm đầy đủ, cập nhật giữ giá trị).
- Đã chạy trong môi trường soạn thảo: toàn bộ 52 test Java không cần MySQL đạt (47 đạt, 5 bị bỏ qua vì cần MySQL). Seed nền và các view của module được kiểm tra khớp schema bằng bản mô phỏng SQLite.
- **Chưa chạy trên MySQL thật:** các thủ tục và `tests.sql` của module này. Cần chạy theo mục "Cách chạy" của `TONG_HOP.md` và dán dòng `PASS module 01: dữ liệu thử đã rollback` vào đây.
