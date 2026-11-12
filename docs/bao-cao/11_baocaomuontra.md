# Module 11 – Báo cáo mượn – trả cơ sở vật chất

Người phụ trách: **Minh**. Đã khớp với module 09 (Cơ sở vật chất) và 10 (Mượn – Trả) trong bản tích hợp; thay cho tab báo cáo mẫu cũ của Trường.

## 1. Báo cáo dùng để làm gì

| | |
|---|---|
| Ai lập | Thành viên Ban Chủ nhiệm đang đăng nhập (tự điền vào báo cáo; có thể chọn người khác trong danh sách) |
| Gửi cho ai | Chủ nhiệm CLB và giảng viên phụ trách / Đoàn trường (đơn vị quản lý CLB) |
| Khi nào | Cuối tháng hoặc cuối học kỳ, để kiểm kê thiết bị chưa trả, trả quá hạn |
| Xem | Tab "Báo cáo Mượn – Trả": sáu chỉ số tổng quan + bảng chi tiết có tô màu trạng thái |
| Xuất file | Nút **Xuất PDF** (khổ A4 nằm ngang, dùng chung mẫu với báo cáo Thành viên: đầu trang, thẻ thống kê, bảng, tổng kết, chữ ký, phân trang) |

Chỉ Ban Chủ nhiệm xem được báo cáo (Service chặn vai trò Thành viên).

## 2. Dữ liệu và quy tắc

Báo cáo đọc `PhieuMuon`, `ChiTietMuon`, `CoSoVatChat`, `ThanhVien`; chỉ tính phiếu `DaDuyet`. Nhãn trạng thái: **Đã trả** (trả đúng hạn), **Trả quá hạn**, **Chưa trả** (còn trong hạn), **Quá hạn** (chưa trả và đã quá ngày hẹn). Số ngày quá hạn tính đến ngày làm báo cáo. Số lượng khả dụng của thiết bị (module 09) luôn bằng tổng trừ số đang mượn; điều này được kiểm tra trong `modules/11_baocaomuontra/tests.sql`.

## 3. SQL (mục 7)

Hai thủ tục trong `sql/modules/11_baocaomuontra/queries.sql`, nạp bởi `run_all.sql`; ứng dụng gọi `sp_BaoCaoMuonTra` (không còn câu SQL nằm riêng trong Java):

```sql
-- chi tiết: p_Loai = 'NGAY_MUON' hoặc 'NGAY_TRA'; p_NgayMoc NULL = hôm nay
CALL sp_BaoCaoMuonTra('2025-01-01', '2026-12-31', 'NGAY_MUON', NULL);
-- sáu chỉ số tổng quan theo ngày mượn
CALL sp_ThongKeMuonTra('2025-01-01', '2026-12-31', NULL);
```

Thủ tục báo lỗi (SIGNAL 45000) khi thiếu ngày, từ ngày sau đến ngày, hoặc loại lọc sai.

## 4. Dữ liệu mẫu

`seed.sql` thêm 5 phiếu `PM_BC01..05` đi qua đúng luồng nghiệp vụ (tạo, thêm thiết bị, duyệt, trả): đúng hạn, trả quá hạn, trả một phần, quá hạn chưa trả, đang mượn trong hạn. Dùng chung thành viên và thiết bị `TB09_*` của seed nền; file chạy sau trigger của module 10.

## 5. Kiểm thử

`BaoCaoMuonTraTest` (không cần CSDL), `BaoCaoMuonTraDatabaseTest` (cần `-Dclb.mysql.tests=true`), `sql/modules/11_baocaomuontra/tests.sql`.
