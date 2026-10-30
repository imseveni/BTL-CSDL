-- Module 06 (Quản lý Sự kiện) – dữ liệu mẫu. Phụ trách: Minh
-- Dùng dải mã riêng (SK01..) để không trùng với module khác.
USE QuanLyCauLacBo;

-- 1. Thêm dữ liệu mẫu cho bảng SuKien (dải mã SK01 - SK05)
INSERT INTO SuKien (MaSK, TenSK, NgayBatDau, NgayKetThuc) VALUES
    ('SK01', 'Ngày hội Chào tân sinh viên CLB IT', '2026-09-05', '2026-09-06'),
    ('SK02', 'Workshop Git & GitHub thực chiến', '2026-09-20', '2026-09-20'),
    ('SK03', 'Cuộc thi Hackathon Mùa Thu 2026', '2026-10-10', '2026-10-12'),
    ('SK04', 'Seminar Định hướng nghề nghiệp AI & Data', '2026-11-01', '2026-11-01'),
    ('SK05', 'Đêm Gala Tổng kết hoạt động cuối năm', '2026-12-20', NULL);

-- 2. Thêm dữ liệu mẫu cho bảng ThamGiaSuKien
-- Lưu ý: Chỉ tự động thêm nếu bảng ThanhVien đã có sẵn các mã MSV từ seed_master.sql
-- để đảm bảo chạy run_all.sql không lỗi ngay cả khi seed_master.sql chưa có dữ liệu.
INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK01', 'Trưởng ban tổ chức' FROM ThanhVien WHERE MSV = 'B21DCCN001';

INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK01', 'Hậu cần' FROM ThanhVien WHERE MSV = 'B21DCCN002';

INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK02', 'Diễn giả' FROM ThanhVien WHERE MSV = 'B21DCCN001';

INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK02', 'Thành viên tham dự' FROM ThanhVien WHERE MSV = 'B21DCCN003';

INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK03', 'Ban giám khảo' FROM ThanhVien WHERE MSV = 'B21DCCN002';

INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro)
SELECT MSV, 'SK03', 'Thí sinh' FROM ThanhVien WHERE MSV = 'B21DCCN004';
