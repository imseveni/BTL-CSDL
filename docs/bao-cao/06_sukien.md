# Báo cáo Module 06: Quản lý Sự kiện (`06_sukien`)

- **Người phụ trách:** Minh
- **Thư mục SQL:** `sql/modules/06_sukien/{seed.sql, queries.sql, tests.sql}`
- **Thư mục Java:** `src/main/sukien/{SuKien.java, ThamGiaSuKien.java, SuKienDao.java, SuKienService.java, SuKienUi.java, SuKienModule.java}`

---

## 1. Mô tả bảng và các cột

Module Quản lý Sự kiện làm việc trực tiếp với 2 bảng chính là `SuKien` và `ThamGiaSuKien` (quan hệ nhiều – nhiều giữa `ThanhVien` và `SuKien`).

### 1.1. Bảng `SuKien`
Lưu trữ thông tin về các sự kiện do Câu lạc bộ tổ chức.

| Tên cột | Kiểu dữ liệu | Cho phép NULL | Mô tả |
|---|---|---|---|
| `MaSK` | `VARCHAR(20)` | Không (`PRIMARY KEY`) | Mã định danh duy nhất của sự kiện (VD: `SK01`, `SK02`...) |
| `TenSK` | `VARCHAR(150)` | Không (`NOT NULL`) | Tên sự kiện |
| `NgayBatDau` | `DATE` | Không (`NOT NULL`) | Ngày bắt đầu diễn ra sự kiện |
| `NgayKetThuc` | `DATE` | Có (`NULL`) | Ngày kết thúc sự kiện (có thể để `NULL` nếu chưa xác định) |

### 1.2. Bảng `ThamGiaSuKien`
Lưu trữ danh sách thành viên tham gia từng sự kiện kèm vai trò cụ thể của thành viên trong sự kiện đó.

| Tên cột | Kiểu dữ liệu | Cho phép NULL | Mô tả |
|---|---|---|---|
| `MSV` | `VARCHAR(20)` | Không (`PRIMARY KEY`, `FK`) | Mã sinh viên tham gia (tham chiếu `ThanhVien.MSV`) |
| `MaSK` | `VARCHAR(20)` | Không (`PRIMARY KEY`, `FK`) | Mã sự kiện (tham chiếu `SuKien.MaSK`) |
| `VaiTro` | `VARCHAR(100)` | Có | Vai trò trong sự kiện (VD: `Trưởng ban tổ chức`, `Diễn giả`, `Hậu cần`, `Thí sinh`...) |

---

## 2. Ràng buộc toàn vẹn (PK, FK, CHECK, Trigger)

### 2.1. Khóa chính (Primary Key)
- `SuKien`: `PRIMARY KEY (MaSK)` — đảm bảo mã sự kiện không trùng lặp.
- `ThamGiaSuKien`: `PRIMARY KEY (MSV, MaSK)` — mỗi thành viên chỉ được ghi nhận một dòng tham gia trong cùng một sự kiện.

### 2.2. Khóa ngoại (Foreign Key)
- `FK_TGSK_ThanhVien`: `ThamGiaSuKien(MSV) REFERENCES ThanhVien(MSV) ON UPDATE CASCADE ON DELETE CASCADE`.
- `FK_TGSK_SuKien`: `ThamGiaSuKien(MaSK) REFERENCES SuKien(MaSK) ON UPDATE CASCADE ON DELETE CASCADE`.
- Liên kết từ bảng `ThuChi`: `FK_ThuChi_SuKien` (`ThuChi(MaSK) REFERENCES SuKien(MaSK) ON UPDATE CASCADE ON DELETE RESTRICT`) — không cho phép xóa sự kiện nếu sự kiện đó đã phát sinh giao dịch thu – chi.

### 2.3. Ràng buộc CHECK
- `CK_SuKien_ThoiGian`: `CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau)` — đảm bảo ngày kết thúc không nhỏ hơn ngày bắt đầu.

### 2.4. Trigger kiểm tra dữ liệu (`sql/modules/06_sukien/queries.sql`)
- `trg_SuKien_BeforeInsert` & `trg_SuKien_BeforeUpdate`:
  - Chặn thêm/sửa nếu `MaSK` hoặc `TenSK` chỉ chứa khoảng trắng rỗng (`SIGNAL SQLSTATE '45000'`).
  - Chặn nếu `NgayKetThuc < NgayBatDau` với thông báo lỗi tiếng Việt rõ ràng: `'Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu'`.

---

## 3. Các View, Thủ tục và Chức năng chính

