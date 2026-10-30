-- Phụ trách: Trường. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;

-- Mã riêng TB09_*, không dùng mã nền của Tú. Không tạo/sửa thành viên ở đây.
-- Người quản lý chỉ tham chiếu MSV đã có thật trong ThanhVien (theo thứ tự MSV).
-- Nếu seed_master còn trống thì MSVQuanLy = NULL (schema cho phép); chạy lại file khi đã có thành viên
-- thì thiết bị đã có giữ nguyên, thiết bị chưa có được thêm.
-- SoLuongKhaDung ban đầu = TongSoLuong; seed module 10 sẽ tự đồng bộ lại theo phiếu mượn.
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_01', 'Máy chiếu', 3, 3, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 0)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_01');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_02', 'Loa kéo di động', 2, 2, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 0)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_02');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_03', 'Micro không dây', 6, 6, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 1)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_03');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_04', 'Bàn gấp', 10, 10, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 1)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_04');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_05', 'Ghế nhựa', 40, 40, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 2)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_05');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_06', 'Banner khung treo', 4, 4, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 2)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_06');
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_07', 'Máy ảnh DSLR', 2, 2, (SELECT MSV FROM ThanhVien ORDER BY MSV LIMIT 1 OFFSET 3)
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_07');
-- Thiết bị chưa có người quản lý (MSVQuanLy cho phép NULL, DECISIONS #4)
INSERT INTO CoSoVatChat(MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy)
SELECT 'TB09_08', 'Ổ cắm kéo dài', 5, 5, NULL
WHERE NOT EXISTS (SELECT 1 FROM CoSoVatChat WHERE MaThietBi = 'TB09_08');
