# Báo cáo Module 07: Quản lý Thu – Chi (`07_thuchi`)

- **Người phụ trách:** Minh
- **Thư mục SQL:** `sql/modules/07_thuchi/{seed.sql, queries.sql, tests.sql}`
- **Thư mục Java:** `src/main/thuchi/{ThuChi.java, ThongKeThuChiSuKien.java, ThuChiDao.java, ThuChiService.java, ThuChiUi.java, ThuChiModule.java}`

---

## 1. Mô tả bảng và các cột

Module Quản lý Thu – Chi làm việc với 2 bảng là `ThuChi` (giao dịch tài chính) và `ThuChi_PhuongThucThanhToan` (thuộc tính đa trị lưu các phương thức thanh toán của một giao dịch).

### 1.1. Bảng `ThuChi`
Lưu thông tin các khoản thu và khoản chi của Câu lạc bộ (có thể gắn với một sự kiện cụ thể hoặc là khoản thu/chi chung của CLB).

| Tên cột | Kiểu dữ liệu | Cho phép NULL | Mô tả |
|---|---|---|---|
| `MaGD` | `VARCHAR(20)` | Không (`PRIMARY KEY`) | Mã định danh giao dịch (VD: `GD01`, `GD02`...) |
| `LoaiGD` | `VARCHAR(20)` | Không (`NOT NULL`) | Phân loại giao dịch: chỉ nhận `'Thu'` hoặc `'Chi'` |
| `MucDich` | `TEXT` | Có | Nội dung / mục đích thu hoặc chi |
| `SoTien` | `DECIMAL(15,2)` | Không (`NOT NULL`) | Số tiền giao dịch (VNĐ, bắt buộc `> 0`) |
| `ThoiGian` | `DATETIME` | Không (`NOT NULL`) | Ngày giờ thực hiện giao dịch |
| `MinhChung` | `TEXT` | Có | Tên file hoặc đường dẫn hóa đơn, chứng từ, biên lai |
| `MSV` | `VARCHAR(20)` | Có (`FK`) | Mã sinh viên thực hiện giao dịch (tham chiếu `ThanhVien.MSV`) |
| `MaSK` | `VARCHAR(20)` | Có (`FK`) | Mã sự kiện phát sinh giao dịch (tham chiếu `SuKien.MaSK`, `NULL` nếu là thu/chi chung CLB) |

### 1.2. Bảng `ThuChi_PhuongThucThanhToan`
Biểu diễn thuộc tính đa trị `PhuongThucThanhToan` (một giao dịch có thể thanh toán kết hợp nhiều phương thức như cả `Tiền mặt` và `Chuyển khoản ngân hàng`).

| Tên cột | Kiểu dữ liệu | Cho phép NULL | Mô tả |
|---|---|---|---|
| `MaGD` | `VARCHAR(20)` | Không (`PRIMARY KEY`, `FK`) | Mã giao dịch (tham chiếu `ThuChi.MaGD`) |
| `PhuongThucThanhToan` | `VARCHAR(100)` | Không (`PRIMARY KEY`) | Tên phương thức thanh toán (VD: `Tiền mặt`, `Chuyển khoản ngân hàng`, `Ví điện tử MoMo`...) |

---

## 2. Ràng buộc toàn vẹn (PK, FK, CHECK, Trigger, Transaction)

### 2.1. Khóa chính (Primary Key)
- `ThuChi`: `PRIMARY KEY (MaGD)`.
- `ThuChi_PhuongThucThanhToan`: `PRIMARY KEY (MaGD, PhuongThucThanhToan)` — ngăn việc nhập trùng cùng một phương thức thanh toán nhiều lần cho cùng một giao dịch.

### 2.2. Khóa ngoại (Foreign Key)
- `FK_ThuChi_ThanhVien`: `ThuChi(MSV) REFERENCES ThanhVien(MSV) ON UPDATE CASCADE ON DELETE RESTRICT`.
- `FK_ThuChi_SuKien`: `ThuChi(MaSK) REFERENCES SuKien(MaSK) ON UPDATE CASCADE ON DELETE RESTRICT` — bảo vệ dữ liệu tài chính, không cho phép xóa sự kiện khi đã có giao dịch thu/chi.
- `FK_PTTT_ThuChi`: `ThuChi_PhuongThucThanhToan(MaGD) REFERENCES ThuChi(MaGD) ON UPDATE CASCADE ON DELETE CASCADE` — khi xóa giao dịch thì các dòng phương thức thanh toán đi kèm tự động được xóa sạch.

