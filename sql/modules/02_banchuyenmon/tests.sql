-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chạy trên kết nối riêng sau run_all.sql; không chạy trong transaction đang mở.
-- Fixture T_DONG_* cố ý dùng INSERT thường: nếu trùng sẽ dừng, không sửa dữ liệu sẵn có.
-- Handler bắt đúng MYSQL_ERRNO mong đợi; mọi lỗi khác làm test thất bại.
DROP PROCEDURE IF EXISTS sp_TestDong02;
DELIMITER $$
CREATE PROCEDURE sp_TestDong02()
BEGIN
    DECLARE v_error INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO BanChuyenMon(MaBan, TenBan) VALUES('T_DONG_B1', 'Ban thử 1'), ('T_DONG_B2', 'Ban thử 2');
    INSERT INTO ThanhVien(MSV, HoVaTen, NgayGiaNhap, MaBan, ChucVu)
    VALUES('T_DONG_TV1', 'Thành viên thử 1', '2026-01-01', 'T_DONG_B1', 'Thành viên'),
          ('T_DONG_TV2', 'Thành viên thử 2', '2026-01-01', 'T_DONG_B2', NULL);
    CALL sp_ThemBan('T_DONG_B3', 'Ban thử 3');
    CALL sp_SuaBan('T_DONG_B3', 'Ban đã sửa');
    CALL sp_SuaBan('T_DONG_B3', 'Ban đã sửa');
    IF NOT COALESCE(((SELECT TenBan FROM BanChuyenMon WHERE MaBan = 'T_DONG_B3') = 'Ban đã sửa'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa tên ban';
    END IF;
    IF NOT COALESCE(((SELECT SoThanhVien FROM vw_BanChuyenMonThongKe WHERE MaBan = 'T_DONG_B3') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ban rỗng vẫn có trong thống kê';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemBan('T_DONG_B3', 'Trùng');
    END;
    IF NOT COALESCE((v_error = 1062), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn mã ban trùng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemBan('T_DONG_BAD', '   ');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn tên trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemBan(REPEAT('x', 21), 'Ban');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn mã ban dài';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_SuaBan('T_DONG_NONE', 'Tên');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa ban không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_XoaBan('T_DONG_B1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn xóa ban có thành viên';
    END IF;
    CALL sp_ChuyenBan('T_DONG_TV1', 'T_DONG_B2');
    CALL sp_DoiChucVu('T_DONG_TV1', 'Trưởng ban');
    IF NOT COALESCE(((SELECT MaBan = 'T_DONG_B2' AND ChucVu = 'Trưởng ban' FROM ThanhVien WHERE MSV = 'T_DONG_TV1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chuyển ban và chức vụ';
    END IF;
    IF NOT COALESCE(((SELECT SoThanhVien FROM vw_BanChuyenMonThongKe WHERE MaBan = 'T_DONG_B2') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đếm thành viên';
    END IF;
    CALL sp_ThanhVienTheoBan('T_DONG_B2');
    CALL sp_ThanhVienTheoBan('T_DONG_B1');
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ChuyenBan('T_DONG_TV1', 'T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ban đích không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ChuyenBan('T_DONG_NONE', 'T_DONG_B1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: MSV chuyển ban không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_DoiChucVu('T_DONG_TV1', '');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chức vụ trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_DoiChucVu('T_DONG_NONE', 'Thành viên');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: MSV đổi chức vụ không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThanhVienTheoBan('T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tra cứu ban không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_XoaBan('T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa ban không tồn tại';
    END IF;
    CALL sp_XoaBan('T_DONG_B1');
    CALL sp_XoaBan('T_DONG_B3');
    IF NOT COALESCE((NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan IN ('T_DONG_B1','T_DONG_B3'))), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa ban rỗng';
    END IF;
    ROLLBACK;
    SELECT 'PASS module 02: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestDong02();
DROP PROCEDURE IF EXISTS sp_TestDong02;
