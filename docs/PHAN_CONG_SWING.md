# Phân công giai đoạn Swing (Giai đoạn 1)

Giai đoạn 0 (Tú) đã xong: cửa sổ chính 10 tab, màn hình mẫu Thành viên, bảng đăng ký module, hướng dẫn. Đọc [SWING_HUONG_DAN.md](SWING_HUONG_DAN.md) trước khi bắt đầu. Mỗi người **chỉ sửa trong package module của mình**; cần đổi `common/`, `app/`, `config/`, `pom.xml` hoặc schema thì tạo Issue cho Tú.

## 1. Bảng tổng quan

| Người | Module | Lớp cần viết | Lớp cần sửa 1 dòng | Độ khó |
|---|---|---|---|---|
| **Tú** | 01 Thành viên | Xong (`ThanhVienSwingUi`) | Xong | Xong |
| **Đông** | 02 Ban chuyên môn | `BanChuyenMonSwingUi` | `BanChuyenMonSwingModule` | Vừa |
| | 03 Quyền hạn | `QuyenHanSwingUi` | `QuyenHanSwingModule` | Vừa |
| | 08 Đánh giá | `DanhGiaSwingUi` | `DanhGiaSwingModule` | Dễ |
| **Chiến** | 04 Khóa học, 05 Dự án | Xong | Xong | Xong |
| | Review, đóng gói, ảnh | Xem mục 4 | | Vừa |
| **Minh** | 06 Sự kiện | `SuKienSwingUi` | `SuKienSwingModule` | Vừa |
| | 07 Thu – Chi | `ThuChiSwingUi` | `ThuChiSwingModule` | Khó vừa |
| **Trường** | 09 Cơ sở vật chất | `CoSoVatChatSwingUi` | `CoSoVatChatSwingModule` | Dễ |
| | 10 Mượn – Trả | `MuonTraSwingUi` | `MuonTraSwingModule` | Khó |

"Lớp cần sửa 1 dòng": trong `createPanel()` thay `new PlaceholderPanel(...)` bằng `new XxxSwingUi(new XxxService())` (Trường: `PhieuMuonService`).

## 2. Quy trình cho mỗi màn hình

1. `git checkout develop && git pull`, tạo nhánh `feature/<module>-swing`.
2. Làm theo mục 4 của hướng dẫn Swing; commit nhỏ, đúng chuẩn `feat(<module>): ...`.
3. Viết test tự động (ít nhất 2) và **kiểm tay trên MySQL thật**.
4. Mở Pull Request vào `develop`, tick checklist ở mục 5, đính kèm ảnh chụp màn hình.
5. Một người review (xem mục 6), Tú merge. Không tự merge PR của mình.

## 3. Việc chi tiết từng người

### Đông

**02 Ban chuyên môn & Chức vụ** (`src/main/banchuyenmon/`). Bảng chính + bảng chi tiết như màn hình Thành viên.

| Màn hình cần có | Service |
|---|---|
| Bảng ban: mã, tên, số thành viên | `danhSach()` → `BanThongKe` |
| Chọn một ban thì bảng dưới hiện thành viên: MSV, họ tên, chức vụ, trạng thái | `thanhVienTheoBan(maBan)` → `ThanhVienTrongBan` |
| Thêm, sửa tên, xóa ban | `them`, `sua`, `xoa` (với `BanChuyenMon(maBan, tenBan)`) |
| Chuyển một thành viên sang ban khác (chọn thành viên ở bảng dưới, chọn ban đích trong danh sách thả xuống) | `chuyenBan(msv, maBan)` |
| Đổi chức vụ của thành viên đang chọn | `doiChucVu(msv, chucVu)` |

**03 Quyền hạn** (`src/main/quyenhan/`).

| Màn hình cần có | Service |
|---|---|
| Bảng quyền: mã, tên, mô tả | `danhSach()` → `QuyenHan` |
| Chọn một quyền thì bảng dưới hiện thành viên đang có quyền | `thanhVienCoQuyen(maQuyen)` → `ThanhVienCoQuyen` |
| Thêm, sửa, xóa quyền | `them`, `sua`, `xoa` |
| Cấp quyền cho một thành viên (nhập MSV), thu hồi quyền | `capQuyen(msv, ma)`, `thuHoiQuyen(msv, ma)` |
| (Tùy chọn) hộp thoại "quyền của một thành viên" | `quyenCuaThanhVien(msv)` |

**08 Đánh giá** (`src/main/danhgia/`). Service chưa có hàm "liệt kê tất cả" và chưa có xóa nên màn hình tra theo thành viên.

