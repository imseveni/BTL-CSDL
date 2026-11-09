-- ============================================================================
-- MODULE 11: BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT (BORROWING & RETURNING REPORTS)
-- Sử dụng lại các bảng đã tồn tại: PhieuMuon, ChiTietMuon, CoSoVatChat, ThanhVien
-- ============================================================================

USE QuanLyCauLacBo;

-- 1. Truy vấn danh sách chi tiết giao dịch mượn – trả lọc theo NGÀY MƯỢN
--    Chỉ thống kê các phiếu mượn đã được phê duyệt (TrangThaiDuyet = 'DaDuyet')
SELECT pm.MaPhieuMuon,
       ct.MaThietBi,
       tb.TenThietBi,
       pm.MSVNguoiMuon,
       tv.HoVaTen AS TenNguoiMuon,
       ct.SoLuongMuon,
       pm.NgayMuon,
       pm.NgayHenTra,
       ct.NgayTraThucTe,
       pm.MucDichMuon,
       CASE
           WHEN ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe <= pm.NgayHenTra THEN 'Đã trả'
           WHEN ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe > pm.NgayHenTra  THEN 'Trả quá hạn'
           WHEN ct.NgayTraThucTe IS NULL     AND CURDATE() > pm.NgayHenTra         THEN 'Quá hạn'
           ELSE 'Chưa trả'
       END AS TrangThaiMuonTra,
       CASE
           WHEN ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe > pm.NgayHenTra
               THEN DATEDIFF(ct.NgayTraThucTe, pm.NgayHenTra)
           WHEN ct.NgayTraThucTe IS NULL AND CURDATE() > pm.NgayHenTra
               THEN DATEDIFF(CURDATE(), pm.NgayHenTra)
           ELSE 0
       END AS SoNgayQuaHan
FROM ChiTietMuon ct
JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
JOIN CoSoVatChat tb ON tb.MaThietBi = ct.MaThietBi
LEFT JOIN ThanhVien tv ON tv.MSV = pm.MSVNguoiMuon
WHERE pm.TrangThaiDuyet = 'DaDuyet'
  AND pm.NgayMuon BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY pm.NgayMuon ASC, pm.MaPhieuMuon ASC, ct.MaThietBi ASC;

-- 2. Truy vấn danh sách chi tiết giao dịch mượn – trả lọc theo NGÀY TRẢ THỰC TẾ
SELECT pm.MaPhieuMuon,
       ct.MaThietBi,
       tb.TenThietBi,
       pm.MSVNguoiMuon,
       tv.HoVaTen AS TenNguoiMuon,
       ct.SoLuongMuon,
       pm.NgayMuon,
       pm.NgayHenTra,
       ct.NgayTraThucTe,
       pm.MucDichMuon,
       CASE
           WHEN ct.NgayTraThucTe <= pm.NgayHenTra THEN 'Đã trả'
           ELSE 'Trả quá hạn'
       END AS TrangThaiMuonTra,
       GREATEST(DATEDIFF(ct.NgayTraThucTe, pm.NgayHenTra), 0) AS SoNgayQuaHan
FROM ChiTietMuon ct
JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
JOIN CoSoVatChat tb ON tb.MaThietBi = ct.MaThietBi
LEFT JOIN ThanhVien tv ON tv.MSV = pm.MSVNguoiMuon
WHERE pm.TrangThaiDuyet = 'DaDuyet'
  AND ct.NgayTraThucTe IS NOT NULL
  AND ct.NgayTraThucTe BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY ct.NgayTraThucTe ASC, pm.MaPhieuMuon ASC, ct.MaThietBi ASC;

-- 3. Truy vấn thống kê tổng quan 6 chỉ số trong khoảng thời gian theo NGÀY MƯỢN
SELECT COUNT(*) AS TongSoLuotMuon,
       SUM(CASE WHEN ct.NgayTraThucTe IS NOT NULL THEN 1 ELSE 0 END) AS TongSoLuotTra,
       COALESCE(SUM(CASE WHEN ct.NgayTraThucTe IS NULL THEN ct.SoLuongMuon ELSE 0 END), 0) AS SoCSVCDangMuon,
       COALESCE(SUM(CASE WHEN ct.NgayTraThucTe IS NOT NULL THEN ct.SoLuongMuon ELSE 0 END), 0) AS SoCSVCDaTra,
       SUM(CASE
               WHEN (ct.NgayTraThucTe IS NOT NULL AND ct.NgayTraThucTe > pm.NgayHenTra)
                 OR (ct.NgayTraThucTe IS NULL AND CURDATE() > pm.NgayHenTra)
               THEN 1 ELSE 0
           END) AS SoTruongHopQuaHan,
       SUM(CASE WHEN ct.NgayTraThucTe IS NULL THEN 1 ELSE 0 END) AS SoTruongHopChuaTra
FROM ChiTietMuon ct
JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
WHERE pm.TrangThaiDuyet = 'DaDuyet'
  AND pm.NgayMuon BETWEEN '2025-01-01' AND '2025-12-31';

