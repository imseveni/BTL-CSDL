-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chưa chốt danh mục LoaiDanhGia với Tú: giữ VARCHAR như schema, không áp enum/trigger.
-- Chuẩn hóa khoảng trắng lúc ghi; hiển thị cả nhóm NULL/trống của dữ liệu cũ.
DROP VIEW IF EXISTS vw_ThongKeDanhGia;
CREATE VIEW vw_ThongKeDanhGia AS
SELECT COALESCE(NULLIF(TRIM(LoaiDanhGia), ''), '(Chưa phân loại)') AS LoaiDanhGia,
       COUNT(*) AS SoDanhGia, COUNT(DISTINCT MSV) AS SoThanhVien
FROM DanhGia
GROUP BY COALESCE(NULLIF(TRIM(LoaiDanhGia), ''), '(Chưa phân loại)');

DROP PROCEDURE IF EXISTS sp_ThemDanhGia;
DELIMITER $$
CREATE PROCEDURE sp_ThemDanhGia(IN p_MaGhiNhan VARCHAR(255), IN p_LoaiDanhGia TEXT, IN p_HinhThuc TEXT,
    IN p_LyDo LONGTEXT, IN p_NgayQuyetDinh DATE, IN p_MSV VARCHAR(255))
BEGIN
    IF p_MaGhiNhan IS NULL OR CHAR_LENGTH(TRIM(p_MaGhiNhan)) = 0 OR CHAR_LENGTH(TRIM(p_MaGhiNhan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã ghi nhận không hợp lệ';
    END IF;
    IF p_MSV IS NULL OR CHAR_LENGTH(TRIM(p_MSV)) = 0 OR CHAR_LENGTH(TRIM(p_MSV)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV không hợp lệ';
    END IF;
    IF p_LoaiDanhGia IS NULL OR CHAR_LENGTH(TRIM(p_LoaiDanhGia)) = 0 OR CHAR_LENGTH(TRIM(p_LoaiDanhGia)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loại đánh giá không hợp lệ';
    END IF;
    IF p_HinhThuc IS NULL OR CHAR_LENGTH(TRIM(p_HinhThuc)) = 0 OR CHAR_LENGTH(TRIM(p_HinhThuc)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hình thức không hợp lệ';
    END IF;
    IF p_LyDo IS NULL OR CHAR_LENGTH(TRIM(p_LyDo)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lý do không hợp lệ';
    END IF;
    IF OCTET_LENGTH(TRIM(p_LyDo)) > 65535 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lý do vượt dung lượng TEXT';
    END IF;
    IF p_NgayQuyetDinh IS NULL OR p_NgayQuyetDinh < '1000-01-01' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày quyết định không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    INSERT INTO DanhGia(MaGhiNhan, LoaiDanhGia, HinhThuc, LyDo, NgayQuyetDinh, MSV)
    VALUES(TRIM(p_MaGhiNhan), TRIM(p_LoaiDanhGia), TRIM(p_HinhThuc), TRIM(p_LyDo), p_NgayQuyetDinh, TRIM(p_MSV));
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_SuaDanhGia;
DELIMITER $$
CREATE PROCEDURE sp_SuaDanhGia(IN p_MaGhiNhan VARCHAR(255), IN p_LoaiDanhGia TEXT, IN p_HinhThuc TEXT,
    IN p_LyDo LONGTEXT, IN p_NgayQuyetDinh DATE, IN p_MSV VARCHAR(255))
BEGIN
    IF p_MaGhiNhan IS NULL OR CHAR_LENGTH(TRIM(p_MaGhiNhan)) = 0 OR CHAR_LENGTH(TRIM(p_MaGhiNhan)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã ghi nhận không hợp lệ';
    END IF;
    IF p_MSV IS NULL OR CHAR_LENGTH(TRIM(p_MSV)) = 0 OR CHAR_LENGTH(TRIM(p_MSV)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV không hợp lệ';
    END IF;
    IF p_LoaiDanhGia IS NULL OR CHAR_LENGTH(TRIM(p_LoaiDanhGia)) = 0 OR CHAR_LENGTH(TRIM(p_LoaiDanhGia)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loại đánh giá không hợp lệ';
    END IF;
    IF p_HinhThuc IS NULL OR CHAR_LENGTH(TRIM(p_HinhThuc)) = 0 OR CHAR_LENGTH(TRIM(p_HinhThuc)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hình thức không hợp lệ';
    END IF;
    IF p_LyDo IS NULL OR CHAR_LENGTH(TRIM(p_LyDo)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lý do không hợp lệ';
    END IF;
    IF OCTET_LENGTH(TRIM(p_LyDo)) > 65535 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lý do vượt dung lượng TEXT';
    END IF;
    IF p_NgayQuyetDinh IS NULL OR p_NgayQuyetDinh < '1000-01-01' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày quyết định không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM DanhGia WHERE MaGhiNhan = TRIM(p_MaGhiNhan)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bản ghi đánh giá không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    UPDATE DanhGia SET LoaiDanhGia = TRIM(p_LoaiDanhGia), HinhThuc = TRIM(p_HinhThuc),
        LyDo = TRIM(p_LyDo), NgayQuyetDinh = p_NgayQuyetDinh, MSV = TRIM(p_MSV)
    WHERE MaGhiNhan = TRIM(p_MaGhiNhan);
END$$
DELIMITER ;
DROP PROCEDURE IF EXISTS sp_LichSuDanhGia;
DELIMITER $$
CREATE PROCEDURE sp_LichSuDanhGia(IN p_MSV VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT MaGhiNhan, LoaiDanhGia, HinhThuc, LyDo, NgayQuyetDinh, MSV FROM DanhGia
    WHERE MSV = TRIM(p_MSV) ORDER BY NgayQuyetDinh DESC, MaGhiNhan;
END$$
DELIMITER ;
