-- ============================================================================
-- MODULE 11: BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT
-- Phụ trách: Minh (khớp với module 09, 10 và 01 trong bản tích hợp).
--
-- NGƯỜI LẬP: thành viên Ban Chủ nhiệm đang dùng hệ thống (Chủ nhiệm, Phó chủ nhiệm hoặc người được giao).
-- NƠI NHẬN : Chủ nhiệm CLB và giảng viên phụ trách / Đoàn trường (đơn vị quản lý CLB).
-- KHI NÀO  : cuối tháng hoặc cuối học kỳ, để kiểm kê thiết bị chưa trả, trả quá hạn.
--
-- Chỉ tính phiếu đã duyệt (TrangThaiDuyet = 'DaDuyet'). Nhãn trạng thái dùng chung với ứng dụng Java:
--   'Đã trả', 'Trả quá hạn', 'Chưa trả', 'Quá hạn' (chưa trả và đã quá ngày hẹn trả).
-- Tham số p_NgayMoc: ngày dùng để xét "quá hạn" cho thiết bị chưa trả; NULL = hôm nay.
-- ============================================================================
SET NAMES utf8mb4;
USE QuanLyCauLacBo;

DROP PROCEDURE IF EXISTS sp_BaoCaoMuonTra;
DROP PROCEDURE IF EXISTS sp_ThongKeMuonTra;

DELIMITER $$

-- 1) Chi tiết giao dịch mượn – trả. p_Loai: 'NGAY_MUON' (lọc theo ngày mượn) hoặc 'NGAY_TRA' (lọc theo ngày trả thực tế).
CREATE PROCEDURE sp_BaoCaoMuonTra(IN p_TuNgay DATE, IN p_DenNgay DATE, IN p_Loai VARCHAR(20), IN p_NgayMoc DATE)
BEGIN
    DECLARE v_Moc DATE DEFAULT COALESCE(p_NgayMoc, CURDATE());
    IF p_TuNgay IS NULL OR p_DenNgay IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phải nhập từ ngày và đến ngày.';
    END IF;
    IF p_TuNgay > p_DenNgay THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Từ ngày không được sau đến ngày.';
    END IF;
    IF p_Loai NOT IN ('NGAY_MUON', 'NGAY_TRA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loại lọc phải là NGAY_MUON hoặc NGAY_TRA.';
    END IF;

    SELECT pm.MaPhieuMuon, ct.MaThietBi, tb.TenThietBi, pm.MSVNguoiMuon, tv.HoVaTen AS TenNguoiMuon,
           ct.SoLuongMuon, pm.NgayMuon, pm.NgayHenTra, ct.NgayTraThucTe, pm.MucDichMuon,
           CASE
               WHEN ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe <= pm.NgayHenTra THEN 'Đã trả'
               WHEN ct.NgayTraThucTe IS NOT NULL                                       THEN 'Trả quá hạn'
               WHEN v_Moc > pm.NgayHenTra                                              THEN 'Quá hạn'
               ELSE 'Chưa trả'
           END AS TrangThaiMuonTra,
           CASE
               WHEN ct.NgayTraThucTe IS NOT NULL THEN GREATEST(DATEDIFF(ct.NgayTraThucTe, pm.NgayHenTra), 0)
               ELSE GREATEST(DATEDIFF(v_Moc, pm.NgayHenTra), 0)
           END AS SoNgayQuaHan
    FROM ChiTietMuon ct
    JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
    JOIN CoSoVatChat tb ON tb.MaThietBi = ct.MaThietBi
    LEFT JOIN ThanhVien tv ON tv.MSV = pm.MSVNguoiMuon
    WHERE pm.TrangThaiDuyet = 'DaDuyet'
      AND ((p_Loai = 'NGAY_MUON' AND pm.NgayMuon BETWEEN p_TuNgay AND p_DenNgay)
        OR (p_Loai = 'NGAY_TRA' AND ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe BETWEEN p_TuNgay AND p_DenNgay))
    ORDER BY CASE WHEN p_Loai = 'NGAY_TRA' THEN ct.NgayTraThucTe ELSE pm.NgayMuon END, pm.MaPhieuMuon, ct.MaThietBi;
END$$

-- 2) Sáu chỉ số tổng quan của các phiếu mượn trong khoảng ngày mượn.
CREATE PROCEDURE sp_ThongKeMuonTra(IN p_TuNgay DATE, IN p_DenNgay DATE, IN p_NgayMoc DATE)
BEGIN
    DECLARE v_Moc DATE DEFAULT COALESCE(p_NgayMoc, CURDATE());
    IF p_TuNgay IS NULL OR p_DenNgay IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phải nhập từ ngày và đến ngày.';
    END IF;
    IF p_TuNgay > p_DenNgay THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Từ ngày không được sau đến ngày.';
    END IF;

    SELECT COUNT(*) AS TongSoLuotMuon,
           COALESCE(SUM(ct.NgayTraThucTe IS NOT NULL), 0) AS TongSoLuotTra,
           COALESCE(SUM(CASE WHEN ct.NgayTraThucTe IS NULL THEN ct.SoLuongMuon ELSE 0 END), 0) AS SoCSVCDangMuon,
           COALESCE(SUM(CASE WHEN ct.NgayTraThucTe IS NOT NULL THEN ct.SoLuongMuon ELSE 0 END), 0) AS SoCSVCDaTra,
           COALESCE(SUM((ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe > pm.NgayHenTra)
                     OR (ct.NgayTraThucTe IS NULL AND v_Moc > pm.NgayHenTra)), 0) AS SoTruongHopQuaHan,
           COALESCE(SUM(ct.NgayTraThucTe IS NULL), 0) AS SoTruongHopChuaTra
    FROM ChiTietMuon ct
    JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
    WHERE pm.TrangThaiDuyet = 'DaDuyet'
      AND pm.NgayMuon BETWEEN p_TuNgay AND p_DenNgay;
END$$

DELIMITER ;
