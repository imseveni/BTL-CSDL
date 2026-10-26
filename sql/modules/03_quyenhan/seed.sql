-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;


-- Mã QH03_* tránh trùng QH01/QH02 của seed_master. Đây là danh mục nghiệp vụ,
-- không tự cấp tài khoản/role MySQL hoặc tự mở quyền truy cập ứng dụng.
INSERT INTO QuyenHan(MaQuyenHan, TenQuyenHan, MoTaChiTiet)
SELECT 'QH03_XEM', 'Xem thông tin nhân sự', 'Tra cứu ban, quyền và lịch sử đánh giá'
WHERE NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = 'QH03_XEM');
INSERT INTO QuyenHan(MaQuyenHan, TenQuyenHan, MoTaChiTiet)
SELECT 'QH03_QLNS', 'Quản lý nhân sự', 'Điều chỉnh ban và chức vụ theo quyết định của câu lạc bộ'
WHERE NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = 'QH03_QLNS');
INSERT INTO QuyenHan(MaQuyenHan, TenQuyenHan, MoTaChiTiet)
SELECT 'QH03_DG', 'Ghi nhận đánh giá', 'Lập và cập nhật quyết định đánh giá thành viên'
WHERE NOT EXISTS (SELECT 1 FROM QuyenHan WHERE MaQuyenHan = 'QH03_DG');
-- Chưa có mã MSV nền được Tú chốt: việc cấp quyền mẫu thực hiện trong tests.sql
-- bằng fixture có rollback, tránh tự cấp quyền cho thành viên thật.
