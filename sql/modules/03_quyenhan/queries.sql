-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

DROP PROCEDURE IF EXISTS sp_ThemQuyenHan;
DELIMITER $$
CREATE PROCEDURE sp_ThemQuyenHan(IN p_MaQuyenHan VARCHAR(255), IN p_TenQuyenHan TEXT, IN p_MoTaChiTiet LONGTEXT)
BEGIN
    IF p_MaQuyenHan IS NULL OR CHAR_LENGTH(TRIM(p_MaQuyenHan)) = 0 OR CHAR_LENGTH(TRIM(p_MaQuyenHan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã quyền hạn không hợp lệ';
    END IF;
    IF p_TenQuyenHan IS NULL OR CHAR_LENGTH(TRIM(p_TenQuyenHan)) = 0 OR CHAR_LENGTH(TRIM(p_TenQuyenHan)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên quyền hạn không hợp lệ';
    END IF;
    IF OCTET_LENGTH(TRIM(p_MoTaChiTiet)) > 65535 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mô tả vượt dung lượng TEXT';
    END IF;
    INSERT INTO QuyenHan(MaQuyenHan, TenQuyenHan, MoTaChiTiet)
    VALUES(TRIM(p_MaQuyenHan), TRIM(p_TenQuyenHan), NULLIF(TRIM(p_MoTaChiTiet), ''));
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_SuaQuyenHan;
DELIMITER $$
CREATE PROCEDURE sp_SuaQuyenHan(IN p_MaQuyenHan VARCHAR(255), IN p_TenQuyenHan TEXT, IN p_MoTaChiTiet LONGTEXT)
BEGIN
    IF p_MaQuyenHan IS NULL OR CHAR_LENGTH(TRIM(p_MaQuyenHan)) = 0 OR CHAR_LENGTH(TRIM(p_MaQuyenHan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã quyền hạn không hợp lệ';
    END IF;
    IF p_TenQuyenHan IS NULL OR CHAR_LENGTH(TRIM(p_TenQuyenHan)) = 0 OR CHAR_LENGTH(TRIM(p_TenQuyenHan)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên quyền hạn không hợp lệ';
    END IF;
    IF OCTET_LENGTH(TRIM(p_MoTaChiTiet)) > 65535 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mô tả vượt dung lượng TEXT';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quyền hạn không tồn tại';
    END IF;
    UPDATE QuyenHan SET TenQuyenHan = TRIM(p_TenQuyenHan), MoTaChiTiet = NULLIF(TRIM(p_MoTaChiTiet), '')
    WHERE MaQuyenHan = TRIM(p_MaQuyenHan);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_XoaQuyenHan;
DELIMITER $$
CREATE PROCEDURE sp_XoaQuyenHan(IN p_MaQuyenHan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quyền hạn không tồn tại';
    END IF;
    -- FK ON DELETE CASCADE tự thu hồi quyền ở ThanhVien_QuyenHan.
    DELETE FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_CapQuyen;
DELIMITER $$
CREATE PROCEDURE sp_CapQuyen(IN p_MSV VARCHAR(255), IN p_MaQuyenHan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quyền hạn không tồn tại';
    END IF;
    -- PK ghép là hàng rào cuối cùng chống cấp trùng, kể cả khi có hai kết nối đồng thời.
    INSERT INTO ThanhVien_QuyenHan(MSV, MaQuyenHan) VALUES(TRIM(p_MSV), TRIM(p_MaQuyenHan));
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_ThuHoiQuyen;
DELIMITER $$
CREATE PROCEDURE sp_ThuHoiQuyen(IN p_MSV VARCHAR(255), IN p_MaQuyenHan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quyền hạn không tồn tại';
    END IF;
    DELETE FROM ThanhVien_QuyenHan WHERE MSV = TRIM(p_MSV) AND MaQuyenHan = TRIM(p_MaQuyenHan);
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên chưa được cấp quyền này';
    END IF;
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_QuyenCuaThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_QuyenCuaThanhVien(IN p_MSV VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT q.MaQuyenHan, q.TenQuyenHan, q.MoTaChiTiet
    FROM QuyenHan q JOIN ThanhVien_QuyenHan tvq ON tvq.MaQuyenHan = q.MaQuyenHan
    WHERE tvq.MSV = TRIM(p_MSV) ORDER BY q.MaQuyenHan;
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_ThanhVienCoQuyen;
DELIMITER $$
CREATE PROCEDURE sp_ThanhVienCoQuyen(IN p_MaQuyenHan VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = TRIM(p_MaQuyenHan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quyền hạn không tồn tại';
    END IF;
    SELECT tv.MSV, tv.HoVaTen, tv.MaBan, tv.ChucVu
    FROM ThanhVien tv JOIN ThanhVien_QuyenHan tvq ON tvq.MSV = tv.MSV
    WHERE tvq.MaQuyenHan = TRIM(p_MaQuyenHan) ORDER BY tv.MSV;
END$$
DELIMITER ;
