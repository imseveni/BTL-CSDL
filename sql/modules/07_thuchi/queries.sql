-- Module 07 (Quản lý Thu – Chi) – truy vấn / view / stored procedure / trigger. Phụ trách: Minh
USE QuanLyCauLacBo;

-- =====================================================================
-- 1. VIEWS: Xem chi tiết giao dịch & Thống kê thu – chi theo từng sự kiện
-- =====================================================================

-- 1.1. View chi tiết giao dịch kèm danh sách phương thức thanh toán, tên thành viên và tên sự kiện
DROP VIEW IF EXISTS vw_ChiTietGiaoDich;
CREATE VIEW vw_ChiTietGiaoDich AS
SELECT
    tc.MaGD,
    tc.LoaiGD,
    tc.MucDich,
    tc.SoTien,
    tc.ThoiGian,
    tc.MinhChung,
    tc.MSV,
    tv.HoVaTen AS HoTenThanhVien,
    tc.MaSK,
    sk.TenSK,
    GROUP_CONCAT(pt.PhuongThucThanhToan ORDER BY pt.PhuongThucThanhToan SEPARATOR ', ') AS CacPhuongThucThanhToan
FROM ThuChi tc
LEFT JOIN ThanhVien tv ON tc.MSV = tv.MSV
LEFT JOIN SuKien sk ON tc.MaSK = sk.MaSK
LEFT JOIN ThuChi_PhuongThucThanhToan pt ON tc.MaGD = pt.MaGD
GROUP BY
    tc.MaGD, tc.LoaiGD, tc.MucDich, tc.SoTien,
    tc.ThoiGian, tc.MinhChung, tc.MSV, tv.HoVaTen, tc.MaSK, sk.TenSK;

-- 1.2. View thống kê tổng thu, tổng chi và chênh lệch theo từng sự kiện
DROP VIEW IF EXISTS vw_ThuChiTheoSuKien;
CREATE VIEW vw_ThuChiTheoSuKien AS
SELECT
    sk.MaSK,
    sk.TenSK,
    COUNT(tc.MaGD) AS SoGiaoDich,
    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Thu' THEN tc.SoTien ELSE 0 END), 0) AS TongThu,
    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Chi' THEN tc.SoTien ELSE 0 END), 0) AS TongChi,
    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Thu' THEN tc.SoTien ELSE 0 END), 0)
        - COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Chi' THEN tc.SoTien ELSE 0 END), 0) AS ChenhLech
FROM SuKien sk
LEFT JOIN ThuChi tc ON sk.MaSK = tc.MaSK
GROUP BY sk.MaSK, sk.TenSK;

-- =====================================================================
-- 2. TRIGGERS: Kiểm tra ràng buộc dữ liệu Thu – Chi
-- =====================================================================
DROP TRIGGER IF EXISTS trg_ThuChi_BeforeInsert;
DELIMITER $$
CREATE TRIGGER trg_ThuChi_BeforeInsert
BEFORE INSERT ON ThuChi
FOR EACH ROW
BEGIN
    IF TRIM(NEW.MaGD) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã giao dịch không được để trống';
    END IF;
    IF NEW.LoaiGD NOT IN ('Thu', 'Chi') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loại giao dịch chỉ được phép là Thu hoặc Chi';
    END IF;
    IF NEW.SoTien <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số tiền giao dịch phải lớn hơn 0';
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_ThuChi_BeforeUpdate;
DELIMITER $$
CREATE TRIGGER trg_ThuChi_BeforeUpdate
BEFORE UPDATE ON ThuChi
FOR EACH ROW
BEGIN
    IF NEW.LoaiGD NOT IN ('Thu', 'Chi') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loại giao dịch chỉ được phép là Thu hoặc Chi';
    END IF;
    IF NEW.SoTien <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số tiền giao dịch phải lớn hơn 0';
    END IF;
END$$
DELIMITER ;

-- =====================================================================
-- 3. STORED PROCEDURES: Nghiệp vụ Quản lý Thu – Chi
-- =====================================================================

