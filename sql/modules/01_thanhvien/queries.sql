-- Module 01 (Quản lý Thành viên) – view, thủ tục, tra cứu liên module. Phụ trách: Tú.
-- Chạy lại được (DROP ... IF EXISTS); không thay đổi schema dùng chung.
-- Lưu ý: MESSAGE_TEXT của SIGNAL tối đa 128 ký tự nên các thông báo đều ngắn.
USE QuanLyCauLacBo;

-- ---------------------------------------------------------------------
-- 1. VIEW
-- ---------------------------------------------------------------------

-- Thông tin đầy đủ của thành viên kèm tên ban.
DROP VIEW IF EXISTS vw_ThanhVienChiTiet;
CREATE VIEW vw_ThanhVienChiTiet AS
SELECT tv.MSV, tv.HoVaTen, tv.NgaySinh, tv.GioiTinh, tv.Email, tv.SDT,
       tv.NgayGiaNhap, tv.TrangThaiHD, tv.MaBan, b.TenBan, tv.ChucVu
FROM ThanhVien tv
JOIN BanChuyenMon b ON b.MaBan = tv.MaBan;

-- Số thành viên theo trạng thái hoạt động.
DROP VIEW IF EXISTS vw_ThongKeTrangThaiThanhVien;
CREATE VIEW vw_ThongKeTrangThaiThanhVien AS
SELECT COALESCE(TrangThaiHD, '(chưa ghi nhận)') AS TrangThaiHD, COUNT(*) AS SoThanhVien
FROM ThanhVien
GROUP BY TrangThaiHD;

-- LIÊN MODULE: mọi hoạt động của thành viên trong một bảng thời gian.
-- Gộp từ khóa học, dự án, sự kiện, đánh giá, phiếu mượn và giao dịch thu chi.
DROP VIEW IF EXISTS vw_LichSuHoatDong;
CREATE VIEW vw_LichSuHoatDong AS
SELECT g.MSV, 'Khóa học' AS LoaiHoatDong, k.MaKhoaHoc AS MaHoatDong, k.TenKhoaHoc AS TenHoatDong,
       g.VaiTro AS ChiTiet, k.NgayKhaiGiang AS Ngay
FROM ThamGiaKhoaHoc g
JOIN KhoaHoc k ON k.MaKhoaHoc = g.MaKhoaHoc
UNION ALL
SELECT g.MSV, 'Dự án', d.MaDuAn, d.TenDuAn, g.VaiTro, d.NgayBatDau
FROM ThamGiaDuAn g
JOIN DuAn d ON d.MaDuAn = g.MaDuAn
UNION ALL
SELECT g.MSV, 'Sự kiện', s.MaSK, s.TenSK, g.VaiTro, s.NgayBatDau
FROM ThamGiaSuKien g
JOIN SuKien s ON s.MaSK = g.MaSK
UNION ALL
SELECT MSV, 'Đánh giá', MaGhiNhan, LoaiDanhGia, HinhThuc, NgayQuyetDinh
FROM DanhGia
UNION ALL
SELECT MSVNguoiMuon, 'Phiếu mượn', MaPhieuMuon, MucDichMuon, TrangThaiDuyet, NgayMuon
FROM PhieuMuon
UNION ALL
SELECT MSV, 'Thu chi', MaGD, MucDich, LoaiGD, DATE(ThoiGian)
FROM ThuChi
WHERE MSV IS NOT NULL;

-- LIÊN MODULE: số lượng tham gia ở từng mảng hoạt động của mỗi thành viên.
DROP VIEW IF EXISTS vw_TongQuanThanhVien;
CREATE VIEW vw_TongQuanThanhVien AS
SELECT tv.MSV, tv.HoVaTen, tv.TrangThaiHD,
       (SELECT COUNT(*) FROM ThamGiaKhoaHoc g WHERE g.MSV = tv.MSV) AS SoKhoaHoc,
       (SELECT COUNT(*) FROM ThamGiaDuAn g WHERE g.MSV = tv.MSV) AS SoDuAn,
       (SELECT COUNT(*) FROM ThamGiaSuKien g WHERE g.MSV = tv.MSV) AS SoSuKien,
       (SELECT COUNT(*) FROM DanhGia d WHERE d.MSV = tv.MSV) AS SoDanhGia,
       (SELECT COUNT(*) FROM PhieuMuon p WHERE p.MSVNguoiMuon = tv.MSV) AS SoPhieuMuon
