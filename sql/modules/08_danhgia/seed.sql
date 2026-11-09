-- Phụ trách: Đông. Chạy lại được; không thay đổi schema dùng chung.
-- Báo cho MySQL biết file này là UTF-8 (nếu thiếu, console Windows làm hỏng chữ tiếng Việt khi nạp).
SET NAMES utf8mb4;
USE QuanLyCauLacBo;


-- Không tạo MSV giả khi seed_master còn trống. Chỉ tham chiếu một MSV đã có thực tế.
-- Nếu chưa có thành viên, câu lệnh chèn 0 dòng; chạy lại file sau khi Tú điền seed nền.
INSERT INTO DanhGia(MaGhiNhan, LoaiDanhGia, HinhThuc, LyDo, NgayQuyetDinh, MSV)
SELECT 'DG08_01', 'Khen thưởng', 'Biểu dương', 'Dữ liệu minh họa cho module đánh giá', '2026-10-01', tv.MSV
FROM ThanhVien tv
WHERE NOT EXISTS (SELECT 1 FROM DanhGia WHERE MaGhiNhan = 'DG08_01')
ORDER BY tv.MSV LIMIT 1;
