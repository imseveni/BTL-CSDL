-- Module 04 (Quản lý Khóa học) – truy vấn / view / stored procedure / trigger. Phụ trách: Chiến
USE QuanLyCauLacBo;

DROP VIEW IF EXISTS vw_KhoaHocThanhVien;
CREATE VIEW vw_KhoaHocThanhVien AS
SELECT k.MaKhoaHoc, k.TenKhoaHoc, k.NgayKhaiGiang, k.NgayBeGiang,
       t.MSV, t.HoVaTen, tg.VaiTro, tg.KetQuaDanhGia
FROM KhoaHoc k
JOIN ThamGiaKhoaHoc tg ON tg.MaKhoaHoc = k.MaKhoaHoc
JOIN ThanhVien t ON t.MSV = tg.MSV;

DROP VIEW IF EXISTS vw_KhoaHocThongKe;
CREATE VIEW vw_KhoaHocThongKe AS
SELECT k.MaKhoaHoc, k.TenKhoaHoc, k.NgayKhaiGiang, k.NgayBeGiang,
       COUNT(tg.MSV) AS SoThanhVien
FROM KhoaHoc k
LEFT JOIN ThamGiaKhoaHoc tg ON tg.MaKhoaHoc = k.MaKhoaHoc
GROUP BY k.MaKhoaHoc, k.TenKhoaHoc, k.NgayKhaiGiang, k.NgayBeGiang;

DROP PROCEDURE IF EXISTS sp_KhoaHocTraCuu;
DELIMITER $$
CREATE PROCEDURE sp_KhoaHocTraCuu(IN p_TuKhoa VARCHAR(150))
BEGIN
    -- LOCATE tìm chuỗi nguyên văn, kể cả khi có ký tự % hoặc _.
    SELECT * FROM vw_KhoaHocThongKe
    WHERE LOCATE(COALESCE(TRIM(p_TuKhoa), ''), MaKhoaHoc) > 0
       OR LOCATE(COALESCE(TRIM(p_TuKhoa), ''), TenKhoaHoc) > 0
    ORDER BY MaKhoaHoc;
END$$
DELIMITER ;

-- Thành viên của khóa: SELECT * FROM vw_KhoaHocThanhVien WHERE MaKhoaHoc = 'KH01';
-- Lịch sử của thành viên: SELECT * FROM vw_KhoaHocThanhVien WHERE MSV = '...';

