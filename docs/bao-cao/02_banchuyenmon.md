# Module 02 – Ban chuyên môn và chức vụ

Người phụ trách: **Đông**. Đã triển khai SQL, Java theo mô hình thực thể → Dao → Service → Ui và nối vào lựa chọn **2** của menu chính.

## 1. Bảng và quan hệ

| Bảng/cột | Kiểu và ý nghĩa | Ràng buộc trong schema |
|---|---|---|
| `BanChuyenMon.MaBan` | VARCHAR(20), mã ban | Khóa chính |
| `BanChuyenMon.TenBan` | VARCHAR(100), tên ban | NOT NULL |
| `ThanhVien.MSV` | VARCHAR(20), thành viên được phân ban | Khóa chính của bảng Thành viên |
| `ThanhVien.MaBan` | VARCHAR(20), ban hiện tại | NOT NULL, FK đến BanChuyenMon; UPDATE CASCADE, DELETE RESTRICT |
| `ThanhVien.ChucVu` | VARCHAR(100), chức vụ hiện tại | Có thể NULL theo schema |

Một ban có 0..N thành viên; mỗi thành viên thuộc đúng một ban. Phân ban và đổi chức vụ cập nhật cột có sẵn trong `ThanhVien`, không cần sửa module 01 hoặc schema. Đây là thông tin hiện tại; schema chưa có bảng lưu lịch sử chuyển ban/chức vụ.

Service và thủ tục yêu cầu mã/tên/chức vụ khi ghi không rỗng, kiểm tra độ dài, loại khoảng trắng ở hai đầu. Thành viên có chức vụ NULL từ dữ liệu cũ vẫn được tra cứu bình thường. Việc chuyển ban giữ nguyên chức vụ; nếu cơ cấu mới yêu cầu chức vụ khác, dùng chức năng đổi chức vụ.

## 2. Truy vấn và thủ tục

| Đối tượng SQL | Chức năng |
|---|---|
| `vw_BanChuyenMonThongKe` | Danh sách ban và COUNT thành viên, kể cả ban có 0 thành viên |
| `sp_ThemBan(MaBan, TenBan)` | Thêm ban; PK chặn mã trùng |
| `sp_SuaBan(MaBan, TenBan)` | Sửa tên, báo lỗi nếu ban không tồn tại |
| `sp_XoaBan(MaBan)` | Chặn ban còn thành viên bằng thông báo dễ hiểu; FK tiếp tục bảo vệ dữ liệu |
| `sp_ChuyenBan(MSV, MaBan)` | Phân/chuyển sang ban tồn tại |
| `sp_DoiChucVu(MSV, ChucVu)` | Gán/đổi chức vụ cho thành viên tồn tại |
| `sp_ThanhVienTheoBan(MaBan)` | MSV, họ tên, chức vụ, trạng thái, sắp xếp theo MSV |

Ví dụ sau khi dựng DB và có thành viên thật:

```sql
USE QuanLyCauLacBo;
CALL sp_ThemBan('BCM02_DEMO', 'Ban Thử nghiệm');
CALL sp_SuaBan('BCM02_DEMO', 'Ban Thử nghiệm mới');
SELECT * FROM vw_BanChuyenMonThongKe ORDER BY MaBan;
CALL sp_XoaBan('BCM02_DEMO'); -- ban rỗng: xóa được
-- Thay MSV_THUC_TE bằng mã thành viên đã có trong DB:
CALL sp_ChuyenBan('MSV_THUC_TE', 'BCM02_CN');
CALL sp_DoiChucVu('MSV_THUC_TE', 'Trưởng ban');
CALL sp_ThanhVienTheoBan('BCM02_CN');
```

Các thao tác chỉ ghi một câu lệnh DML, không có chuỗi ghi nhiều bảng cần transaction riêng. Thủ tục không tự commit, nên có thể được dùng trong transaction của bên gọi. Mã ban là định danh và không được thay đổi qua menu sửa tên.

## 3. Java và menu

- `BanChuyenMon`, `BanThongKe`, `ThanhVienTrongBan`: các record dữ liệu.
- `BanChuyenMonDao`: PreparedStatement, try-with-resources, gọi thủ tục/view, bọc lỗi JDBC trong DataAccessException.
- `BanChuyenMonService`: kiểm tra và chuẩn hóa dữ liệu nhập.
- `BanChuyenMonUi`: thêm/sửa/xóa, danh sách và số lượng, danh sách theo ban, chuyển ban, đổi chức vụ. Xóa cần nhập `y`; nhập sai hoặc lỗi DB sẽ trở lại menu.

## 4. Dữ liệu mẫu và kiểm thử

`seed.sql` thêm ba ban `BCM02_CN`, `BCM02_TT`, `BCM02_NS`, chạy lại không chèn trùng và không sửa dữ liệu cũ. Không tạo thành viên thay cho seed nền của Tú.

`tests.sql` tạo fixture `T_DONG_*` trong transaction và tự ROLLBACK cả khi thành công lẫn thất bại. Bao phủ thêm/sửa/xóa ban rỗng, sửa lại cùng giá trị, ban trống trong thống kê, chuyển ban, gán chức vụ, đếm thành viên; các ca sai gồm mã trùng, tên rỗng, mã quá dài, mã không tồn tại, xóa ban có thành viên và chức vụ rỗng. Lỗi mong đợi được đối chiếu đúng MYSQL_ERRNO, không coi mọi lỗi SQL là kết quả đạt.

Đã chạy trên MySQL 8.0.40: `PASS module 02: dữ liệu thử đã rollback`. Java: 3 test service, kiểm thử menu và 1 ca tích hợp JDBC cho toàn bộ luồng module 02 đều đạt. Xem [biên bản kiểm thử và cách chạy](DONG_BAN_GIAO.md).
