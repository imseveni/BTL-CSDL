-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Mã riêng PM10_*; thiết bị dùng TB09_* của seed module 09 (phải chạy trước, đúng thứ tự run_all.sql).
-- Không tạo/sửa thành viên: người mượn chọn từ ThanhVien đã có thật, xoay vòng theo thứ tự MSV.
-- Nếu seed_master còn trống thì mọi câu dưới chèn/cập nhật 0 dòng; chạy lại file khi đã có thành viên.
--
-- Cách làm: dữ liệu đi qua đúng luồng nghiệp vụ (tạo phiếu ChoDuyet -> thêm thiết bị -> duyệt/từ chối -> trả)
-- nên chạy được cả khi trigger của queries.sql đã tồn tại (chạy lại riêng file này) lẫn chưa tồn tại (run_all.sql).
-- Mọi câu đều có điều kiện để chạy lại không chèn trùng, không trả/duyệt lần hai.
-- Cuối file đồng bộ SoLuongKhaDung (cần khi trigger chưa tồn tại lúc seed chạy lần đầu).

-- 1) Phiếu mượn (luôn ChoDuyet lúc tạo)
INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
SELECT 'PM10_01', '2025-09-01', '2025-09-05', 'Sinh hoạt chung đầu năm', 'ChoDuyet', m.MSV
FROM (SELECT MSV, ROW_NUMBER() OVER (ORDER BY MSV) AS rn, COUNT(*) OVER () AS total FROM ThanhVien) m
WHERE m.rn = 1 + (4 % m.total) AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_01');
INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
SELECT 'PM10_02', '2025-10-10', '2025-10-12', 'Workshop kỹ năng', 'ChoDuyet', m.MSV
FROM (SELECT MSV, ROW_NUMBER() OVER (ORDER BY MSV) AS rn, COUNT(*) OVER () AS total FROM ThanhVien) m
WHERE m.rn = 1 + (5 % m.total) AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_02');
INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
SELECT 'PM10_03', '2025-11-03', '2025-11-07', 'Chương trình văn nghệ', 'ChoDuyet', m.MSV
FROM (SELECT MSV, ROW_NUMBER() OVER (ORDER BY MSV) AS rn, COUNT(*) OVER () AS total FROM ThanhVien) m
WHERE m.rn = 1 + (6 % m.total) AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_03');
INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
SELECT 'PM10_04', '2026-12-01', '2026-12-03', 'Hội thảo định hướng nghề nghiệp', 'ChoDuyet', m.MSV
FROM (SELECT MSV, ROW_NUMBER() OVER (ORDER BY MSV) AS rn, COUNT(*) OVER () AS total FROM ThanhVien) m
WHERE m.rn = 1 + (7 % m.total) AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_04');
INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
SELECT 'PM10_05', '2025-12-05', '2025-12-06', 'Mượn cá nhân', 'ChoDuyet', m.MSV
FROM (SELECT MSV, ROW_NUMBER() OVER (ORDER BY MSV) AS rn, COUNT(*) OVER () AS total FROM ThanhVien) m
WHERE m.rn = 1 + (4 % m.total) AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_05');

-- 2) Chi tiết mượn (chỉ thêm vào phiếu còn ChoDuyet và dòng chưa có).
-- Mỗi dòng một câu lệnh, so sánh trực tiếp với cột để tránh lỗi trộn collation của bảng tạm.
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 1, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_01'
WHERE p.MaPhieuMuon = 'PM10_01' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 2, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_03'
WHERE p.MaPhieuMuon = 'PM10_01' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 4, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_04'
WHERE p.MaPhieuMuon = 'PM10_02' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 20, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_05'
WHERE p.MaPhieuMuon = 'PM10_02' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 1, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_02'
WHERE p.MaPhieuMuon = 'PM10_03' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 3, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_03'
WHERE p.MaPhieuMuon = 'PM10_03' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 1, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_07'
WHERE p.MaPhieuMuon = 'PM10_03' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 2, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_01'
WHERE p.MaPhieuMuon = 'PM10_04' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 2, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_06'
WHERE p.MaPhieuMuon = 'PM10_04' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);
INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
SELECT p.MaPhieuMuon, t.MaThietBi, 2, NULL
FROM PhieuMuon p JOIN CoSoVatChat t ON t.MaThietBi = 'TB09_07'
WHERE p.MaPhieuMuon = 'PM10_05' AND p.TrangThaiDuyet = 'ChoDuyet'
  AND NOT EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = p.MaPhieuMuon AND c.MaThietBi = t.MaThietBi);

-- 3) Duyệt PM10_01..03 (PM10_04 để ChoDuyet làm ví dụ phiếu chờ), từ chối PM10_05
UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet'
WHERE MaPhieuMuon IN ('PM10_01', 'PM10_02', 'PM10_03') AND TrangThaiDuyet = 'ChoDuyet'
  AND EXISTS (SELECT 1 FROM ChiTietMuon c WHERE c.MaPhieuMuon = PhieuMuon.MaPhieuMuon);
UPDATE PhieuMuon SET TrangThaiDuyet = 'TuChoi'
WHERE MaPhieuMuon = 'PM10_05' AND TrangThaiDuyet = 'ChoDuyet';

-- 4) Ghi nhận trả: PM10_01 đúng hạn, PM10_02 muộn 2 ngày; PM10_03 chưa trả (quá hạn)
UPDATE ChiTietMuon SET NgayTraThucTe = '2025-09-05'
WHERE MaPhieuMuon = 'PM10_01' AND NgayTraThucTe IS NULL
  AND EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_01' AND TrangThaiDuyet = 'DaDuyet');
UPDATE ChiTietMuon SET NgayTraThucTe = '2025-10-14'
WHERE MaPhieuMuon = 'PM10_02' AND NgayTraThucTe IS NULL
  AND EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = 'PM10_02' AND TrangThaiDuyet = 'DaDuyet');

-- 5) Đồng bộ SoLuongKhaDung của thiết bị seed = tổng - (đã duyệt, chưa trả). Không đổi nếu đã khớp.
UPDATE CoSoVatChat t
SET t.SoLuongKhaDung = t.TongSoLuong - COALESCE((
        SELECT SUM(c.SoLuongMuon)
        FROM ChiTietMuon c JOIN PhieuMuon p ON p.MaPhieuMuon = c.MaPhieuMuon
        WHERE c.MaThietBi = t.MaThietBi AND p.TrangThaiDuyet = 'DaDuyet' AND c.NgayTraThucTe IS NULL), 0)
WHERE LEFT(t.MaThietBi, 5) = 'TB09_';
