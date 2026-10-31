-- Module 06 (Quản lý Sự kiện) – ca thử (đúng + sai) để kiểm tra ràng buộc. Phụ trách: Minh
USE QuanLyCauLacBo;

-- =====================================================================
-- PHẦN 1: CÁC CA THỬ ĐÚNG (HỢP LỆ)
-- =====================================================================

-- [TEST ĐÚNG 1] Thêm sự kiện mới hợp lệ bằng thủ tục sp_ThemSuKien
CALL sp_ThemSuKien('SK_TEST01', 'Workshop Kỹ năng mềm', '2026-11-15', '2026-11-16');
SELECT * FROM SuKien WHERE MaSK = 'SK_TEST01';

-- [TEST ĐÚNG 2] Cập nhật thông tin sự kiện bằng thủ tục sp_CapNhatSuKien
CALL sp_CapNhatSuKien('SK_TEST01', 'Workshop Kỹ năng giao tiếp & thuyết trình', '2026-11-15', '2026-11-17');
SELECT * FROM SuKien WHERE MaSK = 'SK_TEST01';

-- [TEST ĐÚNG 3] Tra cứu thời gian diễn ra sự kiện từ 2026-09-01 đến 2026-11-30
CALL sp_TraCuuSuKienTheoThoiGian('2026-09-01', '2026-11-30');

-- [TEST ĐÚNG 4] Xem view tổng hợp danh sách sự kiện và số lượng thành viên
SELECT * FROM vw_DanhSachSuKien;

-- [TEST ĐÚNG 5] Xóa sự kiện chưa có giao dịch thu chi (SK_TEST01)
CALL sp_XoaSuKien('SK_TEST01');

-- =====================================================================
-- PHẦN 2: CÁC CA THỬ SAI (VI PHẠM RÀNG BUỘC - KỲ VỌNG BỊ CHẶN)
-- Chạy từng câu lệnh dưới đây để kiểm tra thông báo lỗi.
-- =====================================================================

-- [TEST SAI 1] Thêm sự kiện có NgayKetThuc < NgayBatDau
-- Kỳ vọng: Bị chặn bởi trg_SuKien_BeforeInsert / CK_SuKien_ThoiGian
-- CALL sp_ThemSuKien('SK_ERR01', 'Sự kiện lỗi ngày', '2026-10-20', '2026-10-10');

-- [TEST SAI 2] Thêm sự kiện trùng mã MaSK đã tồn tại ('SK01')
-- Kỳ vọng: Bị chặn bởi sp_ThemSuKien / PRIMARY KEY
-- CALL sp_ThemSuKien('SK01', 'Sự kiện trùng mã', '2026-10-01', '2026-10-02');

-- [TEST SAI 3] Thêm thành viên không tồn tại vào sự kiện
-- Kỳ vọng: Bị chặn bởi sp_ThemThanhVienSuKien / FK_TGSK_ThanhVien
-- CALL sp_ThemThanhVienSuKien('MSV_KHONG_TON_TAI', 'SK01', 'Tình nguyện viên');

-- [TEST SAI 4] Xóa sự kiện đang có giao dịch thu chi liên kết ('SK01')
-- Kỳ vọng: Bị chặn bởi sp_XoaSuKien / FK_ThuChi_SuKien (ON DELETE RESTRICT)
-- CALL sp_XoaSuKien('SK01');
