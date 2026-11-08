-- Module 01 (Quản lý Thành viên) – ca thử đúng và sai. Phụ trách: Tú.
-- Chạy sau run_all.sql, trên kết nối riêng (không nằm trong transaction đang mở).
-- Toàn bộ dữ liệu thử mang mã T_TU_*, được tạo trong một transaction rồi ROLLBACK nên không để lại gì.
-- Fixture dùng INSERT thường: nếu trùng mã thì dừng, không sửa dữ liệu sẵn có.
-- Mỗi ca "sai" bắt đúng MYSQL_ERRNO mong đợi (1644 = SIGNAL 45000); lỗi khác sẽ làm test thất bại.
USE QuanLyCauLacBo;

DROP PROCEDURE IF EXISTS sp_TestTu01;
DELIMITER $$
CREATE PROCEDURE sp_TestTu01()
BEGIN
    DECLARE v_error INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO BanChuyenMon(MaBan, TenBan) VALUES('T_TU_B1', 'Ban thử 1');
    INSERT INTO KhoaHoc(MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang) VALUES('T_TU_KH1', 'Khóa học thử', '2026-01-01');
    -- Thêm hợp lệ, trạng thái và chức vụ lấy mặc định
    CALL sp_ThemThanhVien('T_TU_TV1', '  Thành viên thử 1  ', '2004-01-01', 'Nam', 'tv1@example.com', '0912345678', '2025-01-01', NULL, 'T_TU_B1', NULL);
    IF NOT COALESCE(((SELECT HoVaTen = 'Thành viên thử 1' AND TrangThaiHD = 'Đang hoạt động' AND ChucVu = 'Thành viên' AND TenBan = 'Ban thử 1' FROM vw_ThanhVienChiTiet WHERE MSV = 'T_TU_TV1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm thành viên với giá trị mặc định';
    END IF;
    CALL sp_ThemThanhVien('T_TU_TV2', 'Thành viên thử 2', NULL, NULL, NULL, NULL, '2025-02-01', 'Tạm nghỉ', 'T_TU_B1', 'Phó ban');
    IF NOT COALESCE(((SELECT TrangThaiHD = 'Tạm nghỉ' AND ChucVu = 'Phó ban' AND Email IS NULL AND SDT IS NULL FROM ThanhVien WHERE MSV = 'T_TU_TV2') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thêm thành viên không có email, SĐT';
    END IF;
    -- Các ca sai khi thêm
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_TV1', 'Trùng', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn MSV trùng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('   ', 'Tên', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn MSV trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien(REPEAT('x', 21), 'Tên', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn MSV quá dài';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', '   ', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn họ tên trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', '2999-01-01', NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ngày sinh ở tương lai';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, 'Robot', NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn giới tính lạ';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, 'khong-co-a-cong', NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn email sai định dạng';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, '12ab', '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn số điện thoại sai';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, NULL, NULL, NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ngày gia nhập trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, NULL, '2999-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ngày gia nhập ở tương lai';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', '2025-06-01', NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ngày gia nhập trước ngày sinh';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, NULL, '2025-01-01', 'Mất tích', 'T_TU_B1', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn trạng thái lạ';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_NONE', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ban không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThemThanhVien('T_TU_BAD', 'Tên', NULL, NULL, NULL, NULL, '2025-01-01', NULL, 'T_TU_B1', REPEAT('x', 101));
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn chức vụ quá dài';
    END IF;
    IF NOT COALESCE((NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = 'T_TU_BAD')), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: các ca sai không được để lại dữ liệu';
    END IF;
    -- Cập nhật thông tin cá nhân
    CALL sp_CapNhatThanhVien('T_TU_TV1', 'Tên đã sửa', '2004-02-02', 'Nữ', 'moi@example.com', '+84912345678');
    IF NOT COALESCE(((SELECT HoVaTen = 'Tên đã sửa' AND NgaySinh = '2004-02-02' AND GioiTinh = 'Nữ' AND Email = 'moi@example.com' AND SDT = '+84912345678' FROM ThanhVien WHERE MSV = 'T_TU_TV1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cập nhật thông tin cá nhân';
    END IF;
    IF NOT COALESCE(((SELECT MaBan = 'T_TU_B1' AND TrangThaiHD = 'Đang hoạt động' FROM ThanhVien WHERE MSV = 'T_TU_TV1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cập nhật cá nhân không làm đổi ban và trạng thái';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapNhatThanhVien('T_TU_NONE', 'Tên', NULL, NULL, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cập nhật thành viên không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapNhatThanhVien('T_TU_TV1', '', NULL, NULL, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cập nhật với họ tên trống';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapNhatThanhVien('T_TU_TV1', 'Tên', '2026-06-01', NULL, NULL, NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn ngày sinh sau ngày gia nhập';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_CapNhatThanhVien('T_TU_TV1', 'Tên', NULL, NULL, 'sai', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: cập nhật với email sai';
    END IF;
    -- Đổi trạng thái
    CALL sp_DoiTrangThaiThanhVien('T_TU_TV1', 'Đã rời CLB');
    IF NOT COALESCE(((SELECT TrangThaiHD FROM ThanhVien WHERE MSV = 'T_TU_TV1') = 'Đã rời CLB'), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi trạng thái';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_DoiTrangThaiThanhVien('T_TU_TV1', 'Bay mất');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn trạng thái lạ khi đổi';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_DoiTrangThaiThanhVien('T_TU_NONE', 'Tạm nghỉ');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: đổi trạng thái thành viên không tồn tại';
    END IF;
    -- Xóa: chặn khi đã có lịch sử, cho phép khi chưa có
    INSERT INTO ThamGiaKhoaHoc(MSV, MaKhoaHoc, VaiTro) VALUES('T_TU_TV1', 'T_TU_KH1', 'Học viên');
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_XoaThanhVien('T_TU_TV1');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn xóa thành viên đã có lịch sử';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_XoaThanhVien('T_TU_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa thành viên không tồn tại';
    END IF;
    CALL sp_XoaThanhVien('T_TU_TV2');
    IF NOT COALESCE((NOT EXISTS (SELECT 1 FROM ThanhVien WHERE MSV = 'T_TU_TV2')), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: xóa thành viên chưa có lịch sử';
    END IF;
    -- Các view và tra cứu liên module
    IF NOT COALESCE(((SELECT COUNT(*) FROM vw_LichSuHoatDong WHERE MSV = 'T_TU_TV1' AND LoaiHoatDong = 'Khóa học' AND MaHoatDong = 'T_TU_KH1' AND ChiTiet = 'Học viên') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lịch sử hoạt động có khóa học';
    END IF;
    IF NOT COALESCE(((SELECT SoKhoaHoc = 1 AND SoDuAn = 0 AND SoSuKien = 0 AND SoDanhGia = 0 AND SoPhieuMuon = 0 FROM vw_TongQuanThanhVien WHERE MSV = 'T_TU_TV1') = 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: tổng quan hoạt động của thành viên';
    END IF;
    IF NOT COALESCE(((SELECT SoThanhVien FROM vw_ThongKeTrangThaiThanhVien WHERE TrangThaiHD = 'Đã rời CLB') >= 1), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: thống kê theo trạng thái';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_LichSuHoatDongThanhVien('T_TU_NONE');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: lịch sử của thành viên không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_ThanhVienGiaNhap('2026-02-01', '2026-01-01');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: chặn khoảng ngày bị đảo ngược';
    END IF;
    -- Hai thủ tục báo cáo: kiểm tra tham số sai (kết quả đúng được kiểm tra bằng mắt ở tab Báo cáo)
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_BaoCaoDanhSachThanhVien('T_TU_NONE', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: báo cáo danh sách: chặn ban không tồn tại';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_BaoCaoDanhSachThanhVien(NULL, 'Bay mất');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: báo cáo danh sách: chặn trạng thái lạ';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_BaoCaoHoatDongThanhVien(NULL, '2026-12-31');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: báo cáo hoạt động: chặn thiếu từ ngày';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_BaoCaoHoatDongThanhVien('2026-01-01', NULL);
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: báo cáo hoạt động: chặn thiếu đến ngày';
    END IF;
    SET v_error = 0;
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_error = MYSQL_ERRNO;
        END;
        CALL sp_BaoCaoHoatDongThanhVien('2026-12-31', '2026-01-01');
    END;
    IF NOT COALESCE((v_error = 1644), FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'FAIL: báo cáo hoạt động: chặn khoảng ngày đảo ngược';
    END IF;
    ROLLBACK;
    SELECT 'PASS module 01: dữ liệu thử đã rollback' AS KetQua;
END$$
DELIMITER ;

CALL sp_TestTu01();
DROP PROCEDURE IF EXISTS sp_TestTu01;

-- Kiểm tra không còn dữ liệu thử (mong đợi 0, 0, 0):
SELECT (SELECT COUNT(*) FROM ThanhVien WHERE MSV LIKE 'T\_TU\_%') AS ThanhVienThu,
       (SELECT COUNT(*) FROM BanChuyenMon WHERE MaBan LIKE 'T\_TU\_%') AS BanThu,
       (SELECT COUNT(*) FROM KhoaHoc WHERE MaKhoaHoc LIKE 'T\_TU\_%') AS KhoaHocThu;
