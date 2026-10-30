-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- QUY TẮC (docs/DECISIONS.md và HUONG_DAN_THUC_HIEN.md, mục B5):
--  1. SoLuongMuon <= SoLuongKhaDung: kiểm tra khi thêm chi tiết VÀ khi duyệt phiếu.
--  2. Trừ SoLuongKhaDung lúc DUYỆT (ChoDuyet -> DaDuyet); cộng lại khi ghi NgayTraThucTe.
--  3. NgayTraThucTe >= NgayMuon của phiếu.
-- Vòng đời phiếu: ChoDuyet -> DaDuyet | TuChoi (đã xử lý thì không đổi lại).
-- Chỉ phiếu ChoDuyet mới thêm/sửa/xóa thiết bị; chỉ phiếu DaDuyet mới ghi nhận trả.
--
-- Thủ tục không mở transaction riêng: mỗi lệnh ghi (kể cả trigger kéo theo) là một câu lệnh nguyên tử,
-- lỗi thì cả câu lệnh rollback. Nhờ vậy chạy được bên trong transaction của tests.sql.
-- Khóa dòng thiết bị (FOR UPDATE) đặt trong trigger BEFORE UPDATE của PhieuMuon, nên hai phiếu duyệt
-- cùng lúc không lấy trùng số lượng.
-- Lưu ý: seed.sql chạy trước file này (run_all.sql) nên seed không bị trigger chặn; seed tự đồng bộ kho.

-- ===================== TRIGGER PhieuMuon =====================
DROP TRIGGER IF EXISTS trg_PhieuMuon_BI;
DROP TRIGGER IF EXISTS trg_PhieuMuon_BU;
DROP TRIGGER IF EXISTS trg_PhieuMuon_AU;
DROP TRIGGER IF EXISTS trg_PhieuMuon_BD;
DELIMITER $$
CREATE TRIGGER trg_PhieuMuon_BI BEFORE INSERT ON PhieuMuon
FOR EACH ROW
BEGIN
    IF NEW.NgayHenTra < NEW.NgayMuon THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày hẹn trả không được trước ngày mượn';
    END IF;
    IF NEW.TrangThaiDuyet IS NULL THEN
        SET NEW.TrangThaiDuyet = 'ChoDuyet';
    ELSEIF NEW.TrangThaiDuyet <> 'ChoDuyet' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mới phải ở trạng thái ChoDuyet';
    END IF;
END$$

CREATE TRIGGER trg_PhieuMuon_BU BEFORE UPDATE ON PhieuMuon
FOR EACH ROW
BEGIN
    DECLARE v_Khoa INT DEFAULT 0;
    IF NEW.NgayHenTra < NEW.NgayMuon THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày hẹn trả không được trước ngày mượn';
    END IF;
    IF OLD.TrangThaiDuyet <> 'ChoDuyet' AND NOT (NEW.NgayMuon <=> OLD.NgayMuon) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu đã được xử lý, không được đổi ngày mượn';
    END IF;
    IF NEW.TrangThaiDuyet <> OLD.TrangThaiDuyet THEN
        IF OLD.TrangThaiDuyet <> 'ChoDuyet' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu đã được xử lý, không được đổi trạng thái duyệt';
        END IF;
        IF NEW.TrangThaiDuyet = 'DaDuyet' THEN
            IF NOT EXISTS (SELECT 1 FROM ChiTietMuon WHERE MaPhieuMuon = OLD.MaPhieuMuon) THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu chưa có thiết bị nào, không thể duyệt';
            END IF;
            -- Khóa các dòng thiết bị của phiếu rồi mới kiểm tra số lượng.
            SELECT COUNT(*) INTO v_Khoa FROM CoSoVatChat
            WHERE MaThietBi IN (SELECT MaThietBi FROM ChiTietMuon WHERE MaPhieuMuon = OLD.MaPhieuMuon)
            FOR UPDATE;
            IF EXISTS (SELECT 1 FROM ChiTietMuon c JOIN CoSoVatChat t ON t.MaThietBi = c.MaThietBi
                       WHERE c.MaPhieuMuon = OLD.MaPhieuMuon AND c.SoLuongMuon > t.SoLuongKhaDung) THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng mượn vượt quá số lượng khả dụng, không thể duyệt';
            END IF;
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_PhieuMuon_AU AFTER UPDATE ON PhieuMuon
FOR EACH ROW
BEGIN
    IF OLD.TrangThaiDuyet = 'ChoDuyet' AND NEW.TrangThaiDuyet = 'DaDuyet' THEN
        UPDATE CoSoVatChat t JOIN ChiTietMuon c ON c.MaThietBi = t.MaThietBi
        SET t.SoLuongKhaDung = t.SoLuongKhaDung - c.SoLuongMuon
        WHERE c.MaPhieuMuon = NEW.MaPhieuMuon;
    END IF;