-- 3.1. Thêm mới giao dịch Thu – Chi
DROP PROCEDURE IF EXISTS sp_ThemGiaoDich;
DELIMITER $$
CREATE PROCEDURE sp_ThemGiaoDich(
    IN p_MaGD VARCHAR(20),
    IN p_LoaiGD VARCHAR(20),
    IN p_MucDich TEXT,
    IN p_SoTien DECIMAL(15,2),
    IN p_ThoiGian DATETIME,
    IN p_MinhChung TEXT,
    IN p_MSV VARCHAR(20),
    IN p_MaSK VARCHAR(20)
)
BEGIN
    IF EXISTS (SELECT 1 FROM ThuChi WHERE MaGD = p_MaGD) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã giao dịch đã tồn tại';
    END IF;

    IF p_MSV IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = p_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sinh viên thực hiện giao dịch không tồn tại';
    END IF;

    IF p_MaSK IS NOT NULL AND NOT EXISTS (SELECT 1 FROM SuKien WHERE MaSK = p_MaSK) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sự kiện liên kết không tồn tại';
    END IF;

    INSERT INTO ThuChi (MaGD, LoaiGD, MucDich, SoTien, ThoiGian, MinhChung, MSV, MaSK)
    VALUES (p_MaGD, p_LoaiGD, p_MucDich, p_SoTien, p_ThoiGian, p_MinhChung, p_MSV, p_MaSK);
END$$
DELIMITER ;

-- 3.2. Thêm phương thức thanh toán cho một giao dịch
DROP PROCEDURE IF EXISTS sp_ThemPhuongThucThanhToan;
DELIMITER $$
CREATE PROCEDURE sp_ThemPhuongThucThanhToan(
    IN p_MaGD VARCHAR(20),
    IN p_PhuongThuc VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThuChi WHERE MaGD = p_MaGD) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy mã giao dịch';
    END IF;

    IF TRIM(p_PhuongThuc) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phương thức thanh toán không được để trống';
    END IF;

    INSERT INTO ThuChi_PhuongThucThanhToan (MaGD, PhuongThucThanhToan)
    VALUES (p_MaGD, p_PhuongThuc);
END$$
DELIMITER ;

-- 3.3. Cập nhật thông tin giao dịch
DROP PROCEDURE IF EXISTS sp_CapNhatGiaoDich;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatGiaoDich(
    IN p_MaGD VARCHAR(20),
    IN p_LoaiGD VARCHAR(20),
    IN p_MucDich TEXT,
    IN p_SoTien DECIMAL(15,2),
    IN p_ThoiGian DATETIME,
    IN p_MinhChung TEXT,
    IN p_MSV VARCHAR(20),
    IN p_MaSK VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThuChi WHERE MaGD = p_MaGD) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy giao dịch cần cập nhật';
    END IF;

    UPDATE ThuChi
    SET LoaiGD = p_LoaiGD,
        MucDich = p_MucDich,
        SoTien = p_SoTien,
        ThoiGian = p_ThoiGian,
        MinhChung = p_MinhChung,
        MSV = p_MSV,
        MaSK = p_MaSK
    WHERE MaGD = p_MaGD;
END$$
DELIMITER ;

-- 3.4. Xóa giao dịch (các dòng ThuChi_PhuongThucThanhToan tự động xóa theo ON DELETE CASCADE)
DROP PROCEDURE IF EXISTS sp_XoaGiaoDich;
DELIMITER $$
CREATE PROCEDURE sp_XoaGiaoDich(
    IN p_MaGD VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ThuChi WHERE MaGD = p_MaGD) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy giao dịch cần xóa';
    END IF;

    DELETE FROM ThuChi WHERE MaGD = p_MaGD;
END$$
DELIMITER ;

-- 3.5. Tra cứu giao dịch theo thành viên thực hiện (MSV)
DROP PROCEDURE IF EXISTS sp_TraCuuGiaoDichTheoThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_TraCuuGiaoDichTheoThanhVien(
    IN p_MSV VARCHAR(20)
)
BEGIN
    SELECT *
    FROM vw_ChiTietGiaoDich
    WHERE MSV = p_MSV
    ORDER BY ThoiGian DESC, MaGD ASC;
END$$
DELIMITER ;

-- 3.6. Tra cứu giao dịch theo sự kiện (MaSK)
DROP PROCEDURE IF EXISTS sp_TraCuuGiaoDichTheoSuKien;
DELIMITER $$
CREATE PROCEDURE sp_TraCuuGiaoDichTheoSuKien(
    IN p_MaSK VARCHAR(20)
)
BEGIN
    SELECT *
    FROM vw_ChiTietGiaoDich
    WHERE MaSK = p_MaSK
    ORDER BY ThoiGian DESC, MaGD ASC;
END$$
DELIMITER ;

-- 3.7. Thống kê thu – chi theo từng sự kiện
DROP PROCEDURE IF EXISTS sp_ThongKeThuChiTheoSuKien;
DELIMITER $$
CREATE PROCEDURE sp_ThongKeThuChiTheoSuKien()
BEGIN
    SELECT
        MaSK,
        TenSK,
        SoGiaoDich,
        TongThu,
        TongChi,
        ChenhLech
    FROM vw_ThuChiTheoSuKien
    ORDER BY MaSK ASC;
END$$
DELIMITER ;
