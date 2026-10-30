-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Quy ước: SoLuongKhaDung do module 10 (trigger) tự cập nhật khi duyệt/trả phiếu.
-- Module này chỉ cho sửa TongSoLuong; khả dụng được điều chỉnh theo số đang mượn.
-- Thủ tục không mở transaction riêng (mỗi lệnh ghi là một câu lệnh nguyên tử) để chạy được
-- bên trong transaction của tests.sql rồi ROLLBACK.
-- Tham số khai báo rộng rồi kiểm tra độ dài, để báo lỗi rõ thay vì MySQL tự cắt/ báo lỗi chung.

DROP VIEW IF EXISTS vw_DanhSachThietBi;
CREATE VIEW vw_DanhSachThietBi AS
SELECT t.MaThietBi, t.TenThietBi, t.TongSoLuong, t.SoLuongKhaDung,
       (t.TongSoLuong - t.SoLuongKhaDung) AS SoLuongDangMuon,
       t.MSVQuanLy, tv.HoVaTen AS TenNguoiQuanLy
FROM CoSoVatChat t
LEFT JOIN ThanhVien tv ON tv.MSV = t.MSVQuanLy;

-- Trả 1 nếu thiết bị tồn tại và còn đủ p_SoLuong (> 0) để cho mượn, ngược lại 0.
DROP FUNCTION IF EXISTS fn_KiemTraKhaDung;
DELIMITER $$
CREATE FUNCTION fn_KiemTraKhaDung(p_MaThietBi VARCHAR(255), p_SoLuong INT)
RETURNS TINYINT(1)
READS SQL DATA
BEGIN
    DECLARE v_KhaDung INT DEFAULT NULL;
    IF p_MaThietBi IS NULL OR p_SoLuong IS NULL OR p_SoLuong <= 0 THEN
        RETURN 0;
    END IF;
    SELECT SoLuongKhaDung INTO v_KhaDung FROM CoSoVatChat WHERE MaThietBi = TRIM(p_MaThietBi);
    IF v_KhaDung IS NULL THEN
        RETURN 0;
    END IF;
    RETURN IF(p_SoLuong <= v_KhaDung, 1, 0);
END$$
DELIMITER ;

-- Thêm thiết bị. p_KhaDung NULL -> bằng p_Tong; p_MSVQuanLy NULL/trống -> chưa có người quản lý.
DROP PROCEDURE IF EXISTS sp_ThemThietBi;
DELIMITER $$
CREATE PROCEDURE sp_ThemThietBi(IN p_Ma VARCHAR(255), IN p_Ten VARCHAR(255), IN p_Tong INT,
    IN p_KhaDung INT, IN p_MSVQuanLy VARCHAR(255))
