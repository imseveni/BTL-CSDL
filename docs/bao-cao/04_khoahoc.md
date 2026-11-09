# Module 04 – Quản lý khóa học

Phụ trách: Chiến. Kết quả kiểm tra ngày 30/10/2026.

## Bảng và ràng buộc

| Bảng | Cột | Ý nghĩa/ràng buộc |
|---|---|---|
| KhoaHoc | MaKhoaHoc VARCHAR(20) | Khóa chính |
| KhoaHoc | TenKhoaHoc VARCHAR(150) | Bắt buộc |
| KhoaHoc | NgayKhaiGiang DATE | Bắt buộc |
| KhoaHoc | NgayBeGiang DATE | Có thể NULL; nếu có phải >= ngày khai giảng |
| ThamGiaKhoaHoc | MSV, MaKhoaHoc | Khóa chính ghép; một thành viên tham gia một khóa tối đa một lần |
| ThamGiaKhoaHoc | VaiTro VARCHAR(100) | Vai trò, có thể NULL |
| ThamGiaKhoaHoc | KetQuaDanhGia VARCHAR(255) | Kết quả đánh giá, có thể NULL |

Hai khóa ngoại của ThamGiaKhoaHoc tham chiếu ThanhVien và KhoaHoc,
đều ON UPDATE CASCADE, ON DELETE CASCADE. Không sửa schema chung.
Java kiểm tra mã/tên bắt buộc, độ dài và thứ tự ngày trước khi gọi DAO.
DAO dùng PreparedStatement, try-with-resources. Điều kiện DELETE trong DAO
chặn xóa khóa còn thành viên; DELETE trực tiếp trên DB vẫn theo CASCADE của schema.

## Chức năng và truy vấn

Java hiện có thực thể, DAO, Service, Swing UI: tìm theo mã/tên, thêm/sửa/xóa
khóa học, xem/thêm/sửa/xóa lượt tham gia và cập nhật vai trò/kết quả.
Luồng tra cứu theo thành viên có thể dùng view bên dưới.

- vw_KhoaHocThanhVien: thông tin khóa học, thành viên, vai trò, kết quả.
- vw_KhoaHocThongKe: thống kê số thành viên, bao gồm khóa chưa có ai tham gia.
- sp_KhoaHocTraCuu: tìm chuỗi nguyên văn trong mã/tên; NULL hoặc chuỗi rỗng trả tất cả.
- queries.sql có DROP IF EXISTS trước mỗi đối tượng và chạy lại được.

```sql
CALL sp_KhoaHocTraCuu('SQL');
SELECT * FROM vw_KhoaHocThanhVien WHERE MaKhoaHoc = 'KH01';
SELECT * FROM vw_KhoaHocThanhVien WHERE MSV = 'ma-thanh-vien-thuc-te';
```

## Dữ liệu mẫu

seed.sql thêm KH01 (SQL cơ bản), KH02 (Java JDBC).
Nếu đã có thành viên, gắn tối đa hai MSV đầu tiên theo thứ tự mã vào KH01.
Không tạo mã thành viên giả trong seed module. Hiện seed_master.sql chỉ có TODO,
nên trên DB trống có hai khóa và chưa có lượt tham gia mẫu. Cần Tú bổ sung seed nền.
seed.sql dùng cho lần dựng DB sạch; không thiết kế để nạp lại trên DB đã có KH01/KH02.

## Chạy và kết quả test

Chạy MySQL client từ thư mục sql, trên DB thử nghiệm có thể xóa dữ liệu:

```sql
SOURCE reset_db.sql;
SOURCE run_all.sql;
SOURCE modules/04_khoahoc/tests.sql;
```

reset_db.sql xóa toàn bộ QuanLyCauLacBo. run_all.sql không tự gọi tests.sql.
tests.sql tự tạo fixture ban/thành viên với tiền tố KH_TEST_, chạy trong transaction
và ROLLBACK cả khi thành công lẫn khi gặp lỗi. Thủ tục test được xóa sau khi chạy thành công.
Nếu test thất bại, lỗi được phát lại; chạy lại file sẽ DROP thủ tục còn sót.

| Ca thử | Mong đợi | Kết quả |
|---|---|---|
| Thêm khóa, thêm thành viên, cập nhật đánh giá | View trả đúng kết quả | PASS |
| Ngày bế giảng NULL | Hợp lệ | PASS |
| Thống kê khóa có một người/khóa trống | 1/0 | PASS |
| INSERT và UPDATE ngày kết thúc trước ngày bắt đầu | CHECK, mã lỗi 3819 | PASS |
| Tham gia trùng | PK, mã lỗi 1062 | PASS |
| MSV hoặc khóa học không tồn tại | FK, mã lỗi 1452 | PASS |
| Xóa có điều kiện khi còn người, rồi xóa sau khi bỏ lượt tham gia | Chặn trước, thành công sau | PASS |

Kiểm tra thực tế bằng MySQL Community Server 8.0.46 trên instance riêng:
run_all.sql chạy không lỗi từ DB trống, file queries.sql và tests.sql chạy lại không lỗi.
Sau test còn hai khóa mẫu; không còn thành viên test.

```text
PASS: KhoaHoc - du lieu hop le, thong ke, CHECK, PK, FK, xoa an toan
```

Phạm vi kiểm tra trên là SQL, không phải kiểm thử thao tác Swing tự động.
Maven biên dịch Java thành công trong bước chạy test; test chung MainMenuTest thất bại
vì menu hiện đăng ký 8 module thay vì 10. Cần Tú tích hợp lại hai module vào menu chung.