### 2.3. Ràng buộc CHECK
- `CK_ThuChi_LoaiGD`: `CHECK (LoaiGD IN ('Thu', 'Chi'))`.
- `CK_ThuChi_SoTien`: `CHECK (SoTien > 0)`.

### 2.4. Trigger kiểm tra dữ liệu (`sql/modules/07_thuchi/queries.sql`)
- `trg_ThuChi_BeforeInsert` & `trg_ThuChi_BeforeUpdate`:
  - Kiểm tra `MaGD` không để trống.
  - Kiểm tra `LoaiGD IN ('Thu', 'Chi')`.
  - Kiểm tra `SoTien > 0`.

### 2.5. Quản lý Transaction trong Java (`ThuChiDao.java`)
- Khi **Thêm mới** (`them`) hoặc **Cập nhật** (`capNhat`) một giao dịch, `ThuChiDao` tắt auto-commit (`c.setAutoCommit(false)`), thực hiện ghi bảng `ThuChi` và bảng `ThuChi_PhuongThucThanhToan` trong cùng một giao tác rồi mới `c.commit()`. Nếu có bất kỳ lỗi nào xảy ra ở một trong hai bảng, toàn bộ thao tác được `c.rollback()` để đảm bảo tính nguyên tử (Atomicity).

---

## 3. Các View, Thủ tục và Chức năng chính

### 3.1. Views & Stored Procedures (`sql/modules/07_thuchi/queries.sql`)
1. **`vw_ChiTietGiaoDich`**: Kết nối `ThuChi`, `ThanhVien`, `SuKien` và gộp danh sách phương thức thanh toán bằng `GROUP_CONCAT(pt.PhuongThucThanhToan SEPARATOR ', ')`.
2. **`vw_ThuChiTheoSuKien`**: Thống kê theo từng sự kiện gồm `MaSK`, `TenSK`, `SoGiaoDich`, `TongThu`, `TongChi` và `ChenhLech = TongThu - TongChi`.
3. **`sp_ThemGiaoDich` & `sp_ThemPhuongThucThanhToan`**: Thêm giao dịch và phương thức thanh toán.
4. **`sp_CapNhatGiaoDich` & `sp_XoaGiaoDich`**: Cập nhật và xóa giao dịch.
5. **`sp_TraCuuGiaoDichTheoThanhVien(p_MSV)` & `sp_TraCuuGiaoDichTheoSuKien(p_MaSK)`**: Tra cứu lịch sử giao dịch theo thành viên thực hiện hoặc theo sự kiện.
6. **`sp_ThongKeThuChiTheoSuKien()`**: Trả về bảng tổng hợp thu – chi và chênh lệch của tất cả sự kiện.

### 3.2. Thiết kế tầng Java (`src/main/thuchi/`)
- **`ThuChi.java` & `ThongKeThuChiSuKien.java`**: Thực thể giao dịch (kèm `List<String> phuongThucThanhToan`) và thực thể báo cáo thống kê theo sự kiện.
- **`ThuChiDao.java`**: Truy cập DB bằng `PreparedStatement` + Transaction (`setAutoCommit(false)`, `commit()`, `rollback()`).
- **`ThuChiService.java`**: Chuẩn hóa và kiểm tra hợp lệ (`LoaiGD` là `'Thu'`/`'Chi'`, `SoTien > 0`, có ít nhất 1 phương thức thanh toán, kiểm tra tồn tại `MSV` và `MaSK`).
- **`ThuChiUi.java`**: Menu console 9 chức năng (xem danh sách, thêm, sửa, xóa, tra cứu theo MSV, tra cứu theo MaSK, lọc theo loại Thu/Chi, thống kê thu – chi theo từng sự kiện, xem chi tiết giao dịch).

---

## 4. Kết quả kiểm thử (`tests.sql` & Console)

