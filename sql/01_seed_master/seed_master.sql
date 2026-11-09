-- =====================================================================
-- DỮ LIỆU NỀN dùng chung: ban chuyên môn, thành viên, quyền hạn và phân quyền mẫu.
-- Phụ trách: Tú. Các module khác chỉ THAM CHIẾU mã trong file này, không sửa.
-- Chạy lại được: dùng INSERT IGNORE nên không lỗi khi dữ liệu đã có.
--   * Ban:        BAN01..BAN05
--   * Thành viên: B21DCCN001..B21DCCN020 (dữ liệu giả, không phải người thật)
--   * Quyền hạn:  QH01..QH05
-- Quy ước: B21DCCN001 là Chủ nhiệm (có đủ quyền), B21DCCN002 là Phó chủ nhiệm.
-- =====================================================================
-- Báo cho MySQL biết file này là UTF-8 (nếu thiếu, console Windows làm hỏng chữ tiếng Việt khi nạp).
SET NAMES utf8mb4;
USE QuanLyCauLacBo;

-- 1. Ban chuyên môn
INSERT IGNORE INTO BanChuyenMon (MaBan, TenBan) VALUES
    ('BAN01', 'Ban Chủ nhiệm'),
    ('BAN02', 'Ban Chuyên môn'),
    ('BAN03', 'Ban Truyền thông'),
    ('BAN04', 'Ban Sự kiện'),
    ('BAN05', 'Ban Hậu cần');

-- 2. Thành viên (Email, SDT là thuộc tính đơn trị; số điện thoại là số giả)
INSERT IGNORE INTO ThanhVien
    (MSV, HoVaTen, NgaySinh, GioiTinh, Email, SDT, NgayGiaNhap, TrangThaiHD, MaBan, ChucVu) VALUES
    ('B21DCCN001', 'Nguyễn Văn An', '2003-02-14', 'Nam', 'b21dccn001@example.com', '0900000001', '2023-09-10', 'Đang hoạt động', 'BAN01', 'Chủ nhiệm'),
    ('B21DCCN002', 'Trần Thị Bình', '2003-05-21', 'Nữ', 'b21dccn002@example.com', '0900000002', '2023-09-10', 'Đang hoạt động', 'BAN01', 'Phó chủ nhiệm'),
    ('B21DCCN003', 'Lê Quang Cường', '2003-11-03', 'Nam', 'b21dccn003@example.com', '0900000003', '2023-09-10', 'Đang hoạt động', 'BAN02', 'Trưởng ban'),
    ('B21DCCN004', 'Phạm Thu Dung', '2003-07-30', 'Nữ', 'b21dccn004@example.com', '0900000004', '2023-09-10', 'Đang hoạt động', 'BAN03', 'Trưởng ban'),
    ('B21DCCN005', 'Hoàng Minh Đức', '2004-01-09', 'Nam', 'b21dccn005@example.com', '0900000005', '2023-10-02', 'Đang hoạt động', 'BAN04', 'Trưởng ban'),
    ('B21DCCN006', 'Vũ Thị Giang', '2004-03-18', 'Nữ', 'b21dccn006@example.com', '0900000006', '2023-10-02', 'Đang hoạt động', 'BAN05', 'Trưởng ban'),
    ('B21DCCN007', 'Đặng Văn Hải', '2004-06-25', 'Nam', 'b21dccn007@example.com', '0900000007', '2023-10-02', 'Đang hoạt động', 'BAN02', 'Phó ban'),
    ('B21DCCN008', 'Bùi Thị Hà', '2004-09-12', 'Nữ', 'b21dccn008@example.com', '0900000008', '2024-02-20', 'Đang hoạt động', 'BAN03', 'Phó ban'),
    ('B21DCCN009', 'Đỗ Anh Khoa', '2004-12-01', 'Nam', 'b21dccn009@example.com', '0900000009', '2024-02-20', 'Đang hoạt động', 'BAN04', 'Phó ban'),
    ('B21DCCN010', 'Ngô Thị Lan', '2004-04-07', 'Nữ', 'b21dccn010@example.com', '0900000010', '2024-02-20', 'Đang hoạt động', 'BAN05', 'Phó ban'),
    ('B21DCCN011', 'Dương Văn Long', '2005-01-15', 'Nam', 'b21dccn011@example.com', '0900000011', '2024-09-09', 'Đang hoạt động', 'BAN02', 'Thành viên'),
    ('B21DCCN012', 'Lý Thị Mai', '2005-08-22', 'Nữ', 'b21dccn012@example.com', '0900000012', '2024-09-09', 'Đang hoạt động', 'BAN02', 'Thành viên'),
    ('B21DCCN013', 'Phan Văn Nam', '2005-05-05', 'Nam', 'b21dccn013@example.com', '0900000013', '2024-09-09', 'Đang hoạt động', 'BAN03', 'Thành viên'),
    ('B21DCCN014', 'Trịnh Thị Oanh', '2005-10-27', 'Nữ', 'b21dccn014@example.com', '0900000014', '2024-09-09', 'Đang hoạt động', 'BAN03', 'Thành viên'),
    ('B21DCCN015', 'Mai Văn Phúc', '2005-02-11', 'Nam', 'b21dccn015@example.com', '0900000015', '2024-09-09', 'Đang hoạt động', 'BAN04', 'Thành viên'),
    ('B21DCCN016', 'Cao Thị Quỳnh', '2005-06-19', 'Nữ', 'b21dccn016@example.com', '0900000016', '2025-02-17', 'Đang hoạt động', 'BAN04', 'Thành viên'),
    ('B21DCCN017', 'Tạ Văn Sơn', '2005-11-30', 'Nam', 'b21dccn017@example.com', '0900000017', '2025-02-17', 'Tạm nghỉ', 'BAN05', 'Thành viên'),
    ('B21DCCN018', 'Hồ Thị Trang', '2006-03-08', 'Nữ', 'b21dccn018@example.com', '0900000018', '2025-09-08', 'Đang hoạt động', 'BAN05', 'Thành viên'),
    ('B21DCCN019', 'Đinh Văn Út', '2006-07-16', 'Nam', 'b21dccn019@example.com', '0900000019', '2025-09-08', 'Đang hoạt động', 'BAN02', 'Thành viên'),
    ('B21DCCN020', 'Lương Thị Vân', '2002-12-24', 'Nữ', 'b21dccn020@example.com', '0900000020', '2023-09-10', 'Đã rời CLB', 'BAN03', 'Thành viên');

