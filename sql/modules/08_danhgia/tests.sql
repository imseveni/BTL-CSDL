-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chạy trên kết nối riêng sau run_all.sql; không chạy trong transaction đang mở.
-- Fixture T_DONG_* cố ý dùng INSERT thường: nếu trùng sẽ dừng, không sửa dữ liệu sẵn có.
-- Handler bắt đúng MYSQL_ERRNO mong đợi; mọi lỗi khác làm test thất bại.
DROP PROCEDURE IF EXISTS sp_TestDong08;
DELIMITER $$
CREATE PROCEDURE sp_TestDong08()
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
    CALL sp_ThemDanhGia('T_DONG_D1', 'T_DONG_LOAI', 'Biểu dương', 'Đóng góp', '2026-03-01', 'T_DONG_TV1');
    CALL sp_ThemDanhGia('T_DONG_D2', 'T_DONG_LOAI', 'Giấy khen', 'Đóng góp', '2026-04-01', 'T_DONG_TV1');
    CALL sp_ThemDanhGia('T_DONG_D3', 'T_DONG_LOAI', 'Giấy khen', 'Đóng góp', '2026-04-01', 'T_DONG_TV2');
    IF NOT COALESCE(((SELECT SoDanhGia = 3 AND SoThanhVien = 2 FROM vw_ThongKeDanhGia WHERE LoaiDanhGia = 'T_DONG_LOAI') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thống kê số bản ghi và thành viên phân biệt';
    END IF;
    CALL sp_SuaDanhGia('T_DONG_D1', 'T_DONG_LOAI2', 'Nhắc nhở', 'Đi muộn', '2026-05-01', 'T_DONG_TV2');
    CALL sp_SuaDanhGia('T_DONG_D1', 'T_DONG_LOAI2', 'Nhắc nhở', 'Đi muộn', '2026-05-01', 'T_DONG_TV2');
    IF NOT COALESCE(((SELECT MSV = 'T_DONG_TV2' AND HinhThuc = 'Nhắc nhở' AND LyDo = 'Đi muộn' AND NgayQuyetDinh = '2026-05-01' FROM DanhGia WHERE MaGhiNhan = 'T_DONG_D1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa đủ thông tin đánh giá';
    END IF;
    IF NOT COALESCE(((SELECT SoDanhGia FROM vw_ThongKeDanhGia WHERE LoaiDanhGia = 'T_DONG_LOAI') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thống kê cập nhật sau sửa loại';
    END IF;
    CALL sp_LichSuDanhGia('T_DONG_TV2');
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_D1', 'Khen thưởng', 'Giấy khen', 'Lý do', '2026-01-01', 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1062), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã đánh giá trùng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_BAD', 'Khen thưởng', 'Giấy khen', 'Lý do', '2026-01-01', 'T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: MSV không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_BAD', '', 'Giấy khen', 'Lý do', '2026-01-01', 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: loại trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_BAD', 'Khen thưởng', '', 'Lý do', '2026-01-01', 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: hình thức trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_BAD', 'Khen thưởng', 'Giấy khen', ' ', '2026-01-01', 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lý do trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemDanhGia('T_DONG_BAD', 'Khen thưởng', 'Giấy khen', 'Lý do', NULL, 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ngày trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_SuaDanhGia('T_DONG_NONE', 'Khen thưởng', 'Giấy khen', 'Lý do', '2026-01-01', 'T_DONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa đánh giá không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_SuaDanhGia('T_DONG_D1', 'Khen thưởng', 'Giấy khen', 'Lý do', '2026-01-01', 'T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa sang MSV không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_LichSuDanhGia('T_DONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lịch sử MSV không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        DELETE FROM ThanhVien WHERE MSV = 'T_DONG_TV2';
    END;
    IF NOT COALESCE((v_error = 1451), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: FK chặn xóa thành viên có đánh giá';
    END IF;

    ROLLBACK;
    SELECT 'PASS module 08: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestDong08();
DROP PROCEDURE IF EXISTS sp_TestDong08;
