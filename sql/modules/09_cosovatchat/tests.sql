-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chạy trên kết nối riêng sau run_all.sql; không chạy trong transaction đang mở.
-- Fixture T_TRUONG_* tự tạo (không phụ thuộc seed_master), cố ý dùng INSERT thường:
-- nếu trùng sẽ dừng, không sửa dữ liệu sẵn có.
-- Handler bắt đúng MYSQL_ERRNO mong đợi (1644 = SIGNAL của thủ tục/trigger, 3819 = CHECK,
-- 1451 = FK chặn xóa); mọi lỗi khác làm test thất bại. Cuối cùng ROLLBACK toàn bộ.
-- Các ca sai luôn kiểm tra thêm dữ liệu KHÔNG bị đổi, để chắc lỗi đến từ đúng quy tắc.
DROP PROCEDURE IF EXISTS sp_TestTruong09;
DELIMITER $$
CREATE PROCEDURE sp_TestTruong09()
BEGIN
    DECLARE v_error INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO BanChuyenMon(MaBan, TenBan) VALUES('T_TRUONG_B1', 'Ban thử');
    INSERT INTO ThanhVien(MSV, HoVaTen, NgayGiaNhap, MaBan, ChucVu)
    VALUES('T_TRUONG_TV1', 'Thành viên thử 1', '2026-01-01', 'T_TRUONG_B1', NULL),
          ('T_TRUONG_TV2', 'Thành viên thử 2', '2026-01-01', 'T_TRUONG_B1', NULL);

    -- Thêm hợp lệ: cắt khoảng trắng, khả dụng mặc định = tổng, chưa có người quản lý
    CALL sp_ThemThietBi('T_TRUONG_TB1', '  Máy chiếu thử  ', 4, NULL, NULL);
    IF NOT COALESCE(((SELECT TenThietBi = 'Máy chiếu thử' AND TongSoLuong = 4 AND SoLuongKhaDung = 4 AND MSVQuanLy IS NULL FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm thiết bị mặc định';
    END IF;
    CALL sp_ThemThietBi('T_TRUONG_TB2', 'Loa thử', 5, 3, 'T_TRUONG_TV1');
    IF NOT COALESCE(((SELECT SoLuongKhaDung = 3 AND MSVQuanLy = 'T_TRUONG_TV1' AND TenNguoiQuanLy = 'Thành viên thử 1' FROM vw_DanhSachThietBi WHERE MaThietBi = 'T_TRUONG_TB2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view có người quản lý';
    END IF;
    IF NOT COALESCE(((SELECT SoLuongDangMuon FROM vw_DanhSachThietBi WHERE MaThietBi = 'T_TRUONG_TB2') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view số đang mượn';
    END IF;

    -- Thêm sai
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_TB1', 'Trùng mã', 1, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã thiết bị trùng';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trùng mã không làm thêm dòng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi(' ', 'Tên', 1, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi(REPEAT('X', 21), 'Tên', 1, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã quá 20 ký tự';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_BAD', ' ', 1, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tên trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_BAD', 'Tên', 0, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tổng bằng 0';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_BAD', 'Tên', 3, 4, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: khả dụng lớn hơn tổng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_BAD', 'Tên', 3, -1, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: khả dụng âm';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemThietBi('T_TRUONG_BAD', 'Tên', 3, NULL, 'T_TRUONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: người quản lý không tồn tại';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_BAD') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ca sai không để lại dữ liệu';
    END IF;

    -- Gán / đổi / bỏ người quản lý
    CALL sp_GanNguoiQuanLy('T_TRUONG_TB1', 'T_TRUONG_TV2');
    IF NOT COALESCE(((SELECT MSVQuanLy FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 'T_TRUONG_TV2'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: gán người quản lý';
    END IF;
    CALL sp_GanNguoiQuanLy('T_TRUONG_TB1', 'T_TRUONG_TV1');
    IF NOT COALESCE(((SELECT MSVQuanLy FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 'T_TRUONG_TV1'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi người quản lý';
    END IF;
    CALL sp_GanNguoiQuanLy('T_TRUONG_TB1', '');
    IF NOT COALESCE(((SELECT MSVQuanLy IS NULL FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: bỏ người quản lý';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_GanNguoiQuanLy('T_TRUONG_NONE', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: gán cho thiết bị không có';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_GanNguoiQuanLy('T_TRUONG_TB1', 'T_TRUONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: gán người không tồn tại';
    END IF;
    IF NOT COALESCE(((SELECT MSVQuanLy IS NULL FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: gán sai không đổi dữ liệu';
    END IF;

    -- Cập nhật: TB2 tổng 5, khả dụng 3 (đang mượn 2)
    CALL sp_CapNhatThietBi('T_TRUONG_TB2', 'Loa đổi tên', 6);
    IF NOT COALESCE(((SELECT TenThietBi = 'Loa đổi tên' AND TongSoLuong = 6 AND SoLuongKhaDung = 4 FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tăng tổng, khả dụng tăng theo';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_CapNhatThietBi('T_TRUONG_TB2', 'Loa', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: giảm tổng dưới số đang mượn';
    END IF;
    IF NOT COALESCE(((SELECT TongSoLuong = 6 AND SoLuongKhaDung = 4 FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: giảm sai không đổi dữ liệu';
    END IF;
    CALL sp_CapNhatThietBi('T_TRUONG_TB2', 'Loa', 2);
    IF NOT COALESCE(((SELECT TongSoLuong = 2 AND SoLuongKhaDung = 0 FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: giảm tổng bằng số đang mượn';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_CapNhatThietBi('T_TRUONG_NONE', 'Tên', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa thiết bị không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_CapNhatThietBi('T_TRUONG_TB2', ' ', 2);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa tên trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_CapNhatThietBi('T_TRUONG_TB2', 'Loa', 0);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa tổng bằng 0';
    END IF;

    -- Hàm kiểm tra khả dụng (TB1: tổng 4, khả dụng 4; TB2: khả dụng 0)
    IF NOT COALESCE((fn_KiemTraKhaDung('T_TRUONG_TB1', 4) = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đủ đúng bằng khả dụng';
    END IF;
    IF NOT COALESCE((fn_KiemTraKhaDung('T_TRUONG_TB1', 5) = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: vượt khả dụng';
    END IF;
    IF NOT COALESCE((fn_KiemTraKhaDung('T_TRUONG_TB1', 0) = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: số lượng 0';
    END IF;
    IF NOT COALESCE((fn_KiemTraKhaDung('T_TRUONG_NONE', 1) = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thiết bị không tồn tại';
    END IF;
    IF NOT COALESCE((fn_KiemTraKhaDung('T_TRUONG_TB2', 1) = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: hết khả dụng';
    END IF;
    IF NOT COALESCE((fn_KiemTraKhaDung(NULL, 1) = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã NULL';
    END IF;

    -- Tra cứu
    IF NOT COALESCE(((SELECT COUNT(*) FROM vw_DanhSachThietBi WHERE MaThietBi LIKE 'T\_TRUONG\_TB%') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view liệt kê thiết bị thử';
    END IF;
    CALL sp_TimThietBi('T_TRUONG_TB');
    CALL sp_TimThietBi('Máy chiếu thử');
    CALL sp_TimThietBi('');

    -- Ràng buộc của schema
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE CoSoVatChat SET SoLuongKhaDung = TongSoLuong + 1 WHERE MaThietBi = 'T_TRUONG_TB1';
    END;
    IF NOT COALESCE((v_error = 3819), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: CHECK khả dụng không vượt tổng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        DELETE FROM ThanhVien WHERE MSV = 'T_TRUONG_TV1';
    END;
    IF NOT COALESCE((v_error = 1451), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: FK chặn xóa người đang quản lý thiết bị';
    END IF;

    ROLLBACK;
    SELECT 'PASS module 09: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestTruong09();
DROP PROCEDURE IF EXISTS sp_TestTruong09;
