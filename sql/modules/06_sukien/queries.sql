-- Module 06 (Quản lý Sự kiện) – truy vấn / view / stored procedure / trigger. Phụ trách: Minh
USE QuanLyCauLacBo;

-- =====================================================================
-- 1. VIEW: Danh sách sự kiện kèm số lượng thành viên tham gia
-- =====================================================================
DROP VIEW IF EXISTS vw_DanhSachSuKien;
CREATE VIEW vw_DanhSachSuKien AS
SELECT
    sk.MaSK,
    sk.TenSK,
    sk.NgayBatDau,
    sk.NgayKetThuc,
    COUNT(tg.MSV) AS SoThanhVienThamGia
FROM SuKien sk
LEFT JOIN ThamGiaSuKien tg ON sk.MaSK = tg.MaSK
GROUP BY sk.MaSK, sk.TenSK, sk.NgayBatDau, sk.NgayKetThuc;

-- =====================================================================
-- 2. TRIGGER: Kiểm tra hợp lệ trước khi thêm/cập nhật sự kiện
-- =====================================================================
DROP TRIGGER IF EXISTS trg_SuKien_BeforeInsert;
DELIMITER $$
CREATE TRIGGER trg_SuKien_BeforeInsert
BEFORE INSERT ON SuKien
FOR EACH ROW
BEGIN
    IF TRIM(NEW.MaSK) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sự kiện không được để trống';
    END IF;
    IF TRIM(NEW.TenSK) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên sự kiện không được để trống';
    END IF;
    IF NEW.NgayKetThuc IS NOT NULL AND NEW.NgayKetThuc < NEW.NgayBatDau THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu';
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_SuKien_BeforeUpdate;
DELIMITER $$
CREATE TRIGGER trg_SuKien_BeforeUpdate
BEFORE UPDATE ON SuKien
FOR EACH ROW
BEGIN
    IF TRIM(NEW.TenSK) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên sự kiện không được để trống';
    END IF;
    IF NEW.NgayKetThuc IS NOT NULL AND NEW.NgayKetThuc < NEW.NgayBatDau THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu';
    END IF;
END$$
DELIMITER ;

-- =====================================================================
-- 3. STORED PROCEDURES: Nghiệp vụ Quản lý Sự kiện
-- =====================================================================

