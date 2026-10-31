-- Module 05 (Quản lý Dự án) – ca thử (đúng + sai) để kiểm tra ràng buộc. Phụ trách: Chiến
USE QuanLyCauLacBo;

DROP PROCEDURE IF EXISTS sp_DuAnAssertProgress;
DROP PROCEDURE IF EXISTS sp_DuAnTest;
DELIMITER $$
CREATE PROCEDURE sp_DuAnAssertProgress(IN p_Ma VARCHAR(20), IN p_Expected DECIMAL(5,2))
BEGIN
    IF NOT ((SELECT TienDoChung FROM DuAn WHERE MaDuAn = p_Ma) <=> p_Expected)
       OR NOT ((SELECT ROUND(TienDoChung, 2) FROM vw_DuAnThongKe WHERE MaDuAn = p_Ma) <=> p_Expected) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: tien do luu/view khong dung';
    END IF;
END$$

CREATE PROCEDURE sp_DuAnTest()
BEGIN
    DECLARE rejected BOOLEAN DEFAULT FALSE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO BanChuyenMon VALUES ('DA_TEST_BAN', 'Ban test du an');
    INSERT INTO ThanhVien (MSV, HoVaTen, NgayGiaNhap, MaBan) VALUES
        ('DA_TEST_TV1', 'TV test 1', '2026-10-01', 'DA_TEST_BAN'),
        ('DA_TEST_TV2', 'TV test 2', '2026-10-01', 'DA_TEST_BAN');
    INSERT INTO DuAn (MaDuAn, TenDuAn, NgayBatDau, HanHoanThanh) VALUES
        ('DA_TEST_01', 'Du an test 1', '2026-10-01', '2026-12-01'),
        ('DA_TEST_02', 'Du an test 2', '2026-10-01', NULL);
    CALL sp_DuAnAssertProgress('DA_TEST_01', 0);
    INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV1', 'DA_TEST_01', 'Lap trinh', 20);
    CALL sp_DuAnAssertProgress('DA_TEST_01', 20);
    INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV2', 'DA_TEST_01', 'Kiem thu', 80);
    CALL sp_DuAnAssertProgress('DA_TEST_01', 50);
    UPDATE ThamGiaDuAn SET TienDoRieng = 100 WHERE MSV = 'DA_TEST_TV1' AND MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 90);
    UPDATE ThamGiaDuAn SET MaDuAn = 'DA_TEST_02' WHERE MSV = 'DA_TEST_TV2' AND MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 100);
    CALL sp_DuAnAssertProgress('DA_TEST_02', 80);
    UPDATE ThamGiaDuAn SET TienDoRieng = NULL WHERE MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 0);
    INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV2', 'DA_TEST_01', 'Kiem thu', 0);
    CALL sp_DuAnAssertProgress('DA_TEST_01', 0);
    UPDATE ThamGiaDuAn SET TienDoRieng = 33.33 WHERE MSV = 'DA_TEST_TV1' AND MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 16.67);
    DELETE FROM ThamGiaDuAn WHERE MSV = 'DA_TEST_TV2' AND MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 33.33);
    DELETE FROM ThamGiaDuAn WHERE MaDuAn = 'DA_TEST_01';
    CALL sp_DuAnAssertProgress('DA_TEST_01', 0);

    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV1', 'DA_TEST_01', NULL, 150);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan tien do 150'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV1', 'DA_TEST_01', NULL, -1);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan tien do am'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        UPDATE ThamGiaDuAn SET TienDoRieng = 150 WHERE MaDuAn = 'DA_TEST_02';
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: UPDATE tien do sai khong bi chan'; END IF;
    CALL sp_DuAnAssertProgress('DA_TEST_02', 80);
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 3819 SET rejected = TRUE;
        INSERT INTO DuAn (MaDuAn, TenDuAn, NgayBatDau, HanHoanThanh)
        VALUES ('DA_TEST_BAD', 'Sai ngay', '2026-10-31', '2026-10-01');
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan han sai'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1062 SET rejected = TRUE;
        INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV2', 'DA_TEST_02', NULL, 10);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan tham gia trung'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1452 SET rejected = TRUE;
        INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_MISSING', 'DA_TEST_01', NULL, 10);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan MSV sai'; END IF;
    SET rejected = FALSE;
    BEGIN
        DECLARE CONTINUE HANDLER FOR 1452 SET rejected = TRUE;
        INSERT INTO ThamGiaDuAn VALUES ('DA_TEST_TV1', 'DA_TEST_MISSING', NULL, 10);
    END;
    IF NOT rejected THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DA: khong chan du an sai'; END IF;
    ROLLBACK;
    SELECT 'PASS: DuAn - INSERT/UPDATE/DELETE/chuyen du an, NULL, lam tron, CHECK, PK, FK' AS KetQua;
END$$
DELIMITER ;
CALL sp_DuAnTest();
DROP PROCEDURE sp_DuAnTest;
DROP PROCEDURE sp_DuAnAssertProgress;

