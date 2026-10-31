-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chạy trên kết nối riêng sau run_all.sql; không chạy trong transaction đang mở.
-- Fixture T_DONG_* cố ý dùng INSERT thường: nếu trùng sẽ dừng, không sửa dữ liệu sẵn có.
-- Handler bắt đúng MYSQL_ERRNO mong đợi; mọi lỗi khác làm test thất bại.
DROP PROCEDURE IF EXISTS sp_TestDong03;
DELIMITER $$
CREATE PROCEDURE sp_TestDong03()
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
    CALL sp_ThemQuyenHan('T_DONG_Q1', 'Quyền thử', NULL);
    CALL sp_SuaQuyenHan('T_DONG_Q1', 'Quyền sửa', 'Mô tả');
    CALL sp_SuaQuyenHan('T_DONG_Q1', 'Quyền sửa', 'Mô tả');
    IF NOT COALESCE(((SELECT TenQuyenHan = 'Quyền sửa' AND MoTaChiTiet = 'Mô tả' FROM QuyenHan WHERE MaQuyenHan = 'T_DONG_Q1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa quyền';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemQuyenHan('T_DONG_Q1', 'Trùng', NULL);
    END;
    IF NOT COALESCE((v_error = 1062), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trùng mã quyền';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemQuyenHan('T_DONG_BAD', '', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tên quyền trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_SuaQuyenHan('T_DONG_NONE', 'Tên', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa quyền không tồn tại';
    END IF;
    CALL sp_CapQuyen('T_DONG_TV1', 'T_DONG_Q1');
    CALL sp_CapQuyen('T_DONG_TV2', 'T_DONG_Q1');
    IF NOT COALESCE(((SELECT COUNT(*) FROM ThanhVien_QuyenHan WHERE MaQuyenHan = 'T_DONG_Q1') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cấp quyền cho nhiều thành viên';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapQuyen('T_DONG_TV1', 'T_DONG_Q1');
    END;
    IF NOT COALESCE((v_error = 1062), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cấp quyền trùng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapQuyen('T_DONG_NONE', 'T_DONG_Q1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cấp cho MSV không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapQuyen('T_DONG_TV1', 'T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cấp quyền không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_QuyenCuaThanhVien('T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tra cứu MSV không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThanhVienCoQuyen('T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tra cứu quyền không tồn tại';
    END IF;
    CALL sp_QuyenCuaThanhVien('T_DONG_TV1');
    CALL sp_ThanhVienCoQuyen('T_DONG_Q1');
    CALL sp_ThuHoiQuyen('T_DONG_TV1', 'T_DONG_Q1');
    IF NOT COALESCE((NOT EXISTS (SELECT 1 FROM ThanhVien_QuyenHan WHERE MSV = 'T_DONG_TV1' AND MaQuyenHan = 'T_DONG_Q1')), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thu hồi quyền';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThuHoiQuyen('T_DONG_TV1', 'T_DONG_Q1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thu hồi quyền chưa được cấp';
    END IF;
    CALL sp_QuyenCuaThanhVien('T_DONG_TV1');
    CALL sp_XoaQuyenHan('T_DONG_Q1');
    IF NOT COALESCE((NOT EXISTS (SELECT 1 FROM ThanhVien_QuyenHan WHERE MaQuyenHan = 'T_DONG_Q1')), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa quyền phải cascade các lần cấp';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_XoaQuyenHan('T_DONG_Q1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa quyền không tồn tại';
    END IF;
    ROLLBACK;
    SELECT 'PASS module 03: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestDong03();
DROP PROCEDURE IF EXISTS sp_TestDong03;
