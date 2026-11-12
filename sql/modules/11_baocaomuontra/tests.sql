-- Kiểm thử module 11 (báo cáo mượn – trả). Chạy sau run_all.sql:  mysql -u root -p < modules/11_baocaomuontra/tests.sql
-- Không sửa dữ liệu; in "PASS module 11" nếu đạt, ngược lại dừng với thông báo lỗi.
SET NAMES utf8mb4;
USE QuanLyCauLacBo;

DROP PROCEDURE IF EXISTS sp_TestMinh11;
DELIMITER $$
CREATE PROCEDURE sp_TestMinh11()
BEGIN
    DECLARE v_errno INT DEFAULT 0;
    DECLARE v_n INT DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_errno = MYSQL_ERRNO;

    -- 1. Dữ liệu mẫu: 5 phiếu PM_BC01..05 đã duyệt, đủ 5 tình huống.
    SELECT COUNT(*) INTO v_n FROM PhieuMuon WHERE MaPhieuMuon LIKE 'PM\_BC0%' AND TrangThaiDuyet = 'DaDuyet';
    IF v_n <> 5 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.1: thiếu phiếu mẫu PM_BC01..05'; END IF;
    SELECT COUNT(*) INTO v_n FROM ChiTietMuon ct JOIN PhieuMuon pm USING (MaPhieuMuon)
     WHERE pm.MaPhieuMuon LIKE 'PM\_BC0%' AND ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe > pm.NgayHenTra;
    IF v_n <> 3 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.2: số dòng trả quá hạn của dữ liệu mẫu phải là 3'; END IF;

    -- 2. SoLuongKhaDung của mọi thiết bị = tổng - số đang mượn (phiếu đã duyệt, chưa trả): module 09, 10, 11 khớp nhau.
    SELECT COUNT(*) INTO v_n
      FROM CoSoVatChat c
      LEFT JOIN (SELECT ct.MaThietBi, SUM(ct.SoLuongMuon) AS dang
                   FROM ChiTietMuon ct JOIN PhieuMuon pm USING (MaPhieuMuon)
                  WHERE pm.TrangThaiDuyet = 'DaDuyet' AND ct.NgayTraThucTe IS NULL GROUP BY ct.MaThietBi) m
        ON m.MaThietBi = c.MaThietBi
     WHERE c.SoLuongKhaDung <> c.TongSoLuong - COALESCE(m.dang, 0);
    IF v_n <> 0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.3: SoLuongKhaDung lệch với số đang mượn'; END IF;

    -- 3. Thủ tục báo cáo từ chối tham số sai bằng đúng SIGNAL 45000 (errno 1644).
    SET v_errno = 0; CALL sp_BaoCaoMuonTra('2026-12-31', '2025-01-01', 'NGAY_MUON', NULL);
    IF v_errno <> 1644 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.4: từ ngày sau đến ngày phải bị từ chối'; END IF;
    SET v_errno = 0; CALL sp_BaoCaoMuonTra('2025-01-01', '2026-12-31', 'SAI', NULL);
    IF v_errno <> 1644 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.5: loại lọc sai phải bị từ chối'; END IF;
    SET v_errno = 0; CALL sp_BaoCaoMuonTra(NULL, '2026-12-31', 'NGAY_MUON', NULL);
    IF v_errno <> 1644 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.6: thiếu ngày phải bị từ chối'; END IF;
    SET v_errno = 0; CALL sp_ThongKeMuonTra('2026-12-31', '2025-01-01', NULL);
    IF v_errno <> 1644 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL 11.7: thống kê với khoảng ngày ngược phải bị từ chối'; END IF;

    SELECT 'PASS module 11: dữ liệu mẫu, tồn kho khớp, tham số báo cáo' AS ketqua;
END$$
DELIMITER ;
CALL sp_TestMinh11();
DROP PROCEDURE IF EXISTS sp_TestMinh11;