-- 3.1. Thêm mới sự kiện
DROP PROCEDURE IF EXISTS sp_ThemSuKien;
DELIMITER $$
CREATE PROCEDURE sp_ThemSuKien(
    IN p_MaSK VARCHAR(20),
    IN p_TenSK VARCHAR(150),
    IN p_NgayBatDau DATE,
    IN p_NgayKetThuc DATE
)
BEGIN
    IF EXISTS (SELECT 1 FROM SuKien WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sự kiện đã tồn tại';
    END IF;

    INSERT INTO SuKien (MaSK, TenSK, NgayBatDau, NgayKetThuc)
    VALUES (p_MaSK, p_TenSK, p_NgayBatDau, p_NgayKetThuc);
END$$
DELIMITER ;

-- 3.2. Cập nhật thông tin sự kiện
DROP PROCEDURE IF EXISTS sp_CapNhatSuKien;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatSuKien(
    IN p_MaSK VARCHAR(20),
    IN p_TenSK VARCHAR(150),
    IN p_NgayBatDau DATE,
    IN p_NgayKetThuc DATE
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM SuKien WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy sự kiện cần cập nhật';
    END IF;

    UPDATE SuKien
    SET TenSK = p_TenSK,
        NgayBatDau = p_NgayBatDau,
        NgayKetThuc = p_NgayKetThuc
    WHERE MaSK = p_MaSK;
END$$
DELIMITER ;

-- 3.3. Xóa sự kiện (kiểm tra ràng buộc giao dịch ThuChi trước khi xóa)
DROP PROCEDURE IF EXISTS sp_XoaSuKien;
DELIMITER $$
CREATE PROCEDURE sp_XoaSuKien(
    IN p_MaSK VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM SuKien WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy sự kiện cần xóa';
    END IF;

    IF EXISTS (SELECT 1 FROM ThuChi WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa sự kiện đã có giao dịch thu chi liên kết';
    END IF;

    DELETE FROM SuKien WHERE MaSK = p_MaSK;
END$$
DELIMITER ;

-- 3.4. Tra cứu sự kiện diễn ra trong khoảng thời gian [p_TuNgay, p_DenNgay]
DROP PROCEDURE IF EXISTS sp_TraCuuSuKienTheoThoiGian;
DELIMITER $$
CREATE PROCEDURE sp_TraCuuSuKienTheoThoiGian(
    IN p_TuNgay DATE,
    IN p_DenNgay DATE
)
BEGIN
    SELECT
        sk.MaSK,
        sk.TenSK,
        sk.NgayBatDau,
        sk.NgayKetThuc,
        COUNT(tg.MSV) AS SoThanhVienThamGia
    FROM SuKien sk
    LEFT JOIN ThamGiaSuKien tg ON sk.MaSK = tg.MaSK
    WHERE (p_TuNgay IS NULL OR COALESCE(sk.NgayKetThuc, sk.NgayBatDau) >= p_TuNgay)
      AND (p_DenNgay IS NULL OR sk.NgayBatDau <= p_DenNgay)
    GROUP BY sk.MaSK, sk.TenSK, sk.NgayBatDau, sk.NgayKetThuc
    ORDER BY sk.NgayBatDau ASC, sk.MaSK ASC;
END$$
DELIMITER ;

-- 3.5. Thêm thành viên tham gia sự kiện + ghi nhận vai trò
DROP PROCEDURE IF EXISTS sp_ThemThanhVienSuKien;
DELIMITER $$
CREATE PROCEDURE sp_ThemThanhVienSuKien(
    IN p_MSV VARCHAR(20),
    IN p_MaSK VARCHAR(20),
    IN p_VaiTro VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = p_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sinh viên không tồn tại trong hệ thống';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM SuKien WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sự kiện không tồn tại';
    END IF;

    IF EXISTS (SELECT 1 FROM ThamGiaSuKien WHERE MSV = p_MSV AND MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên này đã được ghi nhận trong sự kiện';
    END IF;

    INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
    VALUES (p_MSV, p_MaSK, p_VaiTro);
END$$
DELIMITER ;

-- 3.6. Xem danh sách thành viên của một sự kiện
DROP PROCEDURE IF EXISTS sp_XemThanhVienTheoSuKien;
DELIMITER $$
CREATE PROCEDURE sp_XemThanhVienTheoSuKien(
    IN p_MaSK VARCHAR(20)
)
BEGIN
    SELECT
        tg.MaSK,
        sk.TenSK,
        tg.MSV,
        tv.HoVaTen,
        tv.ChucVu,
        tv.MaBan,
        tg.VaiTro
    FROM ThamGiaSuKien tg
    INNER JOIN SuKien sk ON tg.MaSK = sk.MaSK
    INNER JOIN ThanhVien tv ON tg.MSV = tv.MSV
    WHERE tg.MaSK = p_MaSK
    ORDER BY tg.MSV ASC;
END$$
DELIMITER ;

-- 3.7. Xem lịch sử tham gia sự kiện của một thành viên
DROP PROCEDURE IF EXISTS sp_XemLichSuSuKienThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_XemLichSuSuKienThanhVien(
    IN p_MSV VARCHAR(20)
)
BEGIN
    SELECT
        tg.MSV,
        tv.HoVaTen,
        sk.MaSK,
        sk.TenSK,
        sk.NgayBatDau,
        sk.NgayKetThuc,
        tg.VaiTro
    FROM ThamGiaSuKien tg
    INNER JOIN SuKien sk ON tg.MaSK = sk.MaSK
    INNER JOIN ThanhVien tv ON tg.MSV = tv.MSV
    WHERE tg.MSV = p_MSV
    ORDER BY sk.NgayBatDau DESC, sk.MaSK ASC;
END$$
DELIMITER ;
