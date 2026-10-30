-- Module 05 (Quản lý Dự án) – truy vấn / view / stored procedure / trigger. Phụ trách: Chiến
USE QuanLyCauLacBo;

-- AVG bỏ qua NULL; chưa có tiến độ nào thì quy ước tiến độ chung bằng 0.
DROP TRIGGER IF EXISTS trg_DuAn_TienDo_Insert;
DROP TRIGGER IF EXISTS trg_DuAn_TienDo_Update;
DROP TRIGGER IF EXISTS trg_DuAn_TienDo_Delete;
DELIMITER $$
CREATE TRIGGER trg_DuAn_TienDo_Insert AFTER INSERT ON ThamGiaDuAn
FOR EACH ROW
BEGIN
    UPDATE DuAn SET TienDoChung = (
        SELECT COALESCE(AVG(TienDoRieng), 0) FROM ThamGiaDuAn WHERE MaDuAn = NEW.MaDuAn
    ) WHERE MaDuAn = NEW.MaDuAn;
END$$

CREATE TRIGGER trg_DuAn_TienDo_Update AFTER UPDATE ON ThamGiaDuAn
FOR EACH ROW
BEGIN
    UPDATE DuAn SET TienDoChung = (
        SELECT COALESCE(AVG(TienDoRieng), 0) FROM ThamGiaDuAn WHERE MaDuAn = OLD.MaDuAn
    ) WHERE MaDuAn = OLD.MaDuAn;
    IF NOT (OLD.MaDuAn <=> NEW.MaDuAn) THEN
        UPDATE DuAn SET TienDoChung = (
            SELECT COALESCE(AVG(TienDoRieng), 0) FROM ThamGiaDuAn WHERE MaDuAn = NEW.MaDuAn
        ) WHERE MaDuAn = NEW.MaDuAn;
    END IF;
END$$

CREATE TRIGGER trg_DuAn_TienDo_Delete AFTER DELETE ON ThamGiaDuAn
FOR EACH ROW
BEGIN
    UPDATE DuAn SET TienDoChung = (
        SELECT COALESCE(AVG(TienDoRieng), 0) FROM ThamGiaDuAn WHERE MaDuAn = OLD.MaDuAn
    ) WHERE MaDuAn = OLD.MaDuAn;
END$$
DELIMITER ;

-- run_all.sql nạp seed trước khi tạo trigger, nên đồng bộ lại dữ liệu đã có.
UPDATE DuAn d
SET TienDoChung = (
    SELECT COALESCE(AVG(t.TienDoRieng), 0) FROM ThamGiaDuAn t WHERE t.MaDuAn = d.MaDuAn
);

DROP VIEW IF EXISTS vw_DuAnThanhVien;
CREATE VIEW vw_DuAnThanhVien AS
SELECT d.MaDuAn, d.TenDuAn, t.MSV, t.HoVaTen, tg.VaiTro, tg.TienDoRieng
FROM DuAn d
JOIN ThamGiaDuAn tg ON tg.MaDuAn = d.MaDuAn
JOIN ThanhVien t ON t.MSV = tg.MSV;

DROP VIEW IF EXISTS vw_DuAnThongKe;
CREATE VIEW vw_DuAnThongKe AS
SELECT d.MaDuAn, d.TenDuAn, d.MucTieu, d.NgayBatDau, d.HanHoanThanh,
       COUNT(tg.MSV) AS SoThanhVien, COALESCE(AVG(tg.TienDoRieng), 0) AS TienDoChung
FROM DuAn d
LEFT JOIN ThamGiaDuAn tg ON tg.MaDuAn = d.MaDuAn
GROUP BY d.MaDuAn, d.TenDuAn, d.MucTieu, d.NgayBatDau, d.HanHoanThanh;

DROP PROCEDURE IF EXISTS sp_DuAnTraCuu;
DELIMITER $$
CREATE PROCEDURE sp_DuAnTraCuu(IN p_TuKhoa VARCHAR(150))
BEGIN
    SELECT * FROM vw_DuAnThongKe
    WHERE LOCATE(COALESCE(TRIM(p_TuKhoa), ''), MaDuAn) > 0
       OR LOCATE(COALESCE(TRIM(p_TuKhoa), ''), TenDuAn) > 0
       OR LOCATE(COALESCE(TRIM(p_TuKhoa), ''), COALESCE(MucTieu, '')) > 0
    ORDER BY MaDuAn;
END$$
DELIMITER ;

