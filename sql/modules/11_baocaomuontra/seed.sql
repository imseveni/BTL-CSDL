-- ============================================================================
-- DỮ LIỆU MẪU CHO BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT (PM_BC01..PM_BC05)
-- Dùng chung thành viên (seed_master) và thiết bị TB09_* (seed module 09); không tạo lại dữ liệu nền.
-- Đi qua đúng luồng nghiệp vụ (tạo phiếu ChoDuyet -> thêm thiết bị -> duyệt -> trả) nên cần trigger của
-- module 10 đã có: run_all.sql chạy file này SAU modules/10_muontra/queries.sql. Chạy lại không chèn trùng.
-- Các trường hợp: đã trả đúng hạn, trả quá hạn, trả một phần, quá hạn chưa trả, đang mượn trong hạn.
-- ============================================================================
SET NAMES utf8mb4;
USE QuanLyCauLacBo;
SET SQL_SAFE_UPDATES = 0;

-- 1. Tạo 5 phiếu mượn mẫu ở trạng thái 'ChoDuyet'
INSERT IGNORE INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon) VALUES
    ('PM_BC01', '2025-09-10', '2025-09-15', 'Ngày hội Chào tân sinh viên CLB',    'ChoDuyet', 'B21DCCN001'),
    ('PM_BC02', '2025-10-05', '2025-10-10', 'Workshop Git & GitHub thực chiến',   'ChoDuyet', 'B21DCCN002'),
    ('PM_BC03', '2025-10-18', '2025-10-22', 'Cuộc thi Hackathon Mùa Thu',         'ChoDuyet', 'B21DCCN003'),
    ('PM_BC04', '2025-11-01', '2025-11-04', 'Seminar Định hướng AI & Data',       'ChoDuyet', 'B21DCCN004'),
    ('PM_BC05', '2026-11-01', '2026-12-25', 'Chuẩn bị Đêm Gala Tổng kết cuối năm', 'ChoDuyet', 'B21DCCN005');

-- 2. Thêm chi tiết cơ sở vật chất vào các phiếu đang ở trạng thái 'ChoDuyet'
INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC01', 'TB09_01', 1, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC01' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC01', 'TB09_06', 2, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC01' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC02', 'TB09_02', 1, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC02' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC02', 'TB09_08', 2, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC02' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC03', 'TB09_04', 3, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC03' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC03', 'TB09_05', 10, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC03' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC04', 'TB09_08', 1, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC04' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC05', 'TB09_06', 1, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC05' AND TrangThaiDuyet = 'ChoDuyet';

INSERT IGNORE INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT 'PM_BC05', 'TB09_08', 1, NULL FROM PhieuMuon WHERE MaPhieuMuon = 'PM_BC05' AND TrangThaiDuyet = 'ChoDuyet';

-- 3. Duyệt từng phiếu mượn sang trạng thái 'DaDuyet'
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC01' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC02' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC03' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC04' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC05' AND TrangThaiDuyet = 'ChoDuyet';

-- 4. Ghi nhận ngày trả thực tế theo đúng khóa chính (MaPhieuMuon, MaThietBi)
-- PM_BC01: Đã trả đúng hạn (2025-09-14 <= 2025-09-15)
UPDATE ChiTietMuon SET NgayTraThucTe = '2025-09-14'
WHERE MaPhieuMuon = 'PM_BC01' AND MaThietBi = 'TB09_01' AND NgayTraThucTe IS NULL;

UPDATE ChiTietMuon SET NgayTraThucTe = '2025-09-14'
WHERE MaPhieuMuon = 'PM_BC01' AND MaThietBi = 'TB09_06' AND NgayTraThucTe IS NULL;

-- PM_BC02: Trả quá hạn 3 ngày (2025-10-13 > 2025-10-10)
UPDATE ChiTietMuon SET NgayTraThucTe = '2025-10-13'
WHERE MaPhieuMuon = 'PM_BC02' AND MaThietBi = 'TB09_02' AND NgayTraThucTe IS NULL;

UPDATE ChiTietMuon SET NgayTraThucTe = '2025-10-13'
WHERE MaPhieuMuon = 'PM_BC02' AND MaThietBi = 'TB09_08' AND NgayTraThucTe IS NULL;

-- PM_BC03: 1 thiết bị trả đúng hạn (2025-10-22), 1 thiết bị trả quá hạn 4 ngày (2025-10-26)
UPDATE ChiTietMuon SET NgayTraThucTe = '2025-10-22'
WHERE MaPhieuMuon = 'PM_BC03' AND MaThietBi = 'TB09_04' AND NgayTraThucTe IS NULL;

UPDATE ChiTietMuon SET NgayTraThucTe = '2025-10-26'
WHERE MaPhieuMuon = 'PM_BC03' AND MaThietBi = 'TB09_05' AND NgayTraThucTe IS NULL;

-- PM_BC04 (Quá hạn chưa trả) và PM_BC05 (Đang mượn trong hạn) giữ NgayTraThucTe = NULL.

SET SQL_SAFE_UPDATES = 1;
