-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Chạy trên kết nối riêng sau run_all.sql; không chạy trong transaction đang mở.
-- Fixture T_TRUONG_* tự tạo (không phụ thuộc seed_master), cố ý dùng INSERT thường:
-- nếu trùng sẽ dừng, không sửa dữ liệu sẵn có.
-- Handler bắt đúng MYSQL_ERRNO mong đợi (1644 = SIGNAL của thủ tục/trigger, 3819 = CHECK,
-- 1451 = FK chặn xóa); mọi lỗi khác làm test thất bại. Cuối cùng ROLLBACK toàn bộ.
-- Các ca sai luôn kiểm tra thêm dữ liệu KHÔNG bị đổi, để chắc lỗi đến từ đúng quy tắc.
DROP PROCEDURE IF EXISTS sp_TestTruong10;
DELIMITER $$
CREATE PROCEDURE sp_TestTruong10()
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
    INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
    VALUES('T_TRUONG_TB1', 'Thiết bị thử 1', 5, 5, NULL),
          ('T_TRUONG_TB2', 'Thiết bị thử 2', 3, 3, NULL),
          ('T_TRUONG_TB3', 'Thiết bị thử 3', 2, 2, NULL);

    -- 1) Tạo phiếu và thêm thiết bị: chưa duyệt thì CHƯA trừ khả dụng
    CALL sp_TaoPhieuMuon('T_TRUONG_P1', '2026-01-10', '2026-01-17', '  Họp thử  ', 'T_TRUONG_TV1');
    IF NOT COALESCE(((SELECT TrangThaiDuyet = 'ChoDuyet' AND MucDichMuon = 'Họp thử' AND MSVNguoiMuon = 'T_TRUONG_TV1' FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tạo phiếu mặc định ChoDuyet, cắt khoảng trắng';
    END IF;
    CALL sp_ThemChiTietMuon('T_TRUONG_P1', 'T_TRUONG_TB1', 3);
    CALL sp_ThemChiTietMuon('T_TRUONG_P1', 'T_TRUONG_TB2', 1);
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 5 AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 3), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm chi tiết chưa trừ khả dụng';
    END IF;

    -- 2) Tạo phiếu sai
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon('T_TRUONG_PX', '2026-01-10', '2026-01-09', 'x', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ngày hẹn trả trước ngày mượn';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon('T_TRUONG_PX', NULL, '2026-01-09', 'x', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thiếu ngày mượn';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon('T_TRUONG_P1', '2026-01-10', '2026-01-17', 'x', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã phiếu trùng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon('T_TRUONG_PX', '2026-01-10', '2026-01-17', 'x', 'T_TRUONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: người mượn không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon(' ', '2026-01-10', '2026-01-17', 'x', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mã phiếu trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TaoPhieuMuon('T_TRUONG_PX', '2026-01-10', '2026-01-17', 'x', ' ');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: MSV trống';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_PX') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: phiếu sai không được tạo';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        INSERT INTO PhieuMuon(MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon) VALUES('T_TRUONG_PX', '2026-01-10', '2026-01-17', 'x', 'DaDuyet', 'T_TRUONG_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: phiếu mới không được tạo sẵn ở trạng thái đã duyệt';
    END IF;

    -- 3) Thêm thiết bị sai
    CALL sp_TaoPhieuMuon('T_TRUONG_P2', '2026-01-10', '2026-01-17', NULL, 'T_TRUONG_TV1');
    IF NOT COALESCE(((SELECT MucDichMuon IS NULL FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mục đích để trống thành NULL';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P1', 'T_TRUONG_TB1', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm trùng thiết bị trong phiếu';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P2', 'T_TRUONG_TB1', 6);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: mượn vượt khả dụng khi thêm';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P2', 'T_TRUONG_TB1', 0);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: số lượng mượn bằng 0';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P2', 'T_TRUONG_TB1', -1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: số lượng mượn âm';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P2', 'T_TRUONG_NONE', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thiết bị không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_NONE', 'T_TRUONG_TB1', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: phiếu không tồn tại';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P2') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm sai không để lại chi tiết';
    END IF;

    -- 4) Duyệt phiếu
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_DuyetPhieuMuon('T_TRUONG_P2');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu chưa có thiết bị';
    END IF;
    IF NOT COALESCE(((SELECT TrangThaiDuyet FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P2') = 'ChoDuyet'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu rỗng không đổi trạng thái';
    END IF;
    CALL sp_DuyetPhieuMuon('T_TRUONG_P1');
    IF NOT COALESCE(((SELECT TrangThaiDuyet FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P1') = 'DaDuyet'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt đổi trạng thái';
    END IF;
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 2 AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt trừ khả dụng mọi thiết bị trong phiếu';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_DuyetPhieuMuon('T_TRUONG_P1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt lại phiếu đã duyệt';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TuChoiPhieuMuon('T_TRUONG_P1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: từ chối phiếu đã duyệt';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE PhieuMuon SET TrangThaiDuyet = 'ChoDuyet' WHERE MaPhieuMuon = 'T_TRUONG_P1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi trạng thái phiếu đã duyệt về chờ';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_DuyetPhieuMuon('T_TRUONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu không tồn tại';
    END IF;
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 2 AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt sai không đổi khả dụng';
    END IF;

    -- 5) Phiếu đã duyệt không sửa được
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P1', 'T_TRUONG_TB3', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm thiết bị vào phiếu đã duyệt';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        INSERT INTO ChiTietMuon(MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe) VALUES('T_TRUONG_P1', 'T_TRUONG_TB3', 1, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: INSERT trực tiếp vào phiếu đã duyệt';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE ChiTietMuon SET SoLuongMuon = 1 WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa số lượng phiếu đã duyệt';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE ChiTietMuon SET MaThietBi = 'T_TRUONG_TB3' WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi thiết bị của dòng chi tiết';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        DELETE FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa chi tiết đang mượn chưa trả';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        DELETE FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa phiếu còn thiết bị chưa trả';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE PhieuMuon SET NgayMuon = '2026-01-01' WHERE MaPhieuMuon = 'T_TRUONG_P1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi ngày mượn phiếu đã duyệt';
    END IF;

    -- 6) Hai phiếu chờ cùng mượn vừa đủ lúc tạo; duyệt phiếu đầu được, phiếu sau bị chặn
    CALL sp_ThemChiTietMuon('T_TRUONG_P2', 'T_TRUONG_TB1', 2);
    CALL sp_TaoPhieuMuon('T_TRUONG_P3', '2026-01-10', '2026-01-17', 'Phiếu 3', 'T_TRUONG_TV1');
    CALL sp_ThemChiTietMuon('T_TRUONG_P3', 'T_TRUONG_TB1', 2);
    CALL sp_DuyetPhieuMuon('T_TRUONG_P2');
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu đầu: khả dụng về 0';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_DuyetPhieuMuon('T_TRUONG_P3');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu thứ hai khi đã hết';
    END IF;
    IF NOT COALESCE(((SELECT TrangThaiDuyet FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P3') = 'ChoDuyet' AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt bị chặn: không đổi trạng thái, không trừ âm';
    END IF;
    CALL sp_TuChoiPhieuMuon('T_TRUONG_P3');
    IF NOT COALESCE(((SELECT TrangThaiDuyet FROM PhieuMuon WHERE MaPhieuMuon = 'T_TRUONG_P3') = 'TuChoi' AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: từ chối không đổi khả dụng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_DuyetPhieuMuon('T_TRUONG_P3');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: duyệt phiếu đã từ chối';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TuChoiPhieuMuon('T_TRUONG_P3');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: từ chối lại phiếu đã từ chối';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_ThemChiTietMuon('T_TRUONG_P3', 'T_TRUONG_TB3', 1);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm thiết bị vào phiếu đã từ chối';
    END IF;

    -- 7) Phiếu chờ: sửa số lượng và xóa dòng được phép (TB3 khả dụng 2)
    CALL sp_TaoPhieuMuon('T_TRUONG_P4', '2026-01-10', '2026-01-17', 'Phiếu 4', 'T_TRUONG_TV1');
    CALL sp_ThemChiTietMuon('T_TRUONG_P4', 'T_TRUONG_TB3', 1);
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE ChiTietMuon SET SoLuongMuon = 3 WHERE MaPhieuMuon = 'T_TRUONG_P4' AND MaThietBi = 'T_TRUONG_TB3';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa số lượng vượt khả dụng';
    END IF;
    UPDATE ChiTietMuon SET SoLuongMuon = 2 WHERE MaPhieuMuon = 'T_TRUONG_P4' AND MaThietBi = 'T_TRUONG_TB3';
    IF NOT COALESCE(((SELECT SoLuongMuon FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P4' AND MaThietBi = 'T_TRUONG_TB3') = 2 AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB3') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: sửa số lượng hợp lệ, chưa trừ khả dụng';
    END IF;
    DELETE FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P4';
    IF NOT COALESCE(((SELECT COUNT(*) FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P4') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa chi tiết của phiếu chờ';
    END IF;

    -- 8) Ghi nhận trả
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraThietBi('T_TRUONG_P1', 'T_TRUONG_TB1', '2026-01-09');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: ngày trả trước ngày mượn';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraThietBi('T_TRUONG_P3', 'T_TRUONG_TB1', '2026-01-12');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả thiết bị của phiếu đã từ chối';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraThietBi('T_TRUONG_P1', 'T_TRUONG_TB3', '2026-01-12');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả thiết bị không có trong phiếu';
    END IF;
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 0 AND (SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 2), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả sai không đổi khả dụng';
    END IF;
    CALL sp_TraThietBi('T_TRUONG_P1', 'T_TRUONG_TB1', '2026-01-20');
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 3), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả một thiết bị: cộng lại khả dụng';
    END IF;
    IF NOT COALESCE(((SELECT SoNgayTre = 3 AND TinhTrang = 'TraMuon' FROM vw_ThietBiTraMuon WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view trả muộn: trễ 3 ngày';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM vw_ThietBiChuaTra WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1') = 0), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đã trả thì không còn trong view chưa trả';
    END IF;
    IF NOT COALESCE(((SELECT COUNT(*) FROM vw_ThietBiChuaTra WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thiết bị chưa trả còn trong view';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraThietBi('T_TRUONG_P1', 'T_TRUONG_TB1', '2026-01-21');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả lần hai cùng thiết bị';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        UPDATE ChiTietMuon SET NgayTraThucTe = NULL WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1';
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: hoàn tác ngày trả';
    END IF;
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 3), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả lần hai không cộng trùng';
    END IF;
    CALL sp_TraTatCaThietBi('T_TRUONG_P1', '2026-01-15');
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB2') = 3), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả tất cả: cộng lại thiết bị còn lại';
    END IF;
    IF NOT COALESCE(((SELECT NgayTraThucTe = '2026-01-20' FROM ChiTietMuon WHERE MaPhieuMuon = 'T_TRUONG_P1' AND MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả tất cả không ghi đè dòng đã trả';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraTatCaThietBi('T_TRUONG_P1', '2026-01-16');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả tất cả khi không còn gì';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_TraTatCaThietBi('T_TRUONG_NONE', '2026-01-16');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: trả tất cả phiếu không tồn tại';
    END IF;

    -- 9) Nhất quán kho và theo dõi quá hạn (P2 đã duyệt, TB1 x2 chưa trả, hẹn trả 2026-01-17)
    IF NOT COALESCE(((SELECT SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1') = 5 - COALESCE((SELECT SUM(c.SoLuongMuon) FROM ChiTietMuon c JOIN PhieuMuon p ON p.MaPhieuMuon = c.MaPhieuMuon WHERE c.MaThietBi = 'T_TRUONG_TB1' AND p.TrangThaiDuyet = 'DaDuyet' AND c.NgayTraThucTe IS NULL), 0)), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: khả dụng = tổng - đang mượn';
    END IF;
    IF NOT COALESCE(((SELECT TinhTrang = 'QuaHan' AND SoNgayTre > 0 AND SoLuongMuon = 2 FROM vw_ThietBiChuaTra WHERE MaPhieuMuon = 'T_TRUONG_P2' AND MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view chưa trả đánh dấu quá hạn';
    END IF;
    IF NOT COALESCE(((SELECT HoVaTen = 'Thành viên thử 1' FROM vw_ThietBiChuaTra WHERE MaPhieuMuon = 'T_TRUONG_P2' AND MaThietBi = 'T_TRUONG_TB1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: view chưa trả có tên người mượn';
    END IF;

    -- 10) Lịch sử mượn và ràng buộc tham chiếu
    IF NOT COALESCE(((SELECT COUNT(*) FROM PhieuMuon WHERE MSVNguoiMuon = 'T_TRUONG_TV1') = 4), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thành viên có 4 phiếu thử';
    END IF;
    CALL sp_LichSuMuon('T_TRUONG_TV1');
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_LichSuMuon('T_TRUONG_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lịch sử thành viên không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        CALL sp_LichSuMuon(' ');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lịch sử MSV trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        DELETE FROM ThanhVien WHERE MSV = 'T_TRUONG_TV1';
    END;
    IF NOT COALESCE((v_error = 1451), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: FK chặn xóa thành viên đã lập phiếu';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        DELETE FROM CoSoVatChat WHERE MaThietBi = 'T_TRUONG_TB1';
    END;
    IF NOT COALESCE((v_error = 1451), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: FK chặn xóa thiết bị đã được mượn';
    END IF;

    ROLLBACK;
    SELECT 'PASS module 10: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestTruong10();
DROP PROCEDURE IF EXISTS sp_TestTruong10;