BEGIN
    DECLARE v_KhaDung INT;
    DECLARE v_MSV VARCHAR(255);
    IF p_Ma IS NULL OR CHAR_LENGTH(TRIM(p_Ma)) = 0 OR CHAR_LENGTH(TRIM(p_Ma)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị không hợp lệ';
    END IF;
    IF p_Ten IS NULL OR CHAR_LENGTH(TRIM(p_Ten)) = 0 OR CHAR_LENGTH(TRIM(p_Ten)) > 150 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên thiết bị không hợp lệ';
    END IF;
    IF p_Tong IS NULL OR p_Tong <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tổng số lượng phải lớn hơn 0';
    END IF;
    SET v_KhaDung = COALESCE(p_KhaDung, p_Tong);
    IF v_KhaDung < 0 OR v_KhaDung > p_Tong THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng khả dụng phải từ 0 đến tổng số lượng';
    END IF;
    SET v_MSV = NULLIF(TRIM(p_MSVQuanLy), '');
    IF v_MSV IS NOT NULL AND CHAR_LENGTH(v_MSV) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV người quản lý không hợp lệ';
    END IF;
    IF v_MSV IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên quản lý không tồn tại';
    END IF;
    IF EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = TRIM(p_Ma)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị đã tồn tại';
    END IF;
    INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
    VALUES(TRIM(p_Ma), TRIM(p_Ten), p_Tong, v_KhaDung, v_MSV);
END$$
DELIMITER ;

-- Sửa tên và tổng số lượng. Số đang mượn = tổng cũ - khả dụng cũ; tổng mới không được nhỏ hơn số này.
-- Khả dụng mới = tổng mới - số đang mượn (giữ khớp với phiếu mượn). Mã và người quản lý giữ nguyên.
DROP PROCEDURE IF EXISTS sp_CapNhatThietBi;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatThietBi(IN p_Ma VARCHAR(255), IN p_Ten VARCHAR(255), IN p_Tong INT)
BEGIN
    DECLARE v_TongCu INT DEFAULT NULL;
    DECLARE v_KhaDungCu INT DEFAULT NULL;
    DECLARE v_DangMuon INT;
    IF p_Ma IS NULL OR CHAR_LENGTH(TRIM(p_Ma)) = 0 OR CHAR_LENGTH(TRIM(p_Ma)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị không hợp lệ';
    END IF;
    IF p_Ten IS NULL OR CHAR_LENGTH(TRIM(p_Ten)) = 0 OR CHAR_LENGTH(TRIM(p_Ten)) > 150 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên thiết bị không hợp lệ';
    END IF;
    IF p_Tong IS NULL OR p_Tong <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tổng số lượng phải lớn hơn 0';
    END IF;
    SELECT TongSoLuong, SoLuongKhaDung INTO v_TongCu, v_KhaDungCu
    FROM CoSoVatChat WHERE MaThietBi = TRIM(p_Ma) FOR UPDATE;
    IF v_TongCu IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị không tồn tại';
    END IF;
    SET v_DangMuon = v_TongCu - v_KhaDungCu;
    IF p_Tong < v_DangMuon THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tổng số lượng mới nhỏ hơn số lượng đang được mượn';
    END IF;
    UPDATE CoSoVatChat
    SET TenThietBi = TRIM(p_Ten), TongSoLuong = p_Tong, SoLuongKhaDung = p_Tong - v_DangMuon
    WHERE MaThietBi = TRIM(p_Ma);
END$$
DELIMITER ;

-- Gán/đổi người quản lý; p_MSV NULL hoặc trống -> bỏ người quản lý.
DROP PROCEDURE IF EXISTS sp_GanNguoiQuanLy;
DELIMITER $$
CREATE PROCEDURE sp_GanNguoiQuanLy(IN p_Ma VARCHAR(255), IN p_MSV VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    IF p_Ma IS NULL OR CHAR_LENGTH(TRIM(p_Ma)) = 0 OR CHAR_LENGTH(TRIM(p_Ma)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = TRIM(p_Ma)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị không tồn tại';
    END IF;
    SET v_MSV = NULLIF(TRIM(p_MSV), '');
    IF v_MSV IS NOT NULL AND CHAR_LENGTH(v_MSV) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV người quản lý không hợp lệ';
    END IF;
    IF v_MSV IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên quản lý không tồn tại';
    END IF;
    UPDATE CoSoVatChat SET MSVQuanLy = v_MSV WHERE MaThietBi = TRIM(p_Ma);
END$$
DELIMITER ;

-- Tra cứu theo mã hoặc tên (chứa từ khóa); từ khóa NULL/trống -> tất cả.
DROP PROCEDURE IF EXISTS sp_TimThietBi;
DELIMITER $$
CREATE PROCEDURE sp_TimThietBi(IN p_TuKhoa VARCHAR(255))
BEGIN
    SELECT * FROM vw_DanhSachThietBi
    WHERE p_TuKhoa IS NULL OR CHAR_LENGTH(TRIM(p_TuKhoa)) = 0
       OR MaThietBi LIKE CONCAT('%', TRIM(p_TuKhoa), '%')
       OR TenThietBi LIKE CONCAT('%', TRIM(p_TuKhoa), '%')
    ORDER BY MaThietBi;
END$$
DELIMITER ;