-- 3. Quyền hạn
INSERT IGNORE INTO QuyenHan (MaQuyenHan, TenQuyenHan, MoTaChiTiet) VALUES
    ('QH01', 'Quản trị hệ thống', 'Toàn quyền thêm, sửa, xóa và tra cứu mọi dữ liệu của câu lạc bộ'),
    ('QH02', 'Duyệt phiếu mượn', 'Duyệt hoặc từ chối phiếu mượn cơ sở vật chất'),
    ('QH03', 'Quản lý tài chính', 'Ghi nhận và kiểm tra các khoản thu chi'),
    ('QH04', 'Quản lý nhân sự', 'Điều chỉnh ban, chức vụ và quyền hạn của thành viên'),
    ('QH05', 'Quản lý hoạt động', 'Quản lý khóa học, dự án và sự kiện');

-- 4. Phân quyền mẫu cho thành viên
INSERT IGNORE INTO ThanhVien_QuyenHan (MSV, MaQuyenHan) VALUES
    ('B21DCCN001', 'QH01'),
    ('B21DCCN001', 'QH02'),
    ('B21DCCN001', 'QH03'),
    ('B21DCCN001', 'QH04'),
    ('B21DCCN001', 'QH05'),
    ('B21DCCN002', 'QH02'),
    ('B21DCCN002', 'QH03'),
    ('B21DCCN002', 'QH04'),
    ('B21DCCN003', 'QH05'),
    ('B21DCCN004', 'QH05'),
    ('B21DCCN005', 'QH05'),
    ('B21DCCN005', 'QH02'),
    ('B21DCCN006', 'QH02'),
    ('B21DCCN007', 'QH05');