### 3.1. View & Stored Procedures (`sql/modules/06_sukien/queries.sql`)
1. **`vw_DanhSachSuKien`**: Tổng hợp danh sách sự kiện kèm số lượng thành viên tham gia (`COUNT(tg.MSV)`).
2. **`sp_ThemSuKien(p_MaSK, p_TenSK, p_NgayBatDau, p_NgayKetThuc)`**: Kiểm tra trùng mã và thêm mới sự kiện.
3. **`sp_CapNhatSuKien(p_MaSK, p_TenSK, p_NgayBatDau, p_NgayKetThuc)`**: Cập nhật thông tin sự kiện theo mã.
4. **`sp_XoaSuKien(p_MaSK)`**: Kiểm tra sự kiện tồn tại và kiểm tra bảng `ThuChi`; nếu đã có giao dịch thu – chi liên kết thì báo lỗi và chặn xóa.
5. **`sp_TraCuuSuKienTheoThoiGian(p_TuNgay, p_DenNgay)`**: Tra cứu các sự kiện diễn ra giao với khoảng thời gian `[p_TuNgay, p_DenNgay]`.
6. **`sp_ThemThanhVienSuKien(p_MSV, p_MaSK, p_VaiTro)`**: Kiểm tra tồn tại `MSV`, `MaSK`, kiểm tra trùng tham gia và ghi nhận vai trò.
7. **`sp_XemThanhVienTheoSuKien(p_MaSK)`**: Xem danh sách thành viên tham gia một sự kiện kèm `HoVaTen`, `ChucVu`, `MaBan`, `VaiTro`.
8. **`sp_XemLichSuSuKienThanhVien(p_MSV)`**: Xem lịch sử các sự kiện mà một thành viên đã tham gia.

### 3.2. Thiết kế tầng Java (`src/main/sukien/`)
- **`SuKien.java` & `ThamGiaSuKien.java`**: Lớp thực thể ánh xạ dữ liệu bảng.
- **`SuKienDao.java`**: Sử dụng `PreparedStatement` và `try-with-resources` cho toàn bộ thao tác CRUD sự kiện, tra cứu theo khoảng thời gian/tên/mã, quản lý thành viên tham gia và tra cứu lịch sử. Bọc `SQLException` bằng `DataAccessException`.
- **`SuKienService.java`**: Kiểm tra nghiệp vụ (độ dài mã/tên, `NgayKetThuc >= NgayBatDau`, kiểm tra tồn tại thành viên/sự kiện, chặn xóa sự kiện đang có giao dịch thu – chi).
- **`SuKienUi.java`**: Giao diện menu console với 9 chức năng.

---

## 4. Kết quả kiểm thử (`tests.sql` & Console)

### 4.1. Kết quả chạy các ca thử đúng (`sql/modules/06_sukien/tests.sql`)
```sql
CALL sp_ThemSuKien('SK_TEST01', 'Workshop Kỹ năng mềm', '2026-11-15', '2026-11-16');
CALL sp_CapNhatSuKien('SK_TEST01', 'Workshop Kỹ năng giao tiếp & thuyết trình', '2026-11-15', '2026-11-17');
SELECT * FROM vw_DanhSachSuKien;
CALL sp_XoaSuKien('SK_TEST01');
```

**Trích xuất kết quả danh sách sự kiện trên Console:**
```text
===== MODULE 06: QUẢN LÝ SỰ KIỆN =====
Mã SK      | Tên sự kiện                                   | Ngày bắt đầu | Ngày kết thúc
----------------------------------------------------------------------------------------
SK05       | Đêm Gala Tổng kết hoạt động cuối năm          | 2026-12-20   | Chưa xác định
SK04       | Seminar Định hướng nghề nghiệp AI & Data      | 2026-11-01   | 2026-11-01  
SK03       | Cuộc thi Hackathon Mùa Thu 2026               | 2026-10-10   | 2026-10-12  
SK02       | Workshop Git & GitHub thực chiến              | 2026-09-20   | 2026-09-20  
SK01       | Ngày hội Chào tân sinh viên CLB IT            | 2026-09-05   | 2026-09-06  
Tổng cộng: 5 sự kiện.
```

### 4.2. Kết quả kiểm thử các ca thử sai (Vi phạm ràng buộc)
| Ca thử sai | Câu lệnh / Thao tác | Kết quả thực tế |
|---|---|---|
| 1. Ngày kết thúc nhỏ hơn ngày bắt đầu | `CALL sp_ThemSuKien('SK_ERR01', 'Sự kiện lỗi ngày', '2026-10-20', '2026-10-10');` | **Bị chặn** (`ERROR 1644 (45000): Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu` / trên UI: `[Cảnh báo nghiệp vụ] Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.`) |
| 2. Trùng mã sự kiện đã có | `CALL sp_ThemSuKien('SK01', 'Sự kiện trùng mã', '2026-10-01', '2026-10-02');` | **Bị chặn** (`ERROR 1644 (45000): Mã sự kiện đã tồn tại` / trên UI: `[Cảnh báo nghiệp vụ] Mã sự kiện 'SK01' đã tồn tại.`) |
| 3. Thêm MSV không tồn tại vào sự kiện | `CALL sp_ThemThanhVienSuKien('MSV_KHONG_TON_TAI', 'SK01', 'Tình nguyện viên');` | **Bị chặn** (`ERROR 1644 (45000): Mã sinh viên không tồn tại trong hệ thống`) |
| 4. Xóa sự kiện đang có giao dịch Thu – Chi | `CALL sp_XoaSuKien('SK01');` | **Bị chặn** (`ERROR 1644 (45000): Không thể xóa sự kiện đã có giao dịch thu chi liên kết` / trên UI: `[Cảnh báo nghiệp vụ] Không thể xóa sự kiện 'SK01' vì đang có giao dịch thu – chi liên kết với sự kiện này.`) |