END$$

-- Xóa dây chuyền qua khóa ngoại không kích hoạt trigger của bảng con, nên chặn ngay ở phiếu.
CREATE TRIGGER trg_PhieuMuon_BD BEFORE DELETE ON PhieuMuon
FOR EACH ROW
BEGIN
    IF OLD.TrangThaiDuyet = 'DaDuyet'
       AND EXISTS (SELECT 1 FROM ChiTietMuon WHERE MaPhieuMuon = OLD.MaPhieuMuon AND NgayTraThucTe IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu còn thiết bị chưa trả, không được xóa';
    END IF;
END$$
DELIMITER ;

-- ===================== TRIGGER ChiTietMuon =====================
DROP TRIGGER IF EXISTS trg_ChiTietMuon_BI;
DROP TRIGGER IF EXISTS trg_ChiTietMuon_BU;
DROP TRIGGER IF EXISTS trg_ChiTietMuon_AU;
DROP TRIGGER IF EXISTS trg_ChiTietMuon_BD;
DELIMITER $$
CREATE TRIGGER trg_ChiTietMuon_BI BEFORE INSERT ON ChiTietMuon
FOR EACH ROW
BEGIN
    DECLARE v_TrangThai VARCHAR(20) DEFAULT NULL;
    DECLARE v_KhaDung INT DEFAULT NULL;
    DECLARE v_Msg VARCHAR(200);
    SELECT TrangThaiDuyet INTO v_TrangThai FROM PhieuMuon WHERE MaPhieuMuon = NEW.MaPhieuMuon;
    IF v_TrangThai IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mượn không tồn tại';
    END IF;
    IF v_TrangThai <> 'ChoDuyet' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ được thêm thiết bị vào phiếu đang ở trạng thái ChoDuyet';
    END IF;
    IF NEW.NgayTraThucTe IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không được ghi ngày trả khi mới thêm thiết bị vào phiếu';
    END IF;
    IF NEW.SoLuongMuon IS NULL OR NEW.SoLuongMuon <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng mượn phải lớn hơn 0';
    END IF;
    SELECT SoLuongKhaDung INTO v_KhaDung FROM CoSoVatChat WHERE MaThietBi = NEW.MaThietBi;
    IF v_KhaDung IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị không tồn tại';
    END IF;
    IF NEW.SoLuongMuon > v_KhaDung THEN
        SET v_Msg = CONCAT('Số lượng mượn vượt quá số lượng khả dụng (hiện còn ', v_KhaDung, ')');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_Msg;
    END IF;
END$$

CREATE TRIGGER trg_ChiTietMuon_BU BEFORE UPDATE ON ChiTietMuon
FOR EACH ROW
BEGIN
    DECLARE v_TrangThai VARCHAR(20) DEFAULT NULL;
    DECLARE v_NgayMuon DATE DEFAULT NULL;
    DECLARE v_KhaDung INT DEFAULT NULL;
    DECLARE v_Msg VARCHAR(200);
    IF NEW.MaPhieuMuon <> OLD.MaPhieuMuon OR NEW.MaThietBi <> OLD.MaThietBi THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không được đổi phiếu hoặc thiết bị của dòng chi tiết';
    END IF;
    SELECT TrangThaiDuyet, NgayMuon INTO v_TrangThai, v_NgayMuon
    FROM PhieuMuon WHERE MaPhieuMuon = OLD.MaPhieuMuon;
    IF NEW.SoLuongMuon <> OLD.SoLuongMuon THEN
        IF v_TrangThai <> 'ChoDuyet' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ được sửa số lượng khi phiếu đang ở trạng thái ChoDuyet';
        END IF;
        IF NEW.SoLuongMuon <= 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng mượn phải lớn hơn 0';
        END IF;
        SELECT SoLuongKhaDung INTO v_KhaDung FROM CoSoVatChat WHERE MaThietBi = OLD.MaThietBi;
        IF NEW.SoLuongMuon > v_KhaDung THEN
            SET v_Msg = CONCAT('Số lượng mượn vượt quá số lượng khả dụng (hiện còn ', v_KhaDung, ')');
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_Msg;
        END IF;
    END IF;
    IF NOT (NEW.NgayTraThucTe <=> OLD.NgayTraThucTe) THEN
        IF OLD.NgayTraThucTe IS NOT NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị này đã được ghi nhận trả, không được sửa';
        END IF;
        IF v_TrangThai <> 'DaDuyet' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu chưa được duyệt, không thể ghi nhận trả';
        END IF;
        IF NEW.NgayTraThucTe < v_NgayMuon THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày trả thực tế không được trước ngày mượn';
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_ChiTietMuon_AU AFTER UPDATE ON ChiTietMuon
FOR EACH ROW
BEGIN
    IF OLD.NgayTraThucTe IS NULL AND NEW.NgayTraThucTe IS NOT NULL THEN
        UPDATE CoSoVatChat SET SoLuongKhaDung = SoLuongKhaDung + NEW.SoLuongMuon
        WHERE MaThietBi = NEW.MaThietBi;
    END IF;
END$$

CREATE TRIGGER trg_ChiTietMuon_BD BEFORE DELETE ON ChiTietMuon
FOR EACH ROW
BEGIN
    DECLARE v_TrangThai VARCHAR(20) DEFAULT NULL;
    SELECT TrangThaiDuyet INTO v_TrangThai FROM PhieuMuon WHERE MaPhieuMuon = OLD.MaPhieuMuon;
    IF v_TrangThai = 'DaDuyet' AND OLD.NgayTraThucTe IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị đang được mượn, chưa trả nên không được xóa khỏi phiếu';
    END IF;
END$$
DELIMITER ;

-- ===================== THỦ TỤC =====================
DROP PROCEDURE IF EXISTS sp_TaoPhieuMuon;
DELIMITER $$
CREATE PROCEDURE sp_TaoPhieuMuon(IN p_MaPhieu VARCHAR(255), IN p_NgayMuon DATE, IN p_NgayHenTra DATE,
    IN p_MucDich TEXT, IN p_MSV VARCHAR(255))
BEGIN
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    IF p_MSV IS NULL OR CHAR_LENGTH(TRIM(p_MSV)) = 0 OR CHAR_LENGTH(TRIM(p_MSV)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV người mượn không hợp lệ';
    END IF;
    IF p_NgayMuon IS NULL OR p_NgayHenTra IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phải nhập ngày mượn và ngày hẹn trả';
    END IF;
    IF p_NgayHenTra < p_NgayMuon THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày hẹn trả không được trước ngày mượn';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên mượn không tồn tại';
    END IF;
    IF EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn đã tồn tại';
    END IF;
    INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon)
    VALUES(TRIM(p_MaPhieu), p_NgayMuon, p_NgayHenTra, NULLIF(TRIM(p_MucDich), ''), 'ChoDuyet', TRIM(p_MSV));
END$$
DELIMITER ;

-- Thêm một thiết bị vào phiếu (gọi nhiều lần cho nhiều thiết bị).
DROP PROCEDURE IF EXISTS sp_ThemChiTietMuon;
DELIMITER $$
CREATE PROCEDURE sp_ThemChiTietMuon(IN p_MaPhieu VARCHAR(255), IN p_MaThietBi VARCHAR(255), IN p_SoLuong INT)
BEGIN
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    IF p_MaThietBi IS NULL OR CHAR_LENGTH(TRIM(p_MaThietBi)) = 0 OR CHAR_LENGTH(TRIM(p_MaThietBi)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mượn không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = TRIM(p_MaThietBi)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị không tồn tại';
    END IF;
    IF EXISTS (SELECT 1 FROM ChiTietMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu) AND MaThietBi = TRIM(p_MaThietBi)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị đã có trong phiếu';
    END IF;
    -- Trigger trg_ChiTietMuon_BI kiểm tra trạng thái phiếu, số lượng > 0 và không vượt khả dụng.
    INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe)
    VALUES(TRIM(p_MaPhieu), TRIM(p_MaThietBi), p_SoLuong, NULL);
END$$
DELIMITER ;

-- Duyệt phiếu: trigger kiểm tra lại số lượng (có khóa dòng thiết bị) rồi trừ SoLuongKhaDung.
DROP PROCEDURE IF EXISTS sp_DuyetPhieuMuon;
DELIMITER $$
CREATE PROCEDURE sp_DuyetPhieuMuon(IN p_MaPhieu VARCHAR(255))
BEGIN
    DECLARE v_TrangThai VARCHAR(20) DEFAULT NULL;
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    SELECT TrangThaiDuyet INTO v_TrangThai FROM PhieuMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu);
    IF v_TrangThai IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mượn không tồn tại';
    END IF;
    IF v_TrangThai <> 'ChoDuyet' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ duyệt được phiếu đang ở trạng thái ChoDuyet';
    END IF;
    UPDATE PhieuMuon SET TrangThaiDuyet = 'DaDuyet' WHERE MaPhieuMuon = TRIM(p_MaPhieu);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_TuChoiPhieuMuon;
