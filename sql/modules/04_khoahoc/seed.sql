-- Module 04 (Quản lý Khóa học) – dữ liệu mẫu. Phụ trách: Chiến
-- Dùng dải mã riêng (vd KH01.., DA01..) để không trùng với module khác.
USE QuanLyCauLacBo;

INSERT INTO KhoaHoc (MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang, NgayBeGiang) VALUES
    ('KH01', 'SQL co ban', '2026-10-01', '2026-10-31'),
    ('KH02', 'Lap trinh Java JDBC', '2026-11-01', NULL);

-- Chỉ tham chiếu thành viên đã có; seed nền của Tú hiện chưa có INSERT.
INSERT INTO ThamGiaKhoaHoc (MSV, MaKhoaHoc, VaiTro, KetQuaDanhGia)
SELECT MSV, 'KH01', 'Hoc vien', NULL
FROM ThanhVien ORDER BY MSV LIMIT 2;