### 4.1. Kết quả chạy các ca thử đúng (`sql/modules/07_thuchi/tests.sql`)
**Trích xuất bảng danh sách giao dịch Thu – Chi trên Console:**
```text
===== MODULE 07: QUẢN LÝ THU – CHI =====
Mã GD    | Loại  |   Số tiền (VNĐ) | Thời gian        | Mã SK    | MSV          | Phương thức TT           | Mục đích                    
------------------------------------------------------------------------------------------------------------------------------------
GD05     | Chi   |       8,500,000 | 2026-10-12 18:00 | SK03     | (Trống)      | Chuyển khoản ngân hàn... | Trao giải thưởng và thuê ...
GD04     | Thu   |      12,000,000 | 2026-10-05 10:15 | SK03     | (Trống)      | Chuyển khoản ngân hàng   | Tài trợ doanh nghiệp cho ...
GD03     | Chi   |         650,000 | 2026-09-19 16:00 | SK02     | (Trống)      | Ví điện tử MoMo          | Mua tài liệu và trà bánh ...
GD06     | Thu   |       3,000,000 | 2026-09-10 08:30 | (Chung)  | (Trống)      | Chuyển khoản ngân hàn... | Thu quỹ định kỳ học kỳ I ...
GD02     | Chi   |       1,850,000 | 2026-09-04 15:30 | SK01     | (Trống)      | Chuyển khoản ngân hàn... | In ấn backdrop, standee v...
GD01     | Thu   |       5,000,000 | 2026-09-01 09:00 | SK01     | (Trống)      | Chuyển khoản ngân hàng   | Thu quỹ tài trợ Ngày hội ...
Tổng số: 6 giao dịch.
```

**Trích xuất bảng Thống kê Thu – Chi theo từng sự kiện (Chức năng 8):**
```text
--- THỐNG KÊ THU – CHI THEO TỪNG SỰ KIỆN ---
Mã SK      | Tên sự kiện                            |    Số GD |   Tổng Thu (VNĐ) |   Tổng Chi (VNĐ) | Chênh lệch (VNĐ)
----------------------------------------------------------------------------------------------------------------------
SK01       | Ngày hội Chào tân sinh viên CLB IT     |        2 |        5,000,000 |        1,850,000 |        3,150,000
SK02       | Workshop Git & GitHub thực chiến       |        1 |                0 |          650,000 |         -650,000
SK03       | Cuộc thi Hackathon Mùa Thu 2026        |        2 |       12,000,000 |        8,500,000 |        3,500,000
SK04       | Seminar Định hướng nghề nghiệp AI &... |        0 |                0 |                0 |                0
SK05       | Đêm Gala Tổng kết hoạt động cuối năm   |        0 |                0 |                0 |                0
----------------------------------------------------------------------------------------------------------------------
TỔNG       | Tất cả sự kiện                         |        5 |       17,000,000 |       11,000,000 |        6,000,000
```

### 4.2. Kết quả kiểm thử các ca thử sai (Vi phạm ràng buộc)
| Ca thử sai | Câu lệnh / Thao tác | Kết quả thực tế |
|---|---|---|
| 1. Loại giao dịch khác `Thu` hoặc `Chi` | `CALL sp_ThemGiaoDich('GD_ERR01', 'VayMuon', 'Giao dịch sai loại', 500000, NOW(), NULL, NULL, 'SK01');` | **Bị chặn** (`ERROR 1644 (45000): Loại giao dịch chỉ được phép là Thu hoặc Chi`) |
| 2. Số tiền bằng `0` hoặc âm | `CALL sp_ThemGiaoDich('GD_ERR02', 'Chi', 'Số tiền âm', -100000, NOW(), NULL, NULL, 'SK01');` | **Bị chặn** (`ERROR 1644 (45000): Số tiền giao dịch phải lớn hơn 0` / trên UI: `Số tiền phải lớn hơn 0.`) |
| 3. Thêm trùng phương thức thanh toán cho cùng 1 `MaGD` | `CALL sp_ThemPhuongThucThanhToan('GD01', 'Chuyển khoản ngân hàng');` | **Bị chặn** (`ERROR 1062 (23000): Duplicate entry 'GD01-Chuyển khoản ngân hàng' for key 'PRIMARY'`) |
| 4. Xóa sự kiện đang còn giao dịch thu – chi | `DELETE FROM SuKien WHERE MaSK = 'SK01';` | **Bị chặn** bởi khóa ngoại `FK_ThuChi_SuKien (ON DELETE RESTRICT)` |

