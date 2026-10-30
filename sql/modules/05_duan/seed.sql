-- Module 05 (Quản lý Dự án) – dữ liệu mẫu. Phụ trách: Chiến
-- Dùng dải mã riêng (vd KH01.., DA01..) để không trùng với module khác.
USE QuanLyCauLacBo;

INSERT INTO DuAn (MaDuAn, TenDuAn, MucTieu, NgayBatDau, HanHoanThanh) VALUES
    ('DA01', 'Quan ly cau lac bo', 'Xay dung ung dung Java va MySQL', '2026-10-01', '2026-12-01'),
    ('DA02', 'Cong thong tin CLB', 'Tra cuu hoat dong cua CLB', '2026-11-01', NULL);

-- Không tự thêm thành viên vào dữ liệu nền dùng chung.
INSERT INTO ThamGiaDuAn (MSV, MaDuAn, VaiTro, TienDoRieng)
SELECT MSV, 'DA01', 'Thanh vien', 20
FROM ThanhVien ORDER BY MSV LIMIT 2;