| Màn hình cần có | Service |
|---|---|
| Ô nhập MSV + nút *Xem lịch sử* → bảng đánh giá của thành viên đó | `lichSu(msv)` → `DanhGia` |
| Thêm, sửa đánh giá (mã ghi nhận, loại, hình thức, lý do, ngày quyết định, MSV) | `them`, `sua` |
| Bảng thống kê: loại đánh giá, số đánh giá, số thành viên | `thongKe()` → `ThongKeDanhGia` |

### Minh

**06 Sự kiện** (`src/main/sukien/`). Bảng chính + bảng chi tiết.

| Màn hình cần có | Service |
|---|---|
| Bảng sự kiện: mã, tên, ngày bắt đầu, ngày kết thúc | `layTatCaSuKien()` |
| Tìm theo tên; lọc theo khoảng ngày (hai ô ngày, trống = không giới hạn) | `timSuKienTheoTen`, `traCuuTheoThoiGian(tu, den)` |
| Thêm, sửa, xóa sự kiện | `themSuKien`, `capNhatSuKien`, `xoaSuKien` |
| Chọn sự kiện thì bảng dưới hiện người tham gia: MSV, họ tên, ban, vai trò | `xemThanhVienCuaSuKien(maSK)` → `ThamGiaSuKien` |
| Thêm thành viên vào sự kiện, sửa vai trò, xóa khỏi sự kiện | `themThanhVienThamGia(msv, maSK, vaiTro)`, `capNhatVaiTroThamGia`, `xoaThanhVienKhoiSuKien` |

**07 Thu – Chi** (`src/main/thuchi/`). Gợi ý hai tab con: *Giao dịch* và *Thống kê theo sự kiện*.

| Màn hình cần có | Service |
|---|---|
| Bảng giao dịch: mã, loại, mục đích, số tiền, thời gian, thành viên, sự kiện, phương thức thanh toán | `layTatCaGiaoDich()` → `ThuChi` |
| Lọc theo loại (Thu/Chi), theo thành viên, theo sự kiện | `locTheoLoai`, `traCuuTheoThanhVien`, `traCuuTheoSuKien` |
| Thêm, sửa, xóa giao dịch | `themGiaoDich`, `capNhatGiaoDich`, `xoaGiaoDich` |
| Bảng thống kê: số giao dịch, tổng thu, tổng chi, chênh lệch theo sự kiện | `thongKeTheoTungSuKien()` |

Lưu ý: `ThuChi` có thời gian dạng `LocalDateTime` và số tiền `BigDecimal` nên tự viết hàm đọc `yyyy-MM-dd HH:mm` và số tiền trong package của Minh (không sửa `common/`). `phuongThucThanhToan` là danh sách: dùng ô nhập ngăn cách bằng dấu phẩy hoặc các ô chọn.

### Trường

**Làm trước:** chạy `sql/modules/09_cosovatchat/tests.sql` và `10_muontra/tests.sql` trên MySQL thật, ghi kết quả vào báo cáo module và bảng ở `docs/bao-cao/TONG_HOP.md`. Không làm giao diện trên SQL chưa kiểm chứng.

**09 Cơ sở vật chất** (`src/main/cosovatchat/`).

| Màn hình cần có | Service |
|---|---|
| Bảng thiết bị: mã, tên, tổng số lượng, khả dụng, người quản lý | `danhSach()`, tìm bằng `timKiem(tuKhoa)` |
| Thêm thiết bị (khả dụng trống = bằng tổng; người quản lý trống = chưa có) | `themMoi(ma, ten, tong, khaDung, msvQuanLy)` |
| Sửa tên, tổng số lượng | `capNhat(ma, ten, tong)` |
| Gán, đổi người quản lý | `ganNguoiQuanLy(ma, msv)` |
| Nút nhỏ kiểm tra "còn đủ số lượng X không" | `kiemTraKhaDung(ma, soLuong)` |

**10 Mượn – Trả** (`src/main/muontra/`). Bảng chính + bảng chi tiết, phức tạp nhất.

| Màn hình cần có | Service |
|---|---|
| Bảng phiếu mượn, có danh sách thả xuống lọc trạng thái (Tất cả, `ChoDuyet`, `DaDuyet`, `TuChoi`) | `danhSachPhieu(trangThai)` (null hoặc trống = tất cả) |
| Chọn phiếu thì bảng dưới hiện các thiết bị trong phiếu | `chiTiet(maPhieu)` → `ChiTietMuon` |
| Tạo phiếu (mã, ngày mượn, ngày hẹn trả, mục đích, MSV người mượn) | `taoPhieu(...)` |
| Thêm thiết bị vào phiếu đang chọn | `themThietBi(maPhieu, maThietBi, soLuong)` |
| Duyệt hoặc từ chối phiếu | `duyet(maPhieu)`, `tuChoi(maPhieu)` |
| Trả một thiết bị, trả tất cả (ngày trả mặc định hôm nay) | `traThietBi(maPhieu, maThietBi, ngayTra)`, `traTatCa(maPhieu, ngayTra)` |
| Hộp thoại hoặc tab con *Theo dõi*: thiết bị chưa trả, thiết bị trả muộn | `thietBiChuaTra()`, `thietBiTraMuon()` → `DongTheoDoi` |
| (Tùy chọn) lịch sử mượn của một thành viên | `lichSuMuon(msv)` |

