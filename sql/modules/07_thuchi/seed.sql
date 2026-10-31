-- Module 07 (Quản lý Thu – Chi) – dữ liệu mẫu. Phụ trách: Minh
-- Dùng dải mã riêng (GD01..) để không trùng với module khác.
USE QuanLyCauLacBo;

-- 1. Thêm dữ liệu mẫu cho bảng ThuChi
-- Lưu ý: Cột MSV để NULL nhằm không phụ thuộc vào dữ liệu ThanhVien khi seed_master.sql chưa cập nhật.
-- Cột MaSK tham chiếu đến các sự kiện SK01..SK04 đã tạo ở module 06_sukien (hoặc NULL nếu thu/chi chung CLB).
INSERT INTO ThuChi (MaGD, LoaiGD, MucDich, SoTien, ThoiGian, MinhChung, MSV, MaSK) VALUES
    ('GD01', 'Thu', 'Thu quỹ tài trợ Ngày hội Chào tân sinh viên', 5000000.00, '2026-09-01 09:00:00', 'UNC_TaiTro_SK01.pdf', NULL, 'SK01'),
    ('GD02', 'Chi', 'In ấn backdrop, standee và mua nước uống Chào tân', 1850000.00, '2026-09-04 15:30:00', 'HD_InAn_SK01.jpg', NULL, 'SK01'),
    ('GD03', 'Chi', 'Mua tài liệu và trà bánh teabreak Workshop Git', 650000.00, '2026-09-19 16:00:00', 'HD_Teabreak_SK02.png', NULL, 'SK02'),
    ('GD04', 'Thu', 'Tài trợ doanh nghiệp cho cuộc thi Hackathon Mùa Thu', 12000000.00, '2026-10-05 10:15:00', 'HopDong_TaiTro_SK03.pdf', NULL, 'SK03'),
    ('GD05', 'Chi', 'Trao giải thưởng và thuê hội trường Hackathon Mùa Thu', 8500000.00, '2026-10-12 18:00:00', 'BienBan_TraoGiai_SK03.pdf', NULL, 'SK03'),
    ('GD06', 'Thu', 'Thu quỹ định kỳ học kỳ I của Câu lạc bộ', 3000000.00, '2026-09-10 08:30:00', 'DS_DongQuy_HK1.xlsx', NULL, NULL);

-- 2. Thêm dữ liệu mẫu cho bảng ThuChi_PhuongThucThanhToan (thuộc tính đa trị)
INSERT INTO ThuChi_PhuongThucThanhToan (MaGD, PhuongThucThanhToan) VALUES
    ('GD01', 'Chuyển khoản ngân hàng'),
    ('GD02', 'Tiền mặt'),
    ('GD02', 'Chuyển khoản ngân hàng'),
    ('GD03', 'Ví điện tử MoMo'),
    ('GD04', 'Chuyển khoản ngân hàng'),
    ('GD05', 'Chuyển khoản ngân hàng'),
    ('GD05', 'Tiền mặt'),
    ('GD06', 'Tiền mặt'),
    ('GD06', 'Chuyển khoản ngân hàng');