FROM ThanhVien tv;

-- ---------------------------------------------------------------------
-- 2. THỦ TỤC
-- ---------------------------------------------------------------------

-- Kiểm tra thông tin cá nhân dùng chung cho thêm mới và cập nhật.
DROP PROCEDURE IF EXISTS sp_KiemTraThongTinCaNhan;
DELIMITER $$
CREATE PROCEDURE sp_KiemTraThongTinCaNhan(
    IN p_HoVaTen VARCHAR(255), IN p_NgaySinh DATE, IN p_GioiTinh VARCHAR(255),
    IN p_Email VARCHAR(255), IN p_SDT VARCHAR(255))
BEGIN
    DECLARE v_GioiTinh VARCHAR(255);
    DECLARE v_Email VARCHAR(255);
    DECLARE v_SDT VARCHAR(255);
    SET v_GioiTinh = NULLIF(TRIM(p_GioiTinh), '');
    SET v_Email = NULLIF(TRIM(p_Email), '');
    SET v_SDT = NULLIF(TRIM(p_SDT), '');
    IF p_HoVaTen IS NULL OR CHAR_LENGTH(TRIM(p_HoVaTen)) = 0 OR CHAR_LENGTH(TRIM(p_HoVaTen)) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Họ và tên không hợp lệ (bắt buộc, tối đa 100 ký tự)';
    END IF;
    IF p_NgaySinh IS NOT NULL AND p_NgaySinh > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày sinh không được ở tương lai';
    END IF;
    IF v_GioiTinh IS NOT NULL AND v_GioiTinh NOT IN ('Nam', 'Nữ', 'Khác') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Giới tính phải là Nam, Nữ hoặc Khác';
    END IF;
    IF v_Email IS NOT NULL AND (CHAR_LENGTH(v_Email) > 100 OR v_Email NOT LIKE '_%@_%._%') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email không hợp lệ';
    END IF;
    IF v_SDT IS NOT NULL AND v_SDT NOT REGEXP '^[+]?[0-9]{9,15}$' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (9 đến 15 chữ số)';
    END IF;
END$$
DELIMITER ;

-- Thêm thành viên mới. Trạng thái trống = 'Đang hoạt động'; chức vụ trống = 'Thành viên'.
DROP PROCEDURE IF EXISTS sp_ThemThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_ThemThanhVien(
    IN p_MSV VARCHAR(255), IN p_HoVaTen VARCHAR(255), IN p_NgaySinh DATE, IN p_GioiTinh VARCHAR(255),
    IN p_Email VARCHAR(255), IN p_SDT VARCHAR(255), IN p_NgayGiaNhap DATE,
    IN p_TrangThaiHD VARCHAR(255), IN p_MaBan VARCHAR(255), IN p_ChucVu VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    DECLARE v_TrangThai VARCHAR(255);
    DECLARE v_MaBan VARCHAR(255);
    DECLARE v_ChucVu VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR CHAR_LENGTH(v_MSV) = 0 OR CHAR_LENGTH(v_MSV) > 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sinh viên không hợp lệ (bắt buộc, tối đa 20 ký tự)';
    END IF;
    CALL sp_KiemTraThongTinCaNhan(p_HoVaTen, p_NgaySinh, p_GioiTinh, p_Email, p_SDT);
    IF p_NgayGiaNhap IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày gia nhập không được để trống';
    END IF;
    IF p_NgayGiaNhap > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày gia nhập không được ở tương lai';
    END IF;
    IF p_NgaySinh IS NOT NULL AND p_NgayGiaNhap < p_NgaySinh THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày gia nhập không được trước ngày sinh';
    END IF;
    SET v_TrangThai = COALESCE(NULLIF(TRIM(p_TrangThaiHD), ''), 'Đang hoạt động');
    IF v_TrangThai NOT IN ('Đang hoạt động', 'Tạm nghỉ', 'Đã rời CLB') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Trạng thái phải là Đang hoạt động, Tạm nghỉ hoặc Đã rời CLB';
    END IF;
    SET v_MaBan = TRIM(p_MaBan);
    IF v_MaBan IS NULL OR NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = v_MaBan) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ban chuyên môn không tồn tại';
    END IF;
    SET v_ChucVu = NULLIF(TRIM(p_ChucVu), '');
    IF v_ChucVu IS NOT NULL AND CHAR_LENGTH(v_ChucVu) > 100 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chức vụ tối đa 100 ký tự';
    END IF;
    IF EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mã sinh viên đã tồn tại';
    END IF;
    INSERT INTO ThanhVien(MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT, NgayGiaNhap, TrangThaiHD, MaBan, ChucVu)
    VALUES(v_MSV, TRIM(p_HoVaTen), p_NgaySinh, NULLIF(TRIM(p_GioiTinh), ''), NULLIF(TRIM(p_Email), ''),
           NULLIF(TRIM(p_SDT), ''), p_NgayGiaNhap, v_TrangThai, v_MaBan, COALESCE(v_ChucVu, 'Thành viên'));
