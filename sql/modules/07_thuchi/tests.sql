-- Module 07 (Quản lý Thu – Chi) – ca thử (đúng + sai) để kiểm tra ràng buộc. Phụ trách: Minh
USE QuanLyCauLacBo;

-- =====================================================================
-- PHẦN 1: CÁC CA THỬ ĐÚNG (HỢP LỆ)
-- =====================================================================

-- [TEST ĐÚNG 1] Thêm giao dịch mới kèm nhiều phương thức thanh toán trong 1 transaction
START TRANSACTION;
CALL sp_ThemGiaoDich(
    'GD_TEST01',
    'Thu',
    'Tài trợ bổ sung cho Seminar AI & Data',
    2500000.00,
    '2026-10-25 14:00:00',
    'UNC_SeminarAI.pdf',
    NULL,
    'SK04'
);
CALL sp_ThemPhuongThucThanhToan('GD_TEST01', 'Chuyển khoản ngân hàng');
CALL sp_ThemPhuongThucThanhToan('GD_TEST01', 'Tiền mặt');
COMMIT;

-- Kiểm tra giao dịch vừa thêm qua view chi tiết
SELECT * FROM vw_ChiTietGiaoDich WHERE MaGD = 'GD_TEST01';

-- [TEST ĐÚNG 2] Cập nhật thông tin giao dịch
CALL sp_CapNhatGiaoDich(
    'GD_TEST01',
    'Thu',
    'Tài trợ vàng cho Seminar AI & Data',
    3000000.00,
    '2026-10-25 15:00:00',
    'UNC_SeminarAI_Updated.pdf',
    NULL,
    'SK04'
);

-- [TEST ĐÚNG 3] Tra cứu giao dịch theo sự kiện SK04
CALL sp_TraCuuGiaoDichTheoSuKien('SK04');

-- [TEST ĐÚNG 4] Thống kê tổng thu, tổng chi và chênh lệch theo từng sự kiện
CALL sp_ThongKeThuChiTheoSuKien();

-- [TEST ĐÚNG 5] Xóa giao dịch thử nghiệm GD_TEST01
CALL sp_XoaGiaoDich('GD_TEST01');

-- =====================================================================
-- PHẦN 2: CÁC CA THỬ SAI (VI PHẠM RÀNG BUỘC - KỲ VỌNG BỊ CHẶN)
-- Chạy từng câu lệnh dưới đây để kiểm tra thông báo lỗi.
-- =====================================================================

-- [TEST SAI 1] Thêm giao dịch có LoaiGD khác 'Thu' hoặc 'Chi'
-- Kỳ vọng: Bị chặn bởi trg_ThuChi_BeforeInsert / CK_ThuChi_LoaiGD
-- CALL sp_ThemGiaoDich('GD_ERR01', 'VayMuon', 'Giao dịch sai loại', 500000.00, NOW(), NULL, NULL, 'SK01');

-- [TEST SAI 2] Thêm giao dịch có SoTien <= 0
-- Kỳ vọng: Bị chặn bởi trg_ThuChi_BeforeInsert / CK_ThuChi_SoTien
-- CALL sp_ThemGiaoDich('GD_ERR02', 'Chi', 'Giao dịch số tiền âm', -100000.00, NOW(), NULL, NULL, 'SK01');
-- CALL sp_ThemGiaoDich('GD_ERR03', 'Thu', 'Giao dịch số tiền bằng 0', 0.00, NOW(), NULL, NULL, 'SK01');

-- [TEST SAI 3] Thêm trùng phương thức thanh toán cho cùng 1 giao dịch ('GD01', 'Chuyển khoản ngân hàng')
-- Kỳ vọng: Bị chặn bởi PRIMARY KEY (MaGD, PhuongThucThanhToan)
-- CALL sp_ThemPhuongThucThanhToan('GD01', 'Chuyển khoản ngân hàng');

-- [TEST SAI 4] Xóa sự kiện đang còn giao dịch thu chi ('SK01')
-- Kỳ vọng: Bị chặn bởi FK_ThuChi_SuKien (ON DELETE RESTRICT)
-- DELETE FROM SuKien WHERE MaSK = 'SK01';
