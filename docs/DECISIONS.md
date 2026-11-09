# Các điểm lệch giữa tài liệu – và cách đã xử lý

Bản mô tả (Phần I–III) và hai hình ERD (`docs/erd/`) có vài chỗ không khớp nhau.
`sql/00_schema/schema.sql` đã được chỉnh theo **hình ERD và mô hình quan hệ** (nguồn chuẩn).
Cả nhóm xác nhận các dòng "☐" rồi tick lại; thấy sai thì báo Tú.

| # | Vấn đề | Cách xử lý trong schema | Xác nhận |
|---|---|---|---|
| 1 | `ThamGiaKhoaHoc`: Phần I ghi `NgayDangKy`, `KetQuaXepLoai`; ERD + mô hình quan hệ ghi `VaiTro`, `KetQuaDanhGia` | Theo ERD: `VaiTro`, `KetQuaDanhGia` | ☐ |
| 2 | `DuAn.TienDoChung`: ERD vẽ nét đứt (thuộc tính suy diễn) nhưng mô hình quan hệ vẫn có cột | Giữ cột `TienDoChung`, **trigger tự tính trung bình `TienDoRieng`** (Chiến, module 05) | ☐ |
| 3 | `CoSoVatChat.TinhTrang`: có trong Phần I, **không có** trong ERD | Bỏ cột (theo ERD) | ☐ |
| 4 | Người quản lý thiết bị: Phần I (chức năng "Phân công thành viên quản lý thiết bị") và Phần II ("Cơ sở vật chất được một thành viên chịu trách nhiệm quản lý") đặt người quản lý ở **thiết bị**; riêng hình ERD và Phần II mục 11 vẽ "Quản lý" nối vào khung kết tập Phiếu mượn – Mượn – Cơ sở vật chất | Giữ `MSVQuanLy` ở `CoSoVatChat` (cho phép NULL); `ChiTietMuon` không có cột này. Lý do: hai nguồn văn bản cùng hướng, và module 09, 10 đã hoàn thành theo hướng này. Nếu cả nhóm muốn theo đúng khung kết tập thì phải thêm `ChiTietMuon.MSVQuanLy` và Trường sửa lại module 09, 10 | ☐ |
| 5 | `ThuChi.MaSK`: "Phát sinh" 1 sự kiện – N giao dịch, nét đơn (không bắt buộc) | `MaSK` cho phép NULL | ☐ |
| 6 | `PhuongThucThanhToan` | Đa trị (oval đôi trong ERD) → bảng `ThuChi_PhuongThucThanhToan` | ☐ |
| 7 | `PhieuMuon.TrangThaiDuyet` | Có trong ERD → giữ (`ChoDuyet`/`DaDuyet`/`TuChoi`) | ☐ |
| 8 | Kỹ thuật: thiếu `;`, CHECK cần MySQL ≥ 8.0.16 | Đã sửa; cả nhóm dùng MySQL 8.x | ☐ |
| 9 | `Email`, `SDT`: ERD vẽ là thuộc tính **đơn trị** (nhóm đã chuyển từ đa trị sang đơn trị); nhưng hình mô hình quan hệ (trang 3) còn vẽ 2 bảng `TV-SĐT`, `TV-Email` | Schema giữ 2 cột `Email`, `SDT` ngay trong `ThanhVien`, **không** có bảng riêng. **Tú vẽ lại** hình mô hình quan hệ: bỏ 2 bảng đó, thêm `Email`, `SĐT` vào bảng Thành viên | ☑ Đã có hình đầy đủ `docs/erd/Mo_hinh_quan_he_day_du.png` (không còn hai bảng đó) |
| 10 | Mô hình quan hệ (trang 3 trong draw.io) còn thiếu 6 bảng: Quyền hạn, TV–Quyền hạn, Đánh giá, Phiếu mượn, Chi tiết mượn, Cơ sở vật chất | Đã sinh hình đầy đủ 16 bảng trực tiếp từ `schema.sql` (`docs/erd/Mo_hinh_quan_he_day_du.png` và `.svg`); Đông đã vẽ riêng phần nhân sự. Nếu báo cáo cần đúng nét vẽ của nhóm thì chép lại hình này vào trang 3 của draw.io | ☑ |
| 11 | `ThuChi.MSV` và `ThuChi.MaSK`: Phần II ghi "mỗi giao dịch gắn một sự kiện" và "do một thành viên thực hiện", nhưng Phần I cho phép khoản thu chi ngoài sự kiện, ERD vẽ nét đơn và SQL gốc để NULL | Giữ như SQL gốc: cả hai cột cho phép NULL. Nếu nhóm muốn bắt buộc thì đổi `MSV` (hoặc `MaSK`) thành `NOT NULL` | ☐ |

## Ràng buộc phải làm bằng trigger/thủ tục (không viết được bằng CHECK)
- `SoLuongMuon <= SoLuongKhaDung` khi mượn (module 10 – Trường)
- Cập nhật `SoLuongKhaDung` khi mượn/trả (module 10 – Trường)
- `NgayTraThucTe >= NgayMuon` (so sánh qua bảng `PhieuMuon`) (module 10 – Trường)
- `DuAn.TienDoChung` = trung bình `ThamGiaDuAn.TienDoRieng` (module 05 – Chiến)
