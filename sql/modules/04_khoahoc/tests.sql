-- Module 04 (Quản lý Khóa học) – ca thử (đúng + sai) để kiểm tra ràng buộc. Phụ trách: Chiến
USE QuanLyCauLacBo;

-- Chạy trên DB thử nghiệm sau run_all.sql. Fixture tự tạo và ROLLBACK.
-- Chỉ bắt đúng mã lỗi mong đợi; mọi lỗi khác làm bài test thất bại.
DROP PROCEDURE IF EXISTS sp_KhoaHocTest;
DELIMITER $$
CREATE PROCEDURE sp_KhoaHocTest()
BEGIN
    DECLARE rejected BOOLEAN DEFAULT FALSE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO BanChuyenMon VALUES ('KH_TEST_BAN', 'Ban test khoa hoc');
    INSERT INTO ThanhVien (MSV, HoVaTen, NgayGiaNhap, MaBan)
    VALUES ('KH_TEST_TV', 'Thanh vien test', '2026-10-01', 'KH_TEST_BAN');

    INSERT INTO KhoaHoc VALUES ('KH_TEST_01', 'Khoa hoc test', '2026-10-01', '2026-10-31');
    INSERT INTO KhoaHoc VALUES ('KH_TEST_02', 'Chua be giang', '2026-10-01', NULL);
    INSERT INTO ThamGiaKhoaHoc VALUES ('KH_TEST_TV', 'KH_TEST_01', 'Hoc vien', NULL);
    UPDATE ThamGiaKhoaHoc SET KetQuaDanhGia = 'Dat'
    WHERE MSV = 'KH_TEST_TV' AND MaKhoaHoc = 'KH_TEST_01';
    IF (SELECT COUNT(*) FROM vw_KhoaHocThanhVien
        WHERE MSV = 'KH_TEST_TV' AND KetQuaDanhGia = 'Dat') <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: them/thay doi ket qua that bai';
    END IF;
    IF (SELECT SoThanhVien FROM vw_KhoaHocThongKe WHERE MaKhoaHoc = 'KH_TEST_01') <> 1
       OR (SELECT SoThanhVien FROM vw_KhoaHocThongKe WHERE MaKhoaHoc = 'KH_TEST_02') <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: thong ke sai';
    END IF;

    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        INSERT INTO KhoaHoc VALUES ('KH_TEST_BAD', 'Sai ngay', '2026-10-31', '2026-10-01');
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: khong chan ngay sai'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        UPDATE KhoaHoc SET NgayBeGiang = '2026-09-01' WHERE MaKhoaHoc = 'KH_TEST_01';
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: UPDATE ngay sai khong bi chan'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1062 SET rejected = TRUE;
        INSERT INTO ThamGiaKhoaHoc VALUES ('KH_TEST_TV', 'KH_TEST_01', 'Hoc vien', NULL);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: khong chan tham gia trung'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1452 SET rejected = TRUE;
        INSERT INTO ThamGiaKhoaHoc VALUES ('KH_TEST_MISSING', 'KH_TEST_01', NULL, NULL);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: khong chan MSV khong ton tai'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1452 SET rejected = TRUE;
        INSERT INTO ThamGiaKhoaHoc VALUES ('KH_TEST_TV', 'KH_TEST_MISSING', NULL, NULL);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: khong chan khoa khong ton tai'; END IF;

    -- Cùng điều kiện xóa mà KhoaHocDao sử dụng: còn thành viên thì không xóa.
    DELETE FROM KhoaHoc WHERE MaKhoaHoc = 'KH_TEST_01'
    AND NOT EXISTS (SELECT 1 FROM ThamGiaKhoaHoc WHERE MaKhoaHoc = 'KH_TEST_01');
    IF ROW_COUNT() <> 0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: xoa nham khoa con thanh vien'; END IF;
    DELETE FROM ThamGiaKhoaHoc WHERE MaKhoaHoc = 'KH_TEST_01';
    DELETE FROM KhoaHoc WHERE MaKhoaHoc = 'KH_TEST_01';
    IF ROW_COUNT() <> 1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'KH: xoa khoa rong that bai'; END IF;
    ROLLBACK;
    SELECT 'PASS: KhoaHoc - du lieu hop le, thong ke, CHECK, PK, FK, xoa an toan' AS KetQua;
END$$
DELIMITER ;
CALL sp_KhoaHocTest();
DROP PROCEDURE sp_KhoaHocTest;