### Chiến

Hai module 04, 05 đã có giao diện và đã cài `Refreshable`. Việc của Chiến ở giai đoạn này:

1. **Review chéo** các PR của Đông và Minh (đúng khuôn, chạy qua `Tasks`, không gọi `Dao`, có test, có ảnh).
2. **Đóng gói chạy được bằng `java -jar`:** mở PR đề xuất thêm plugin đóng gói (shade hoặc assembly) vào `pom.xml` và hướng dẫn chạy; Tú merge.
3. **Kịch bản demo và ảnh màn hình:** soạn kịch bản 5 đến 7 phút đi qua các tab, chụp ảnh từng tab sau khi các module khác xong, đặt vào `docs/bao-cao/anh/`.
4. **(Tùy chọn, nếu còn thời gian)** màn hình *Tổng quan*: số thành viên theo trạng thái, sự kiện sắp tới, thiết bị đang cho mượn, tổng thu – chi. Làm xong báo Tú để thêm tab.

### Tú

Review và merge, xử lý conflict, chạy toàn bộ trên MySQL thật, cập nhật README và báo cáo tổng hợp, và các Issue cần sửa `common/`, `app/`, `pom.xml`. Review thêm các PR của Trường (module 10).

## 4. Thứ tự làm gợi ý

1. Tất cả: đọc hướng dẫn, chạy `mvn compile exec:java` để thấy cửa sổ và màn hình Thành viên, đổi kết nối MySQL.
2. Trường: chạy test SQL thật của 09 và 10 trước. Các bạn khác bắt đầu màn hình **dễ nhất** của mình (Đông: 08; Minh: 06; Trường: 09).
3. Màn hình thứ hai, thứ ba. Chiến review PR đầu tiên của từng người.
4. Cả nhóm: chụp ảnh, sửa lỗi, tích hợp với Tú.

## 5. Định nghĩa "xong" cho một màn hình (tick trong PR)

- [ ] Có danh sách và tìm kiếm (hoặc lọc), nạp được dữ liệu khi mở tab và khi bấm *Tải lại*
- [ ] Thêm hợp lệ thành công; thêm sai (trống, trùng, sai định dạng) báo lỗi tiếng Việt, không mất dữ liệu đã nhập
- [ ] Sửa, xóa có hộp xác nhận; trường hợp xóa bị chặn có thông báo dễ hiểu
- [ ] Mọi truy vấn MySQL chạy trong `tasks.run`; cửa sổ không đơ
- [ ] Panel cài `Refreshable`; đổi kết nối MySQL thì bảng nạp lại
- [ ] Không gọi `Dao` trực tiếp; không sửa file ngoài package module
- [ ] Có ít nhất 2 test tự động (chạy được bằng `mvn test` không cần MySQL)
- [ ] Đã kiểm tay trên MySQL thật; có ảnh chụp tab chính và một hộp thoại
- [ ] Đã cập nhật báo cáo module (`docs/bao-cao/NN_*.md`) mục "Giao diện Swing"

## 6. Cặp review

| PR của | Review bởi |
|---|---|
| Đông | Chiến |
| Minh | Chiến |
| Trường | Tú, thêm Chiến nếu có thể |
| Chiến | Đông |
| Tú | Minh |

## 7. Mốc thời gian (điền khi chốt deadline)

| Mốc | Nội dung | Hạn |
|---|---|---|
| M1 | Mỗi người xong **một** màn hình đầu và đã được review; Trường có kết quả test SQL thật của 09, 10 | ____ |
| M2 | Xong toàn bộ màn hình, test và ảnh | ____ |
| M3 | Tích hợp, kiểm tra cả nhóm trên MySQL thật, đóng gói jar, báo cáo | ____ |
| Nộp | | ____ |

## 8. Rủi ro

- **SQL chưa kiểm chứng trên MySQL thật** (module 06, 07, 09, 10): lỗi SQL sẽ lộ ra khi làm giao diện. Người phụ trách chạy `tests.sql` thật trước và báo ngay.
- **Máy không có màn hình** (kể cả máy chủ chạy test) không mở được cửa sổ: test chỉ dựng `JPanel`, việc xem thực tế phải làm trên máy có màn hình.
- **Sửa file dùng chung** (`SwingSupport`, `ClubWorkspace`) làm mọi người xung đột: tạo Issue cho Tú thay vì tự sửa.
