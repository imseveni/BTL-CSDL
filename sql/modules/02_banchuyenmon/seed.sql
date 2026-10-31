-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
USE QuanLyCauLacBo;


-- Mã riêng BCM02_*, không dùng mã ban nền của Tú. Không tạo/sửa thành viên ở đây.
INSERT INTO BanChuyenMon(MaBan, TenBan)
SELECT 'BCM02_CN', 'Ban Công nghệ' WHERE NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = 'BCM02_CN');
INSERT INTO BanChuyenMon(MaBan, TenBan)
SELECT 'BCM02_TT', 'Ban Truyền thông' WHERE NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = 'BCM02_TT');
INSERT INTO BanChuyenMon(MaBan, TenBan)
SELECT 'BCM02_NS', 'Ban Nhân sự' WHERE NOT EXISTS (SELECT 1 FROM BanChuyenMon WHERE MaBan = 'BCM02_NS');
