-- =====================================================================
-- PHÂN QUYỀN CƠ SỞ DỮ LIỆU (MySQL 8.0 trở lên). Phụ trách: Tú.
-- Hai nhóm người dùng theo đề bài: Thành viên và Ban Chủ nhiệm, cộng thêm vai trò quản trị.
--
-- Cách chạy: file này KHÔNG nằm trong run_all.sql vì cần tài khoản có quyền CREATE ROLE
-- và GRANT (thường là root). Chạy SAU run_all.sql, từ thư mục sql/:
--     mysql -u root -p < 02_security/phan_quyen.sql
-- Chạy lại được: CREATE ... IF NOT EXISTS, GRANT lặp lại không gây lỗi.
--
-- Đây là phân quyền ở tầng MySQL (tài khoản đăng nhập vào CSDL), khác với bảng QuyenHan là
-- danh mục quyền nghiệp vụ trong ứng dụng.
-- =====================================================================
-- Báo cho MySQL biết file này là UTF-8 (nếu thiếu, console Windows làm hỏng chữ tiếng Việt khi nạp).
SET NAMES utf8mb4;
USE QuanLyCauLacBo;

-- 1. View cho thành viên thường: không lộ ngày sinh, email, số điện thoại.
DROP VIEW IF EXISTS vw_ThanhVienCongKhai;
CREATE VIEW vw_ThanhVienCongKhai AS
SELECT tv.MSV, tv.HoVaTen, tv.GioiTinh, b.TenBan, tv.ChucVu, tv.TrangThaiHD
FROM ThanhVien tv
JOIN BanChuyenMon b ON b.MaBan = tv.MaBan;

-- 2. Ba vai trò
CREATE ROLE IF NOT EXISTS 'clb_thanh_vien', 'clb_ban_chu_nhiem', 'clb_quan_tri';

-- 2a. Thành viên: chỉ xem một số view công khai, không đọc trực tiếp bảng nào.
GRANT SELECT ON QuanLyCauLacBo.vw_ThanhVienCongKhai  TO 'clb_thanh_vien';
GRANT SELECT ON QuanLyCauLacBo.vw_BanChuyenMonThongKe TO 'clb_thanh_vien';
GRANT SELECT ON QuanLyCauLacBo.vw_DanhSachSuKien      TO 'clb_thanh_vien';
GRANT SELECT ON QuanLyCauLacBo.vw_DanhSachThietBi     TO 'clb_thanh_vien';

-- 2b. Ban Chủ nhiệm: đọc, thêm, sửa, xóa dữ liệu và chạy thủ tục; không được sửa cấu trúc bảng.
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON QuanLyCauLacBo.* TO 'clb_ban_chu_nhiem';

-- 2c. Quản trị: toàn quyền trên CSDL của câu lạc bộ (chỉ trên database này).
GRANT ALL PRIVILEGES ON QuanLyCauLacBo.* TO 'clb_quan_tri';

-- 3. Tạo tài khoản cụ thể. KHÔNG ghi mật khẩu thật vào Git: thay <mat-khau> khi chạy thủ công.
-- CREATE USER IF NOT EXISTS 'tv_demo'@'localhost'  IDENTIFIED BY '<mat-khau>';
-- CREATE USER IF NOT EXISTS 'bcn_demo'@'localhost' IDENTIFIED BY '<mat-khau>';
-- GRANT 'clb_thanh_vien'     TO 'tv_demo'@'localhost';
-- GRANT 'clb_ban_chu_nhiem'  TO 'bcn_demo'@'localhost';
-- SET DEFAULT ROLE 'clb_thanh_vien'    TO 'tv_demo'@'localhost';
-- SET DEFAULT ROLE 'clb_ban_chu_nhiem' TO 'bcn_demo'@'localhost';

-- 4. Kiểm tra (mở phiên đăng nhập bằng tài khoản demo rồi chạy):
--   Với tv_demo:
--     SELECT * FROM vw_ThanhVienCongKhai LIMIT 3;   -- được
--     SELECT * FROM ThanhVien LIMIT 3;               -- bị từ chối (ERROR 1142)
--     DELETE FROM BanChuyenMon;                      -- bị từ chối
--   Với bcn_demo:
--     SELECT * FROM ThanhVien LIMIT 3;               -- được
--     CALL sp_DoiTrangThaiThanhVien('B21DCCN017', 'Đang hoạt động');  -- được
--     DROP TABLE ThuChi;                             -- bị từ chối
-- Xem quyền của một vai trò:  SHOW GRANTS FOR 'clb_thanh_vien';