END$$
DELIMITER ;

-- Cập nhật thông tin cá nhân (họ tên, ngày sinh, giới tính, email, SĐT).
-- Ban và chức vụ đổi ở module 02; trạng thái đổi bằng sp_DoiTrangThaiThanhVien.
DROP PROCEDURE IF EXISTS sp_CapNhatThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatThanhVien(
    IN p_MSV VARCHAR(255), IN p_HoVaTen VARCHAR(255), IN p_NgaySinh DATE, IN p_GioiTinh VARCHAR(255),
    IN p_Email VARCHAR(255), IN p_SDT VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    CALL sp_KiemTraThongTinCaNhan(p_HoVaTen, p_NgaySinh, p_GioiTinh, p_Email, p_SDT);
    IF p_NgaySinh IS NOT NULL AND p_NgaySinh > (SELECT NgayGiaNhap FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày sinh không được sau ngày gia nhập';
    END IF;
    UPDATE ThanhVien
    SET HoVaTen = TRIM(p_HoVaTen), NgaySinh = p_NgaySinh, GioiTinh = NULLIF(TRIM(p_GioiTinh), ''),
        Email = NULLIF(TRIM(p_Email), ''), SDT = NULLIF(TRIM(p_SDT), '')
    WHERE MSV = v_MSV;
END$$
DELIMITER ;

-- Đổi trạng thái hoạt động (cách nên dùng thay cho xóa để giữ lịch sử).
DROP PROCEDURE IF EXISTS sp_DoiTrangThaiThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_DoiTrangThaiThanhVien(IN p_MSV VARCHAR(255), IN p_TrangThai VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    DECLARE v_TrangThai VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    SET v_TrangThai = TRIM(p_TrangThai);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    IF v_TrangThai IS NULL OR v_TrangThai NOT IN ('Đang hoạt động', 'Tạm nghỉ', 'Đã rời CLB') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Trạng thái phải là Đang hoạt động, Tạm nghỉ hoặc Đã rời CLB';
    END IF;
    UPDATE ThanhVien SET TrangThaiHD = v_TrangThai WHERE MSV = v_MSV;
END$$
DELIMITER ;

-- Xóa thành viên. Chỉ cho xóa khi chưa có lịch sử hoạt động nào; quyền được cấp sẽ bị xóa theo.
DROP PROCEDURE IF EXISTS sp_XoaThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_XoaThanhVien(IN p_MSV VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    IF EXISTS (SELECT 1 FROM ThamGiaKhoaHoc WHERE MSV = v_MSV)
       OR EXISTS (SELECT 1 FROM ThamGiaDuAn WHERE MSV = v_MSV)
       OR EXISTS (SELECT 1 FROM ThamGiaSuKien WHERE MSV = v_MSV)
       OR EXISTS (SELECT 1 FROM ThuChi WHERE MSV = v_MSV)
       OR EXISTS (SELECT 1 FROM DanhGia WHERE MSV = v_MSV)
       OR EXISTS (SELECT 1 FROM PhieuMuon WHERE MSVNguoiMuon = v_MSV)
       OR EXISTS (SELECT 1 FROM CoSoVatChat WHERE MSVQuanLy = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên đã có lịch sử hoạt động, hãy đổi trạng thái thành Đã rời CLB';
    END IF;
    DELETE FROM ThanhVien WHERE MSV = v_MSV;
END$$
DELIMITER ;

-- Tìm kiếm: từ khóa khớp một phần MSV hoặc họ tên; ban và trạng thái là bộ lọc tùy chọn (NULL hoặc trống = bỏ qua).
DROP PROCEDURE IF EXISTS sp_TimThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_TimThanhVien(IN p_TuKhoa VARCHAR(255), IN p_MaBan VARCHAR(255), IN p_TrangThai VARCHAR(255))
BEGIN
    DECLARE v_TuKhoa VARCHAR(255);
    DECLARE v_MaBan VARCHAR(255);
    DECLARE v_TrangThai VARCHAR(255);
    SET v_TuKhoa = NULLIF(TRIM(p_TuKhoa), '');
    SET v_MaBan = NULLIF(TRIM(p_MaBan), '');
    SET v_TrangThai = NULLIF(TRIM(p_TrangThai), '');
    SELECT MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT, NgayGiaNhap, TrangThaiHD, MaBan, TenBan, ChucVu
    FROM vw_ThanhVienChiTiet
    WHERE (v_TuKhoa IS NULL OR LOCATE(v_TuKhoa, MSV) > 0 OR LOCATE(v_TuKhoa, HoVaTen) > 0)
      AND (v_MaBan IS NULL OR MaBan = v_MaBan)
      AND (v_TrangThai IS NULL OR TrangThaiHD = v_TrangThai)
    ORDER BY MSV;
END$$
DELIMITER ;

-- Xem chi tiết một thành viên.
DROP PROCEDURE IF EXISTS sp_ChiTietThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_ChiTietThanhVien(IN p_MSV VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT, NgayGiaNhap, TrangThaiHD, MaBan, TenBan, ChucVu
    FROM vw_ThanhVienChiTiet WHERE MSV = v_MSV;
END$$
DELIMITER ;

-- Theo dõi ngày gia nhập: danh sách thành viên gia nhập trong khoảng ngày (NULL = không giới hạn đầu đó).
DROP PROCEDURE IF EXISTS sp_ThanhVienGiaNhap;
DELIMITER $$
CREATE PROCEDURE sp_ThanhVienGiaNhap(IN p_TuNgay DATE, IN p_DenNgay DATE)
BEGIN
    IF p_TuNgay IS NOT NULL AND p_DenNgay IS NOT NULL AND p_DenNgay < p_TuNgay THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Đến ngày không được trước từ ngày';
    END IF;
    SELECT MSV, HoVaTen, NgayGiaNhap, TrangThaiHD, MaBan, TenBan, ChucVu
    FROM vw_ThanhVienChiTiet
    WHERE (p_TuNgay IS NULL OR NgayGiaNhap >= p_TuNgay)
      AND (p_DenNgay IS NULL OR NgayGiaNhap <= p_DenNgay)
    ORDER BY NgayGiaNhap, MSV;
END$$
DELIMITER ;

-- LIÊN MODULE: lịch sử hoạt động của một thành viên, mới nhất trước.
DROP PROCEDURE IF EXISTS sp_LichSuHoatDongThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_LichSuHoatDongThanhVien(IN p_MSV VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT LoaiHoatDong, MaHoatDong, TenHoatDong, ChiTiet, Ngay
    FROM vw_LichSuHoatDong
    WHERE MSV = v_MSV
    ORDER BY Ngay DESC, LoaiHoatDong, MaHoatDong;
END$$
DELIMITER ;

-- LIÊN MODULE: tổng quan số hoạt động của một thành viên.
DROP PROCEDURE IF EXISTS sp_TongQuanThanhVien;
DELIMITER $$
CREATE PROCEDURE sp_TongQuanThanhVien(IN p_MSV VARCHAR(255))
BEGIN
    DECLARE v_MSV VARCHAR(255);
    SET v_MSV = TRIM(p_MSV);
    IF v_MSV IS NULL OR NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = v_MSV) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thành viên không tồn tại';
    END IF;
    SELECT MSV, HoVaTen, TrangThaiHD, SoKhoaHoc, SoDuAn, SoSuKien, SoDanhGia, SoPhieuMuon
    FROM vw_TongQuanThanhVien WHERE MSV = v_MSV;
END$$
DELIMITER ;
