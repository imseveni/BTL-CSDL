-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

DROP VIEW IF EXISTS vw_BanChuyenMonThongKe;
CREATE VIEW vw_BanChuyenMonThongKe AS
SELECT b.MaBan, b.TenBan, COUNT(tv.MSV) AS SoThanhVien
FROM BanChuyenMon b LEFT JOIN ThanhVien tv ON tv.MaBan = b.MaBan
GROUP BY b.MaBan, b.TenBan;

DROP PROCEDURE IF EXISTS sp_ThemBan;
DELIMITER $$
CREATE PROCEDURE sp_ThemBan(IN p_MaBan VARCHAR(255), IN p_TenBan TEXT)
BEGIN
    IF p_MaBan IS NULL OR CHAR_LENGTH(TRIM(p_MaBan)) = 0 OR CHAR_LENGTH(TRIM(p_MaBan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã ban không hợp lệ';
    END IF;
    IF p_TenBan IS NULL OR CHAR_LENGTH(TRIM(p_TenBan)) = 0 OR CHAR_LENGTH(TRIM(p_TenBan)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên ban không hợp lệ';
    END IF;
    INSERT INTO BanChuyenMon(MaBan, TenBan) VALUES(TRIM(p_MaBan), TRIM(p_TenBan));
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_SuaBan;
DELIMITER $$
CREATE PROCEDURE sp_SuaBan(IN p_MaBan VARCHAR(255), IN p_TenBan TEXT)
BEGIN
    IF p_MaBan IS NULL OR CHAR_LENGTH(TRIM(p_MaBan)) = 0 OR CHAR_LENGTH(TRIM(p_MaBan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã ban không hợp lệ';
    END IF;
    IF p_TenBan IS NULL OR CHAR_LENGTH(TRIM(p_TenBan)) = 0 OR CHAR_LENGTH(TRIM(p_TenBan)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên ban không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = TRIM(p_MaBan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ban chuyên môn không tồn tại';
    END IF;
    UPDATE BanChuyenMon SET TenBan = TRIM(p_TenBan) WHERE MaBan = TRIM(p_MaBan);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_XoaBan;
DELIMITER $$
CREATE PROCEDURE sp_XoaBan(IN p_MaBan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = TRIM(p_MaBan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ban chuyên môn không tồn tại';
    END IF;
    IF EXISTS (SELECT 1 FROM ThanhVien WHERE MaBan = TRIM(p_MaBan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa ban còn thành viên; hãy chuyển thành viên sang ban khác trước';
    END IF;
    DELETE FROM BanChuyenMon WHERE MaBan = TRIM(p_MaBan);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_ChuyenBan;
DELIMITER $$
CREATE PROCEDURE sp_ChuyenBan(IN p_MSV VARCHAR(255), IN p_MaBan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = TRIM(p_MaBan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ban chuyên môn không tồn tại';
    END IF;
    UPDATE ThanhVien SET MaBan = TRIM(p_MaBan) WHERE MSV = TRIM(p_MSV);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_DoiChucVu;
DELIMITER $$
CREATE PROCEDURE sp_DoiChucVu(IN p_MSV VARCHAR(255), IN p_ChucVu TEXT)
BEGIN
    IF p_ChucVu IS NULL OR CHAR_LENGTH(TRIM(p_ChucVu)) = 0 OR CHAR_LENGTH(TRIM(p_ChucVu)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chức vụ không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    UPDATE ThanhVien SET ChucVu = TRIM(p_ChucVu) WHERE MSV = TRIM(p_MSV);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_ThanhVienTheoBan;
DELIMITER $$
CREATE PROCEDURE sp_ThanhVienTheoBan(IN p_MaBan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = TRIM(p_MaBan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ban chuyên môn không tồn tại';
    END IF;
    SELECT MSV, HoVaTen, ChucVu, TrangThaiHD FROM ThanhVien
    WHERE MaBan = TRIM(p_MaBan) ORDER BY MSV;
END$$
DELIMITER ;
