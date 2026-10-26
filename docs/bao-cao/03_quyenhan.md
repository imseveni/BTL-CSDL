# Module 03 – Quyền hạn

Người phụ trách: **Đông**. Đã triển khai SQL, Java và nối vào lựa chọn **3** của menu chính.

## 1. Bảng và ràng buộc

| Bảng/cột | Kiểu và ý nghĩa | Ràng buộc |
|---|---|---|
| `QuyenHan.MaQuyenHan` | VARCHAR(20), mã quyền | PK |
| `QuyenHan.TenQuyenHan` | VARCHAR(100), tên quyền | NOT NULL |
| `QuyenHan.MoTaChiTiet` | TEXT, diễn giải | Có thể NULL |
| `ThanhVien_QuyenHan.MSV` | VARCHAR(20), thành viên được cấp | FK → ThanhVien.MSV |
| `ThanhVien_QuyenHan.MaQuyenHan` | VARCHAR(20), quyền được cấp | FK → QuyenHan.MaQuyenHan |

`ThanhVien_QuyenHan` có PK ghép `(MSV, MaQuyenHan)`: một thành viên có nhiều quyền, một quyền cấp cho nhiều thành viên, mỗi cặp chỉ xuất hiện một lần. Hai FK đều ON UPDATE CASCADE và ON DELETE CASCADE. Xóa một quyền sẽ thu hồi quyền đó khỏi mọi thành viên; xóa thành viên cũng xóa các lần cấp quyền của người đó.

Service và thủ tục chặn mã/tên trống, mã quá 20 ký tự, tên quá 100 ký tự; mô tả trống được lưu NULL, mô tả không vượt dung lượng TEXT 65.535 byte UTF-8. Không áp đặt danh sách mã quyền cố định.

Danh mục quyền và việc cấp quyền thuộc dữ liệu nghiệp vụ. Chức năng đăng nhập, giới hạn thao tác theo tài khoản và user/role MySQL thuộc phần tích hợp/phân quyền của Tú trong PHAN_CONG; module này chưa thực thi cơ chế đó.

## 2. Thủ tục và truy vấn

| Đối tượng SQL | Chức năng |
|---|---|
| `sp_ThemQuyenHan(Ma, Ten, MoTa)` | Thêm quyền |
| `sp_SuaQuyenHan(Ma, Ten, MoTa)` | Sửa tên/mô tả; mã không đổi |
| `sp_XoaQuyenHan(Ma)` | Xóa quyền và cascade các lần cấp quyền |
| `sp_CapQuyen(MSV, Ma)` | Kiểm tra MSV/quyền; PK ghép chặn cấp trùng |
| `sp_ThuHoiQuyen(MSV, Ma)` | Thu hồi; báo lỗi nếu thành viên chưa có quyền |
| `sp_QuyenCuaThanhVien(MSV)` | Danh sách quyền, tên và mô tả của thành viên |
| `sp_ThanhVienCoQuyen(Ma)` | Danh sách MSV, họ tên, mã ban, chức vụ |

Danh sách toàn bộ quyền: `SELECT MaQuyenHan, TenQuyenHan, MoTaChiTiet FROM QuyenHan ORDER BY MaQuyenHan`.

Ví dụ:

```sql
USE QuanLyCauLacBo;
CALL sp_ThemQuyenHan('QH03_DEMO', 'Quyền minh họa', NULL);
CALL sp_SuaQuyenHan('QH03_DEMO', 'Xem báo cáo', 'Đọc báo cáo nhân sự');
-- Thay MSV_THUC_TE bằng mã thành viên đã có trong DB:
CALL sp_CapQuyen('MSV_THUC_TE', 'QH03_DEMO');
CALL sp_QuyenCuaThanhVien('MSV_THUC_TE');
CALL sp_ThanhVienCoQuyen('QH03_DEMO');
CALL sp_ThuHoiQuyen('MSV_THUC_TE', 'QH03_DEMO');
CALL sp_XoaQuyenHan('QH03_DEMO');
```

Mỗi thao tác ghi là một DML; FK và PK bảo đảm tính toàn vẹn. Xóa quyền cùng cascade được MySQL thực hiện nguyên tử. Thủ tục không tự commit.

## 3. Java và menu

`QuyenHan`, `ThanhVienCoQuyen` là record dữ liệu. `QuyenHanDao` gọi thủ tục bằng tham số PreparedStatement, đóng tài nguyên bằng try-with-resources. `QuyenHanService` kiểm tra đầu vào. `QuyenHanUi` có đủ 8 mục: danh sách, thêm, sửa, xóa, cấp, thu hồi, quyền của thành viên, thành viên có quyền. Trước khi xóa, menu nêu rõ tác động thu hồi hàng loạt và yêu cầu nhập `y`.

## 4. Seed và kết quả kiểm thử

Seed thêm `QH03_XEM`, `QH03_QLNS`, `QH03_DG`, tránh trùng mã QH01/QH02 dự kiến của Tú. Có thể chạy lại mà không ghi đè dữ liệu cũ. Không tự cấp quyền cho thành viên thật khi chưa có mã seed nền được chốt; ca cấp quyền mẫu nằm trong transaction của tests.sql.

`tests.sql` kiểm tra thêm/sửa quyền, sửa lại cùng giá trị, cấp cho hai thành viên, tra cứu hai chiều, thu hồi, danh sách trống sau thu hồi, xóa cascade. Các ca sai kiểm tra mã trùng, tên trống, MSV/quyền không tồn tại, cấp trùng và thu hồi quyền chưa có. Fixture tự rollback.

Kết quả MySQL 8.0.40: `PASS module 03: dữ liệu thử đã rollback`. 3 test service, test xác nhận xóa trên menu và ca tích hợp JDBC cấp/thu hồi/tra cứu/cascade đều đạt. Xem [biên bản kiểm thử](DONG_BAN_GIAO.md).

## 5. Bổ sung mô hình quan hệ

Hai bảng `QuyenHan` và `ThanhVien_QuyenHan` được thể hiện trong [sơ đồ bổ sung nhân sự](../erd/Mo_hinh_quan_he_nhan_su.svg), kèm PK ghép, FK và lực lượng quan hệ. Có [bản PNG](../erd/Mo_hinh_quan_he_nhan_su.png) để đưa vào báo cáo tổng hợp.