DELIMITER $$
CREATE PROCEDURE sp_TuChoiPhieuMuon(IN p_MaPhieu VARCHAR(255))
BEGIN
    DECLARE v_TrangThai VARCHAR(20) DEFAULT NULL;
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    SELECT TrangThaiDuyet INTO v_TrangThai FROM PhieuMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu);
    IF v_TrangThai IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mượn không tồn tại';
    END IF;
    IF v_TrangThai <> 'ChoDuyet' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ từ chối được phiếu đang ở trạng thái ChoDuyet';
    END IF;
    UPDATE PhieuMuon SET TrangThaiDuyet = 'TuChoi' WHERE MaPhieuMuon = TRIM(p_MaPhieu);
END$$
DELIMITER ;

-- Ghi nhận trả một thiết bị; p_NgayTra NULL -> hôm nay. Trigger cộng lại SoLuongKhaDung.
DROP PROCEDURE IF EXISTS sp_TraThietBi;
DELIMITER $$
CREATE PROCEDURE sp_TraThietBi(IN p_MaPhieu VARCHAR(255), IN p_MaThietBi VARCHAR(255), IN p_NgayTra DATE)
BEGIN
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    IF p_MaThietBi IS NULL OR CHAR_LENGTH(TRIM(p_MaThietBi)) = 0 OR CHAR_LENGTH(TRIM(p_MaThietBi)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã thiết bị không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ChiTietMuon
                   WHERE MaPhieuMuon = TRIM(p_MaPhieu) AND MaThietBi = TRIM(p_MaThietBi)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thiết bị này không có trong phiếu';
    END IF;
    UPDATE ChiTietMuon SET NgayTraThucTe = COALESCE(p_NgayTra, CURDATE())
    WHERE MaPhieuMuon = TRIM(p_MaPhieu) AND MaThietBi = TRIM(p_MaThietBi);
END$$
DELIMITER ;

-- Trả tất cả thiết bị còn lại của phiếu bằng MỘT câu UPDATE (nguyên tử: lỗi ở dòng nào thì không dòng nào đổi).
DROP PROCEDURE IF EXISTS sp_TraTatCaThietBi;
DELIMITER $$
CREATE PROCEDURE sp_TraTatCaThietBi(IN p_MaPhieu VARCHAR(255), IN p_NgayTra DATE)
BEGIN
    IF p_MaPhieu IS NULL OR CHAR_LENGTH(TRIM(p_MaPhieu)) = 0 OR CHAR_LENGTH(TRIM(p_MaPhieu)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã phiếu mượn không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu mượn không tồn tại';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ChiTietMuon WHERE MaPhieuMuon = TRIM(p_MaPhieu) AND NgayTraThucTe IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phiếu không còn thiết bị nào chưa trả';
    END IF;
    UPDATE ChiTietMuon SET NgayTraThucTe = COALESCE(p_NgayTra, CURDATE())
    WHERE MaPhieuMuon = TRIM(p_MaPhieu) AND NgayTraThucTe IS NULL;
END$$
DELIMITER ;

-- Lịch sử mượn của một thành viên (mỗi dòng = một thiết bị trong một phiếu), mới nhất trước.
DROP PROCEDURE IF EXISTS sp_LichSuMuon;
DELIMITER $$
CREATE PROCEDURE sp_LichSuMuon(IN p_MSV VARCHAR(255))
BEGIN
    IF p_MSV IS NULL OR CHAR_LENGTH(TRIM(p_MSV)) = 0 OR CHAR_LENGTH(TRIM(p_MSV)) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MSV không hợp lệ';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = TRIM(p_MSV)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT p.MaPhieuMuon, p.NgayMuon, p.NgayHenTra, p.MucDichMuon, p.TrangThaiDuyet,
           c.MaThietBi, t.TenThietBi, c.SoLuongMuon, c.NgayTraThucTe
    FROM PhieuMuon p
    LEFT JOIN ChiTietMuon c ON c.MaPhieuMuon = p.MaPhieuMuon
    LEFT JOIN CoSoVatChat t ON t.MaThietBi = c.MaThietBi
    WHERE p.MSVNguoiMuon = TRIM(p_MSV)
    ORDER BY p.NgayMuon DESC, p.MaPhieuMuon, c.MaThietBi;
END$$
DELIMITER ;

-- ===================== VIEW theo dõi =====================
-- Đã duyệt nhưng chưa trả; TinhTrang = QuaHan nếu đã quá ngày hẹn trả.
DROP VIEW IF EXISTS vw_ThietBiChuaTra;
CREATE VIEW vw_ThietBiChuaTra AS
SELECT p.MaPhieuMuon, p.MSVNguoiMuon, tv.HoVaTen, c.MaThietBi, t.TenThietBi, c.SoLuongMuon,
       p.NgayMuon, p.NgayHenTra, CAST(NULL AS DATE) AS NgayTraThucTe,
       GREATEST(DATEDIFF(CURDATE(), p.NgayHenTra), 0) AS SoNgayTre,
       CASE WHEN p.NgayHenTra < CURDATE() THEN 'QuaHan' ELSE 'DangMuon' END AS TinhTrang
FROM ChiTietMuon c
JOIN PhieuMuon p   ON p.MaPhieuMuon = c.MaPhieuMuon
JOIN CoSoVatChat t ON t.MaThietBi = c.MaThietBi
JOIN ThanhVien tv  ON tv.MSV = p.MSVNguoiMuon
WHERE p.TrangThaiDuyet = 'DaDuyet' AND c.NgayTraThucTe IS NULL;

-- Đã trả nhưng muộn hơn ngày hẹn.
DROP VIEW IF EXISTS vw_ThietBiTraMuon;
CREATE VIEW vw_ThietBiTraMuon AS
SELECT p.MaPhieuMuon, p.MSVNguoiMuon, tv.HoVaTen, c.MaThietBi, t.TenThietBi, c.SoLuongMuon,
       p.NgayMuon, p.NgayHenTra, c.NgayTraThucTe,
       DATEDIFF(c.NgayTraThucTe, p.NgayHenTra) AS SoNgayTre,
       'TraMuon' AS TinhTrang
FROM ChiTietMuon c
JOIN PhieuMuon p   ON p.MaPhieuMuon = c.MaPhieuMuon
JOIN CoSoVatChat t ON t.MaThietBi = c.MaThietBi
JOIN ThanhVien tv  ON tv.MSV = p.MSVNguoiMuon
WHERE c.NgayTraThucTe IS NOT NULL AND c.NgayTraThucTe > p.NgayHenTra;
