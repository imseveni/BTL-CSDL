-- ============================================================================
-- DỮ LIỆU MẪU ĐẦY ĐỦ CHO BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT (XUẤT PDF)
-- Tự động nạp cả Thành viên nền + Cơ sở vật chất + Phiếu mượn + Duyệt + Trả.
-- Tương thích 100% với MySQL Workbench (tắt Safe Updates) và Trigger Module 10.
-- ============================================================================

USE QuanLyCauLacBo;
SET SQL_SAFE_UPDATES = 0;

-- 1. Đảm bảo có Ban chuyên môn & Thành viên nền (nếu chưa chạy seed_master.sql)
INSERT IGNORE INTO BanChuyenMon (MaBan, TenBan) VALUES
    ('BAN01', 'Ban Chủ nhiệm'),
    ('BAN02', 'Ban Chuyên môn'),
    ('BAN03', 'Ban Truyền thông'),
    ('BAN04', 'Ban Sự kiện'),
    ('BAN05', 'Ban Hậu cần');

INSERT IGNORE INTO ThanhVien
    (MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT, NgayGiaNhap, TrangThaiHD, MaBan, ChucVu) VALUES
    ('B21DCCN001', 'Nguyễn Văn An',  '2003-02-14', 'Nam', 'b21dccn001@example.com', '0900000001', '2023-09-10', 'Đang hoạt động', 'BAN01', 'Chủ nhiệm'),
    ('B21DCCN002', 'Trần Thị Bình',  '2003-05-21', 'Nữ',  'b21dccn002@example.com', '0900000002', '2023-09-10', 'Đang hoạt động', 'BAN01', 'Phó chủ nhiệm'),
    ('B21DCCN003', 'Lê Quang Cường', '2003-11-03', 'Nam', 'b21dccn003@example.com', '0900000003', '2023-09-10', 'Đang hoạt động', 'BAN02', 'Trưởng ban'),
    ('B21DCCN004', 'Phạm Thu Dung',  '2003-07-30', 'Nữ',  'b21dccn004@example.com', '0900000004', '2023-09-10', 'Đang hoạt động', 'BAN03', 'Trưởng ban'),
    ('B21DCCN005', 'Hoàng Minh Đức', '2004-01-09', 'Nam', 'b21dccn005@example.com', '0900000005', '2023-10-02', 'Đang hoạt động', 'BAN04', 'Trưởng ban');

-- 2. Đảm bảo có danh mục Cơ sở vật chất với đủ số lượng khả dụng
INSERT IGNORE INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy) VALUES
    ('TB09_01', 'Máy chiếu',         5,  5,  'B21DCCN001'),
    ('TB09_02', 'Loa kéo di động',   4,  4,  'B21DCCN001'),
    ('TB09_03', 'Micro không dây',   10, 10, 'B21DCCN002'),
    ('TB09_04', 'Bàn gấp',           15, 15, 'B21DCCN002'),
    ('TB09_05', 'Ghế nhựa',          50, 50, 'B21DCCN003'),
    ('TB09_06', 'Banner khung treo', 6,  6,  'B21DCCN003'),
    ('TB09_07', 'Máy ảnh DSLR',      4,  4,  'B21DCCN004'),
    ('TB09_08', 'Ổ cắm kéo dài',     8,  8,  NULL);

-- 3. Tạo 5 phiếu mượn mẫu ở trạng thái 'ChoDuyet'
INSERT IGNORE INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon) VALUES
    ('PM_BC01', '2025-09-10', '2025-09-15', 'Ngày hội Chào tân sinh viên CLB',    'ChoDuyet', 'B21DCCN001'),
    ('PM_BC02', '2025-10-05', '2025-10-10', 'Workshop Git & GitHub thực chiến',   'ChoDuyet', 'B21DCCN002'),
    ('PM_BC03', '2025-10-18', '2025-10-22', 'Cuộc thi Hackathon Mùa Thu',         'ChoDuyet', 'B21DCCN003'),
    ('PM_BC04', '2025-11-01', '2025-11-04', 'Seminar Định hướng AI & Data',       'ChoDuyet', 'B21DCCN004'),
    ('PM_BC05', '2026-11-01', '2026-12-25', 'Chuẩn bị Đêm Gala Tổng kết cuối năm', 'ChoDuyet', 'B21DCCN005');

-- 4. Thêm chi tiết cơ sở vật chất vào các phiếu đang ở trạng thái 'ChoDuyet'
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

-- 5. Duyệt từng phiếu mượn sang trạng thái 'DaDuyet'
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC01' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC02' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC03' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC04' AND TrangThaiDuyet = 'ChoDuyet';
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = 'PM_BC05' AND TrangThaiDuyet = 'ChoDuyet';

-- 6. Ghi nhận ngày trả thực tế theo đúng khóa chính (MaPhieuMuon, MaThietBi)
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

-- Kiểm tra kết quả ngay sau khi chạy:
SELECT pm.MaPhieuMuon, ct.MaThietBi, tb.TenThietBi, pm.MSVNguoiMuon,
       ct.SoLuongMuon, pm.NgayMuon, pm.NgayHenTra, ct.NgayTraThucTe, pm.TrangThaiDuyet
FROM ChiTietMuon ct
JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
JOIN CoSoVatChat tb ON tb.MaThietBi = ct.MaThietBi
WHERE pm.TrangThaiDuyet = 'DaDuyet';
