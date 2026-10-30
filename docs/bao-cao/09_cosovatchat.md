# Module 09 – Cơ sở vật chất

Người phụ trách: **Trường**. Đã triển khai SQL, Java và nối vào menu chính qua `CoSoVatChatModule`.

## 1. Bảng và các cột

| Cột CoSoVatChat | Kiểu | Ý nghĩa/ràng buộc schema |
|---|---|---|
| `MaThietBi` | VARCHAR(20) | PK, mã thiết bị |
| `TenThietBi` | VARCHAR(150) | NOT NULL |
| `TongSoLuong` | INT | NOT NULL, CHECK `>= 0` |
| `SoLuongKhaDung` | INT | NOT NULL, CHECK `0 <= SoLuongKhaDung <= TongSoLuong` |
| `MSVQuanLy` | VARCHAR(20) | Cho phép NULL (chưa có người quản lý), FK → ThanhVien.MSV, UPDATE CASCADE, DELETE RESTRICT |

Quan hệ "Quản lý": 1 thành viên quản lý nhiều thiết bị; mỗi thiết bị có tối đa 1 người quản lý. Cột `TinhTrang` không có trong ERD nên đã bỏ (DECISIONS #3); `MSVQuanLy` nằm ở bảng này theo DECISIONS #4. FK `DELETE RESTRICT` ngăn xóa thành viên đang quản lý thiết bị.

## 2. Ràng buộc và quy ước

| Ràng buộc | Nơi thực hiện |
|---|---|
| Mã ≤ 20 ký tự, tên ≤ 150 ký tự, không trống; mã không trùng | `sp_ThemThietBi` |
| Tổng số lượng khi thêm/sửa phải > 0 (schema cho phép ≥ 0, ứng dụng chặt hơn) | `sp_ThemThietBi`, `sp_CapNhatThietBi` |
| `0 <= SoLuongKhaDung <= TongSoLuong` | `sp_ThemThietBi` và CHECK của schema |
| Người quản lý phải là thành viên có thật | `sp_ThemThietBi`, `sp_GanNguoiQuanLy` |
| Không giảm tổng xuống dưới số đang mượn | `sp_CapNhatThietBi` |
| `SoLuongKhaDung` chỉ đổi do mượn/trả | Trigger của module 10; module này không cho sửa trực tiếp cột đó |

Số đang mượn = `TongSoLuong - SoLuongKhaDung`. Khi sửa tổng, khả dụng mới = tổng mới - số đang mượn, nên phiếu mượn và kho luôn khớp. Thủ tục không mở transaction riêng (mỗi lệnh ghi nguyên tử) để chạy được trong transaction của `tests.sql`. Tham số thủ tục khai báo rộng rồi kiểm tra độ dài, để báo lỗi rõ ràng thay vì bị MySQL cắt chuỗi.

## 3. Thủ tục, hàm, view và ví dụ

| Đối tượng SQL | Chức năng |
|---|---|
| `vw_DanhSachThietBi` | Danh sách thiết bị, số đang mượn, tên người quản lý (LEFT JOIN nên thiết bị chưa có người quản lý vẫn hiện) |
| `fn_KiemTraKhaDung(ma, soLuong)` | 1 nếu thiết bị tồn tại và còn đủ `soLuong` (> 0), ngược lại 0 |
| `sp_ThemThietBi(Ma, Ten, Tong, KhaDung, MSVQuanLy)` | Thêm thiết bị; `KhaDung` NULL = bằng tổng; `MSVQuanLy` NULL/trống = chưa có |
| `sp_CapNhatThietBi(Ma, Ten, Tong)` | Sửa tên và tổng; khả dụng điều chỉnh theo số đang mượn |
| `sp_GanNguoiQuanLy(Ma, MSV)` | Gán/đổi người quản lý; `MSV` NULL/trống = bỏ người quản lý |
| `sp_TimThietBi(TuKhoa)` | Tra cứu theo mã hoặc tên chứa từ khóa; trống = tất cả |

```sql
USE QuanLyCauLacBo;
-- Thay MSV_THUC_TE bằng mã thành viên có sẵn:
CALL sp_ThemThietBi('TB09_DEMO', 'Loa bluetooth', 3, NULL, NULL);
CALL sp_GanNguoiQuanLy('TB09_DEMO', 'MSV_THUC_TE');
CALL sp_CapNhatThietBi('TB09_DEMO', 'Loa bluetooth mini', 5);
SELECT fn_KiemTraKhaDung('TB09_DEMO', 2);     -- 1
SELECT fn_KiemTraKhaDung('TB09_DEMO', 99);    -- 0
CALL sp_TimThietBi('loa');
SELECT * FROM vw_DanhSachThietBi ORDER BY MaThietBi;
```

## 4. Java và seed

`CoSoVatChat` là record dữ liệu; `CoSoVatChatDao` chỉ truy cập JDBC (gọi view/thủ tục/hàm), `CoSoVatChatService` kiểm tra dữ liệu nhập, `CoSoVatChatUi` là menu (xem, tìm, thêm, sửa, gán người quản lý, kiểm tra khả dụng; nhập 0 để quay lại), `CoSoVatChatModule` nối vào menu chính.

Seed dùng mã riêng `TB09_01`…`TB09_08`, chạy lại không chèn trùng. Người quản lý tham chiếu MSV **thực sự có** trong ThanhVien; nếu seed nền còn trống thì để NULL (schema cho phép). `TB09_08` cố ý không có người quản lý. Khả dụng ban đầu bằng tổng; seed module 10 tự đồng bộ lại theo phiếu mượn.

## 5. Kết quả kiểm thử

`tests.sql` tự dựng dữ liệu `T_TRUONG_*` (không phụ thuộc seed nền) trong một transaction rồi rollback. Ca đúng: thêm có/không người quản lý, cắt khoảng trắng, view số đang mượn, gán – đổi – bỏ người quản lý, tăng và giảm tổng đúng bằng số đang mượn, hàm kiểm tra khả dụng, tra cứu. Ca sai: mã trùng/trống/quá 20 ký tự, tên trống, tổng 0, khả dụng lớn hơn tổng hoặc âm, người quản lý không tồn tại, giảm tổng dưới số đang mượn, vi phạm CHECK (mã lỗi 3819), FK chặn xóa người đang quản lý thiết bị (1451). Sau mỗi ca sai đều kiểm tra dữ liệu không bị đổi.

Kết quả chạy trên MySQL (điền sau khi chạy thật):

```text
(dán output của: mysql -u root -p < sql/modules/09_cosovatchat/tests.sql)
```

Unit test Java: `CoSoVatChatServiceTest` (4 ca, Dao giả) và các ca menu trong `MuonTraMenuTest` chạy bằng `mvn test`; ca tích hợp MySQL trong `MuonTraDatabaseTest` bật bằng `mvn "-Dclb.mysql.tests=true" test`.

## 6. Bổ sung mô hình quan hệ

Bảng CoSoVatChat cùng quan hệ "Quản lý" với ThanhVien cần được bổ sung vào mô hình quan hệ cùng PhieuMuon và ChiTietMuon (DECISIONS #10); xem [10_muontra.md](10_muontra.md) mục 6.
